package middleware

import (
	"github.com/gobuffalo/buffalo"
)

// CacheControl takes a string and makes a header value to the key Cache-Control.
// This is so you can set some sane cache defaults to certain endpoints.
func CacheControl(cacheHeaderValue string) buffalo.MiddlewareFunc {
	return func(next buffalo.Handler) buffalo.Handler {
		return func(c buffalo.Context) error {
			c.Response().Header().Set("Cache-Control", cacheHeaderValue)
			return next(c)
		}
	}
}
