package actions

import (
	stdlog "log"

	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/middleware"
	"github.com/gobuffalo/buffalo/middleware/csrf"
	"github.com/gobuffalo/buffalo/middleware/i18n"
	"github.com/gobuffalo/buffalo/middleware/ssl"
	"github.com/gobuffalo/buffalo/worker"
	"github.com/gobuffalo/gocraft-work-adapter"
	"github.com/gobuffalo/packr"
	"github.com/gocraft/work"
	"github.com/gomods/athens/pkg/cdn/metadata/azurecdn"
	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/download"
	"github.com/gomods/athens/pkg/download/goget"
	"github.com/gomods/athens/pkg/eventlog"
	"github.com/gomods/athens/pkg/log"
	"github.com/gomods/athens/pkg/storage"
	"github.com/gomodule/redigo/redis"
	"github.com/rs/cors"
	"github.com/unrolled/secure"
)

// AppConfig contains dependencies used in App
type AppConfig struct {
	Storage        storage.Backend
	EventLog       eventlog.Eventlog
	CacheMissesLog eventlog.Appender
}

const (
	// OlympusWorkerName is the name of the Olympus worker
	OlympusWorkerName = "olympus-worker"
	// DownloadHandlerName is name of the handler downloading packages from VCS
	DownloadHandlerName = "download-handler"
	// PushNotificationHandlerName is the name of the handler processing push notifications
	PushNotificationHandlerName = "push-notification-worker"
)

var (
	workerQueue               = "default"
	workerModuleKey           = "module"
	workerVersionKey          = "version"
	workerPushNotificationKey = "push-notification"
	// ENV is used to help switch settings based on where the
	// application is being run. Default is "development".
	ENV = env.GoEnvironmentWithDefault("development")
	// T is buffalo Translator
	T *i18n.Translator
)

// App is where all routes and middleware for buffalo should be defined.
// This is the nerve center of your application.
func App(config *AppConfig) (*buffalo.App, error) {
	port := env.Port(":3001")

	w, err := getWorker(config.Storage, config.EventLog)
	if err != nil {
		return nil, err
	}

	lggr := log.New(env.CloudRuntime(), env.LogLevel())
	app := buffalo.New(buffalo.Options{
		Addr: port,
		Env:  ENV,
		PreWares: []buffalo.PreWare{
			cors.Default().Handler,
		},
		SessionName: "_olympus_session",
		Worker:      w,
		WorkerOff:   true, // TODO(marwan): turned off until worker is being used.
		Logger:      log.Buffalo(),
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

	// TODO: parameterize the GoGet getter here.
	//
	// Defaulting to Azure for now
	app.Use(GoGet(azurecdn.Metadata{
		// TODO: initialize the azurecdn.Storage struct here
	}))

	// Wraps each request in a transaction.
	//  c.Value("tx").(*pop.PopTransaction)
	// Remove to disable this.
	// app.Use(middleware.PopTransaction(models.DB))

	// Setup and use translations:
	if T, err = i18n.New(packr.NewBox("../locales"), "en-US"); err != nil {
		app.Stop(err)
	}
	app.Use(T.Middleware())

	app.GET("/diff/{lastID}", diffHandler(config.Storage, config.EventLog))
	app.GET("/feed/{lastID}", feedHandler(config.Storage))
	app.GET("/eventlog/{sequence_id}", eventlogHandler(config.EventLog))
	app.POST("/cachemiss", cachemissHandler(w))
	app.POST("/push", pushNotificationHandler(w))

	// Download Protocol
	gg, err := goget.New()
	if err != nil {
		return nil, err
	}
	dp := download.New(gg, config.Storage)
	opts := &download.HandlerOpts{Protocol: dp, Logger: lggr, Engine: renderEng}
	download.RegisterHandlers(app, opts)

	app.ServeFiles("/", assetsBox) // serve files from the public directory

	return app, nil
}

func getWorker(store storage.Backend, eLog eventlog.Eventlog) (worker.Worker, error) {
	workerType := env.OlympusBackgroundWorkerType()
	switch workerType {
	case "redis":
		return registerRedis(store, eLog)
	case "memory":
		return registerInMem(store, eLog)
	default:
		stdlog.Printf("Provided background worker type %s. Expected redis|memory. Defaulting to memory", workerType)
		return registerInMem(store, eLog)
	}
}

func registerInMem(store storage.Backend, eLog eventlog.Eventlog) (worker.Worker, error) {
	w := worker.NewSimple()
	if err := w.Register(PushNotificationHandlerName, GetProcessPushNotificationJob(store, eLog)); err != nil {
		return nil, err
	}
	return w, nil
}

func registerRedis(store storage.Backend, eLog eventlog.Eventlog) (worker.Worker, error) {
	port := env.OlympusRedisQueuePortWithDefault(":6379")
	w := gwa.New(gwa.Options{
		Pool: &redis.Pool{
			MaxActive: 5,
			MaxIdle:   5,
			Wait:      true,
			Dial: func() (redis.Conn, error) {
				return redis.Dial("tcp", port)
			},
		},
		Name:           OlympusWorkerName,
		MaxConcurrency: env.AthensMaxConcurrency(),
	})

	opts := work.JobOptions{
		SkipDead: true,
		MaxFails: env.WorkerMaxFails(),
	}

	return w, w.RegisterWithOptions(PushNotificationHandlerName, opts, GetProcessPushNotificationJob(store, eLog))
}
