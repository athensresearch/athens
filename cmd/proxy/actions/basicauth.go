package actions

import (
	"crypto/subtle"
	"net/http"

	"github.com/gobuffalo/buffalo"
)

func basicAuth(user, pass string) buffalo.MiddlewareFunc {
	return func(next buffalo.Handler) buffalo.Handler {
		return func(c buffalo.Context) error {
			if !checkAuth(c.Request(), user, pass) {
				c.Response().Header().Set("WWW-Authenticate", `Basic realm="basic auth required"`)
				c.Render(401, nil)
				return nil
			}

			return next(c)
		}
	}
}

func checkAuth(r *http.Request, user, pass string) bool {
	givenUser, givenPass, ok := r.BasicAuth()
	if !ok {
		return false
	}

	isUser := subtle.ConstantTimeCompare([]byte(user), []byte(givenUser))
	if isUser != 1 {
		return false
	}

	isPass := subtle.ConstantTimeCompare([]byte(pass), []byte(givenPass))
	if isPass != 1 {
		return false
	}

	return true
}
