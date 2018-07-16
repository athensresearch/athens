package actions

import (
	"fmt"
	"net/http"
	"strings"

	"github.com/gobuffalo/buffalo"
	cdnmetadata "github.com/gomods/athens/pkg/cdn/metadata"
	"github.com/gomods/athens/pkg/paths"
)

// GoGet is middleware that checks for the 'go-get=1' query string. If it exists,
// uses getter to determine the redirect location
func GoGet(getter cdnmetadata.Getter) buffalo.MiddlewareFunc {
	return func(next buffalo.Handler) buffalo.Handler {
		return func(c buffalo.Context) error {
			if strings.Contains(c.Request().URL.Query().Get("go-get"), "1") {
				return goGetMeta(c, getter)
			}
			return next(c)
		}
	}
}

func goGetMeta(c buffalo.Context, getter cdnmetadata.Getter) error {
	params, err := paths.GetAllParams(c)
	if err != nil {
		return err
	}
	loc, err := getter.Get(params.Module)
	if err != nil {
		msg := fmt.Sprintf("module %s does not exist", params.Module)
		return c.Render(http.StatusNotFound, renderEng.JSON(msg))
	}
	c.Set("redirectLoc", loc)
	c.Set("module", params.Module)
	return c.Render(http.StatusOK, renderEng.HTML("goget.html"))
}
