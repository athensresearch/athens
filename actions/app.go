package actions

import (
	"log"

	// "github.com/gomods/athens/models"
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/middleware"
	"github.com/gobuffalo/buffalo/middleware/csrf"
	"github.com/gobuffalo/buffalo/middleware/i18n"
	"github.com/gobuffalo/buffalo/middleware/ssl"
	"github.com/gobuffalo/envy"
	"github.com/gobuffalo/packr"
	"github.com/gomods/athens/pkg/user"
	"github.com/rs/cors"
	"github.com/unrolled/secure"
)

// ENV is used to help switch settings based on where the
// application is being run. Default is "development".
var ENV = envy.Get("GO_ENV", "development")

// MODE identifies whether athens is running in proxy or registry mode.
//
// valid values are "proxy" or "registry"
var MODE = envy.Get("ATHENS_MODE", "proxy")
var app *buffalo.App

// T is the translator to use
var T *i18n.Translator

var gopath string
var userStore *user.Store

func init() {
	g, err := envy.MustGet("GOPATH")
	if err != nil {
		log.Fatalf("GOPATH is not set!")
	}
	gopath = g
}

// App is where all routes and middleware for buffalo
// should be defined. This is the nerve center of your
// application.
func App() *buffalo.App {
	if app == nil {
		app = buffalo.New(buffalo.Options{
			Env: ENV,
			PreWares: []buffalo.PreWare{
				cors.Default().Handler,
			},
			SessionName: "_athens_session",
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
		csrfMiddleware := csrf.New
		app.Use(csrfMiddleware)

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

		if MODE == "proxy" {
			log.Printf("starting athens in proxy mode")
			if err := addProxyRoutes(app); err != nil {
				log.Fatalf("error adding proxy routes (%s)", err)
				return nil
			}
		} else if MODE == "registry" {
			log.Printf("starting athens in registry mode")
			if err := addRegistryRoutes(app); err != nil {
				log.Fatalf("error adding registry routes (%s)", err)
				return nil
			}
		} else {
			log.Fatalf("unsupported mode %s, exiting", MODE)
			return nil
		}

		// serve files from the public directory:
		// has to be last
		app.ServeFiles("/", assetsBox)

	}

	return app
}
