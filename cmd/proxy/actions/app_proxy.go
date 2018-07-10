package actions

import (
	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/download"
	"github.com/gomods/athens/pkg/module"
	"github.com/gomods/athens/pkg/storage"
)

func addProxyRoutes(app *buffalo.App, storage storage.Backend, mf *module.Filter) error {
	app.GET("/", proxyHomeHandler)

	// Download Protocol
	app.GET(download.PathList, download.ListHandler(storage, proxy))
	app.GET(download.PathVersionInfo, cacheMissHandler(download.VersionInfoHandler(storage, proxy), app.Worker, mf))
	app.GET(download.PathVersionModule, cacheMissHandler(download.VersionModuleHandler(storage), app.Worker, mf))
	app.GET(download.PathVersionZip, cacheMissHandler(download.VersionZipHandler(storage), app.Worker, mf))

	app.POST("/admin/fetch/{module:[a-zA-Z./]+}/{owner}/{repo}/{ref}/{version}", fetchHandler(storage))
	return nil
}
