package middleware

import (
	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/log"
	"github.com/sirupsen/logrus"
)

type middlewareFunc func(entry log.Entry, validatorHook string) buffalo.MiddlewareFunc

// LogEntryMiddleware builds a log.Entry applying the request parameter to the given
// log.Logger and propagates it to the given MiddlewareFunc
func LogEntryMiddleware(middleware middlewareFunc, lggr *log.Logger, validatorHook string) buffalo.MiddlewareFunc {
	return func(next buffalo.Handler) buffalo.Handler {
		return func(c buffalo.Context) error {
			req := c.Request()
			ent := lggr.WithFields(logrus.Fields{
				"http-method": req.Method,
				"http-path":   req.URL.Path,
				"http-url":    req.URL.String(),
			})
			m := middleware(ent, validatorHook)
			return m(next)(c)
		}
	}
}
