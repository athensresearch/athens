package actions

import (
	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/download"
	"github.com/gomods/athens/pkg/download/goget"
	"github.com/gomods/athens/pkg/log"
	"github.com/gomods/athens/pkg/storage"
)

func addProxyRoutes(
	app *buffalo.App,
	s storage.Backend,
	l *log.Logger,
) error {
	app.GET("/", proxyHomeHandler)
	app.GET("/healthz", healthHandler)

	// Download Protocol
	gg, err := goget.New()
	if err != nil {
		return err
	}
	p := download.New(gg, s, env.GoGetWorkers())
	opts := &download.HandlerOpts{Protocol: p, Logger: l, Engine: proxy}
	download.RegisterHandlers(app, opts)

	return nil
}
