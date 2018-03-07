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
	"github.com/gomods/athens/pkg/storage"
	"github.com/gomods/athens/pkg/storage/memory"
	"github.com/gomods/athens/pkg/user"
	"github.com/gomods/athens/pkg/user/mongo"
	"github.com/rs/cors"
	"github.com/unrolled/secure"

	"github.com/markbates/goth/gothic"
)

// ENV is used to help switch settings based on where the
// application is being run. Default is "development".
var ENV = envy.Get("GO_ENV", "development")
var app *buffalo.App
var T *i18n.Translator

var gopath string
var storageReader storage.Reader
var storageWriter storage.Saver
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
	mem := memory.New()
	storageReader := mem
	storageWriter := mem
	mgoStore := mongo.NewMongoUserStore("127.0.0.1:27017")
	err := mgoStore.Connect()
	if err != nil {
		panic(err)
	}

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

		// serve go-get requests
		app.Use(GoGet())
		app.GET("/", homeHandler)

		app.GET("/{base_url:.+}/{module}/@v/list", listHandler(storageReader))
		app.GET("/{base_url:.+}/{module}/@v/{version}.info", versionInfoHandler(storageReader))
		app.GET("/{base_url:.+}/{module}/@v/{version}.mod", versionModuleHandler(storageReader))
		app.GET("/{base_url:.+}/{module}/@v/{version}.zip", versionZipHandler(storageReader))
		app.POST("/admin/upload/{base_url:[a-zA-Z./]+}/{module}/{version}", uploadHandler(storageWriter))

		auth := app.Group("/auth")
		auth.GET("/{provider}", buffalo.WrapHandlerFunc(gothic.BeginAuthHandler))
		auth.GET("/{provider}/callback", authCallback(mgoStore))
		//	app.GET("/{base_url:.+}/{module}", homeHandler)
		// serve files from the public directory:

		// has to be last
		app.ServeFiles("/", assetsBox)
	}

	return app
}
