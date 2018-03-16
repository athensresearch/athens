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
	sp, err := getStandardParams(c)
	if err != nil {
		return err
	}
	loc, err := getter.Get(sp.baseURL, sp.module)
	if err != nil {
		return c.Error(
			http.StatusNotFound,
			fmt.Errorf("%s/%s does not exist", sp.baseURL, sp.module),
		)
	}
	c.Set("redirectLoc", loc)
	c.Set("baseURL", sp.baseURL)
	c.Set("module", sp.module)
	return c.Render(http.StatusOK, r.HTML("goget.html"))
}
