package actions

import (
	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/download"
	"github.com/gomods/athens/pkg/download/goget"
	"github.com/gomods/athens/pkg/log"
	"github.com/gomods/athens/pkg/module"
	"github.com/gomods/athens/pkg/storage"
)

func addProxyRoutes(
	app *buffalo.App,
	storage storage.Backend,
	mf *module.Filter,
	lggr *log.Logger,
) error {
	app.GET("/", proxyHomeHandler)

	dp := download.New(goget.New(), storage)
	// Download Protocol
	app.GET(download.PathList, download.ListHandler(dp, lggr, proxy))
	app.GET(download.PathLatest, download.LatestHandler(dp, lggr, proxy))
	app.GET(download.PathVersionInfo, download.VersionInfoHandler(dp, lggr, proxy))
	app.GET(download.PathVersionModule, download.VersionModuleHandler(dp, lggr, proxy))
	app.GET(download.PathVersionZip, download.VersionZipHandler(dp, lggr, proxy))

	app.POST("/admin/fetch/{module:[a-zA-Z./]+}/{owner}/{repo}/{ref}/{version}", fetchHandler(storage))
	return nil
}
