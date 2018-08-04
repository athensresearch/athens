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
	s storage.Backend,
	mf *module.Filter,
	l *log.Logger,
) error {
	app.GET("/", proxyHomeHandler)

	// Download Protocol
	gg, err := goget.New()
	if err != nil {
		return err
	}
	p := download.New(gg, s)
	opts := &download.HandlerOpts{Protocol: p, Logger: l, Engine: proxy}
	download.RegisterHandlers(app, opts)

	return nil
}
