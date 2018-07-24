package download

import (
	"net/http"
	"strings"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/render"
	"github.com/gomods/athens/pkg/paths"
	"github.com/gomods/athens/pkg/storage"
	errs "github.com/pkg/errors"
)

// PathList URL.
const PathList = "/{module:.+}/@v/list"

// ListHandler implements GET baseURL/module/@v/list
func ListHandler(lister storage.Lister, eng *render.Engine) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		sp := buffet.SpanFromContext(c)
		sp.SetOperationName("listHandler")
		mod, err := paths.GetModule(c)
		if err != nil {
			return err
		}
		versions, err := lister.List(c, mod)
		if storage.IsNotFoundError(err) {
			return c.Render(http.StatusNotFound, eng.JSON(err.Error()))
		} else if err != nil {
			return errs.WithStack(err)
		}
		return c.Render(http.StatusOK, eng.String(strings.Join(versions, "\n")))
	}
}
