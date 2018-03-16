package actions

import (
	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/user/mongo"
	"github.com/markbates/goth/gothic"
)

func addRegistryRoutes(app *buffalo.App) error {
	cdnGetter := newCDNGetter()
	mgoStore := mongo.NewMongoUserStore("127.0.0.1:27017")
	if err := mgoStore.Connect(); err != nil {
		return err
	}
	// serve go-get requests
	app.Use(GoGet(cdnGetter))
	auth := app.Group("/auth")
	auth.GET("/{provider}", buffalo.WrapHandlerFunc(gothic.BeginAuthHandler))
	auth.GET("/{provider}/callback", authCallback(mgoStore))
	//	app.GET("/{base_url:.+}/{module}", homeHandler)
	return nil
}
