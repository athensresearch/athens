package actions

import (
	"log"

	// "github.com/gomods/athens/models"
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/middleware"
	"github.com/gomods/athens/pkg/storage"
	"github.com/gomods/athens/pkg/storage/memory"
	// "github.com/gobuffalo/buffalo/middleware/csrf"
	"github.com/gobuffalo/buffalo/middleware/i18n"
	"github.com/gobuffalo/buffalo/middleware/ssl"
	"github.com/gobuffalo/envy"
	"github.com/gobuffalo/packr"
	"github.com/unrolled/secure"
)

// ENV is used to help switch settings based on where the
// application is being run. Default is "development".
var ENV = envy.Get("GO_ENV", "development")
var app *buffalo.App
var T *i18n.Translator

var gopath string
var storageReader storage.Reader
var storageWriter storage.Saver

func init() {
	g, err := envy.MustGet("GOPATH")
	if err != nil {
		log.Fatalf("GOPATH is not set!")
	}
	gopath = g
	storageReader = storage.Reader{
		Lister: &memory.Lister{},
		Getter: &memory.Getter{},
	}
	storageWriter = &memory.Saver{}
}

// App is where all routes and middleware for buffalo
// should be defined. This is the nerve center of your
// application.
func App() *buffalo.App {
	if app == nil {
		app = buffalo.New(buffalo.Options{
			Env:         ENV,
			SessionName: "_toodo_session",
		})
		// Automatically redirect to SSL
		app.Use(ssl.ForceSSL(secure.Options{
			SSLRedirect:     ENV == "production",
			SSLProxyHeaders: map[string]string{"X-Forwarded-Proto": "https"},
		}))

		if ENV == "development" {
			app.Use(middleware.ParameterLogger)
		}

		// Protect against CSRF attacks. https://www.owasp.org/index.php/Cross-Site_Request_Forgery_(CSRF)
		// Remove to disable this.
		// csrfMiddleware := csrf.New
		// app.Use(csrfMiddleware)

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

		app.GET("/all", allHandler(storageReader))
		app.GET("/{base_url:.+}/{module}/@v/list", listHandler(storageReader))
		app.GET("/{base_url:.+}/{module}/@v/{version}.info", versionInfoHandler(storageReader))
		app.GET("/{base_url:.+}/{module}/@v/{version}.mod", versionModuleHandler(storageReader))
		app.GET("/{base_url:.+}/{module}/@v/{version}.zip", versionZipHandler(storageReader))
		app.POST("/admin/upload/{base_url:[a-zA-Z./]+}/{module}/{version}", uploadHandler(storageWriter))

		// serve files from the public directory:
		app.ServeFiles("/", assetsBox)
	}

	return app
}
