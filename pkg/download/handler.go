package download

import (
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/render"
	"github.com/gomods/athens/pkg/log"
	"github.com/sirupsen/logrus"
)

// ProtocolHandler is a function that takes all that it needs to return
// a ready-to-go buffalo handler that serves up cmd/go's download protocol.
type ProtocolHandler func(dp Protocol, lggr log.Entry, eng *render.Engine) buffalo.Handler

// HandlerOpts are the generic options
// for a ProtocolHandler
type HandlerOpts struct {
	Protocol Protocol
	Logger   *log.Logger
	Engine   *render.Engine
}

// LogEntryHandler constructs a log.Entry out of the given
// *log.Logger so that it applies default fields to every single
// incoming request without having to do those in every single handler.
// This is like a middleware minus the context magic.
func LogEntryHandler(ph ProtocolHandler, opts *HandlerOpts) buffalo.Handler {
	return func(c buffalo.Context) error {
		req := c.Request()
		ent := opts.Logger.WithFields(logrus.Fields{
			"http-method": req.Method,
			"http-path":   req.URL.Path,
			"http-url":    req.URL.String(),
		})

		handler := ph(opts.Protocol, ent, opts.Engine)

		return handler(c)
	}
}

// RegisterHandlers is a convenience method that registers
// all the download protocol paths for you.
func RegisterHandlers(app *buffalo.App, opts *HandlerOpts) {
	// If true, this would only panic at boot time, static nil checks anyone?
	if opts == nil || opts.Protocol == nil || opts.Engine == nil || opts.Logger == nil {
		panic("absolutely unacceptable handler opts")
	}

	app.GET(PathList, LogEntryHandler(ListHandler, opts))
	app.GET(PathLatest, LogEntryHandler(LatestHandler, opts))
	app.GET(PathVersionInfo, LogEntryHandler(VersionInfoHandler, opts))
	app.GET(PathVersionModule, LogEntryHandler(VersionModuleHandler, opts))
	app.GET(PathVersionZip, LogEntryHandler(VersionZipHandler, opts))
}
