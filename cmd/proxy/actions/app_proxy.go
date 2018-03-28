package actions

import (
	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/storage"
)

func addProxyRoutes(app *buffalo.App, storage storage.Backend) error {
	app.GET("/", proxyHomeHandler)
	app.GET("/{module:.+}/@v/list", listHandler(storage))
	app.GET("/{module:.+}/@v/{version}.info", versionInfoHandler(storage))
	app.GET("/{module:.+}/@v/{version}.mod", versionModuleHandler(storage))
	app.GET("/{module:.+}/@v/{version}.zip", versionZipHandler(storage))
	app.POST("/admin/upload/{module:[a-zA-Z./]+}/{version}", uploadHandler(storage))
	app.POST("/admin/fetch/{module:[a-zA-Z./]+}/{owner}/{repo}/{ref}/{version}", fetchHandler(storage))
	return nil
}
