package actions

import (
	"fmt"
	"log"

	"github.com/garyburd/redigo/redis"
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/middleware"
	"github.com/gobuffalo/buffalo/middleware/csrf"
	"github.com/gobuffalo/buffalo/middleware/i18n"
	"github.com/gobuffalo/buffalo/middleware/ssl"
	"github.com/gobuffalo/buffalo/worker"
	"github.com/gobuffalo/gocraft-work-adapter"
	"github.com/gobuffalo/packr"
	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/module"
	"github.com/gomods/athens/pkg/user"
	"github.com/rs/cors"
	"github.com/unrolled/secure"
)

const (
	// FetcherWorkerName is the name of the worker fetching sources from experienced cache misses
	FetcherWorkerName = "olympusfetcher"
	// ReporterWorkerName is the name of the worker reporting cache misses
	ReporterWorkerName = "olympusreporter"
	workerQueue        = "default"
	workerModuleKey    = "module"
	workerVersionKey   = "version"

	maxTryCount = 5
)

// ENV is used to help switch settings based on where the
// application is being run. Default is "development".
var ENV = env.GoEnvironmentWithDefault("development")

var app *buffalo.App

// T is the translator to use
var T *i18n.Translator

var gopath string
var userStore *user.Store

func init() {
	g, err := env.GoPath()
	if err != nil {
		log.Fatal(err)
	}
	gopath = g
}

// App is where all routes and middleware for buffalo
// should be defined. This is the nerve center of your
// application.
func App() (*buffalo.App, error) {
	if app == nil {
		redisPort := env.RedisQueuePortWithDefault(":6379")

		app = buffalo.New(buffalo.Options{
			Env: ENV,
			PreWares: []buffalo.PreWare{
				cors.Default().Handler,
			},
			SessionName: "_athens_session",
			Worker:      getWorker(redisPort),
		})
		// Automatically redirect to SSL
		app.Use(ssl.ForceSSL(secure.Options{
			SSLRedirect:     ENV == "production",
			SSLProxyHeaders: map[string]string{"X-Forwarded-Proto": "https"},
		}))

		if ENV == "development" {
			app.Use(middleware.ParameterLogger)
		}
		initializeTracing(app)
		// Protect against CSRF attacks. https://www.owasp.org/index.php/Cross-Site_Request_Forgery_(CSRF)
		// Remove to disable this.
		if env.EnableCSRFProtection() {
			csrfMiddleware := csrf.New
			app.Use(csrfMiddleware)
		}

		// Wraps each request in a transaction.
		//  c.Value("tx").(*pop.PopTransaction)
		// Remove to disable this.
		// app.Use(middleware.PopTransaction(models.DB))

		// Setup and use translations:
		var err error
		if T, err = i18n.New(packr.NewBox("../locales"), "en-US"); err != nil {
			app.Stop(err)
		}
		app.Use(T.Middleware())

		app.GET("/", homeHandler)

		store, err := GetStorage()
		if err != nil {
			err = fmt.Errorf("error getting storage configuration (%s)", err)
			return nil, err
		}
		if err := store.Connect(); err != nil {
			err = fmt.Errorf("error connecting to storage (%s)", err)
			return nil, err
		}

		mf := module.NewFilter()
		if err := addProxyRoutes(app, store, mf); err != nil {
			err = fmt.Errorf("error adding proxy routes (%s)", err)
			return nil, err
		}

		// serve files from the public directory:
		// has to be last
		app.ServeFiles("/", assetsBox)

	}

	return app, nil
}

func getWorker(port string) worker.Worker {
	return gwa.New(gwa.Options{
		Pool: &redis.Pool{
			MaxActive: 5,
			MaxIdle:   5,
			Wait:      true,
			Dial: func() (redis.Conn, error) {
				return redis.Dial("tcp", port)
			},
		},
		Name:           FetcherWorkerName,
		MaxConcurrency: 25,
	})
}
