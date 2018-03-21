package actions

import (
	"github.com/gobuffalo/buffalo"
)

func addProxyRoutes(app *buffalo.App) error {
	storage, err := newStorage()
	if err != nil {
		return err
	}

	app.GET("/", proxyHomeHandler)
	app.GET("/{base_url:.+}/{module}/@v/list", listHandler(storage))
	app.GET("/{base_url:.+}/{module}/@v/{version}.info", versionInfoHandler(storage))
	app.GET("/{base_url:.+}/{module}/@v/{version}.mod", versionModuleHandler(storage))
	app.GET("/{base_url:.+}/{module}/@v/{version}.zip", versionZipHandler(storage))
	app.POST("/admin/upload/{base_url:[a-zA-Z./]+}/{module}/{version}", uploadHandler(storage))
	app.POST("/admin/fetch/{base_url:[a-zA-Z./]+}/{owner}/{repo}/{ref}/{version}", fetchHandler(storage))
	return nil
}
