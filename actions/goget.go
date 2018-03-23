package actions

import (
	"fmt"
	"net/http"
	"strings"

	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/cdn"
)

// GoGet is middleware that checks for the 'go-get=1' query string. If it exists,
// uses getter to determine the redirect location
func GoGet(getter cdn.Getter) buffalo.MiddlewareFunc {
	return func(next buffalo.Handler) buffalo.Handler {
		return func(c buffalo.Context) error {
			if strings.Contains(c.Request().URL.Query().Get("go-get"), "1") {
				return goGetMeta(c, getter)
			}
			return next(c)
		}
	}
}

func goGetMeta(c buffalo.Context, getter cdn.Getter) error {
	params, err := getAllPathParams(c)
	if err != nil {
		return err
	}
	loc, err := getter.Get(params.module)
	if err != nil {
		return c.Error(http.StatusNotFound, fmt.Errorf("module %s does not exist", params.module))
	}
	c.Set("redirectLoc", loc)
	c.Set("module", params.module)
	return c.Render(http.StatusOK, proxy.HTML("goget.html"))
}
