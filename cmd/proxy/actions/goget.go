package actions

import (
	"fmt"
	"net/http"

	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/cdn"
	"github.com/gomods/athens/pkg/paths"
)

// GoGet is middleware that checks for the 'go-get=1' query string. If it exists,
// uses getter to determine the redirect location
func GoGet(getter cdn.Getter) buffalo.MiddlewareFunc {
	return func(next buffalo.Handler) buffalo.Handler {
		return func(c buffalo.Context) error {
			if paths.IsGoGet(c.Request().URL) {
				return goGetMeta(c, getter)
			}
			return next(c)
		}
	}
}

func goGetMeta(c buffalo.Context, getter cdn.Getter) error {
	params, err := paths.GetAllParams(c)
	if err != nil {
		return err
	}
	loc, err := getter.Get(params.Module)
	if err != nil {
		return c.Error(http.StatusNotFound, fmt.Errorf("module %s does not exist", params.Module))
	}
	c.Set("redirectLoc", loc)
	c.Set("module", params.Module)
	return c.Render(http.StatusOK, proxy.HTML("goget.html"))
}
