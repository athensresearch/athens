package actions

import (
	"log"

	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/middleware"
	"github.com/gobuffalo/buffalo/middleware/ssl"
	"github.com/gobuffalo/envy"
	"github.com/rs/cors"
	"github.com/unrolled/secure"

	"github.com/gobuffalo/buffalo/middleware/csrf"
	"github.com/gobuffalo/buffalo/middleware/i18n"
	"github.com/gobuffalo/packr"
)

// ENV is used to help switch settings based on where the
// application is being run. Default is "development".
var ENV = envy.Get("GO_ENV", "development")
var app *buffalo.App

// T is buffalo Translator
var T *i18n.Translator

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
			SessionName: "_olympus_session",
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

		storage, err := newStorage()
		if err != nil {
			log.Fatalf("error creating storage (%s)", err)
			return nil
		}
		eventlogReader, err := newEventlog()
		if err != nil {
			log.Fatalf("error creating eventlog (%s)", err)
			return nil
		}

		cacheMissesLog, err := newCacheMissesLog()
		if err != nil {
			log.Fatalf("error creating cachemisses log (%s)", err)
			return nil
		}

		app.GET("/", homeHandler)
		app.GET("/diff/{lastID}", diffHandler(storage, eventlogReader))
		app.GET("/feed/{lastID}", feedHandler(storage))
		app.GET("/eventlog/{sequence_id}", eventlogHandler(eventlogReader))
		app.POST("/cachemiss", cachemissHandler(cacheMissesLog))
		app.ServeFiles("/", assetsBox) // serve files from the public directory
	}

	return app
}
