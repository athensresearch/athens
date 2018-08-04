package download

import (
	"net/http"
	"strings"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/render"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/log"
	"github.com/gomods/athens/pkg/paths"
)

// PathList URL.
const PathList = "/{module:.+}/@v/list"

// ListHandler implements GET baseURL/module/@v/list
func ListHandler(dp Protocol, lggr log.Entry, eng *render.Engine) buffalo.Handler {
	const op errors.Op = "download.ListHandler"
	return func(c buffalo.Context) error {
		sp := buffet.SpanFromContext(c).SetOperationName("listHandler")
		defer sp.Finish()
		mod, err := paths.GetModule(c)
		if err != nil {
			lggr.SystemErr(errors.E(op, err))
			return c.Render(500, nil)
		}

		versions, err := dp.List(c, mod)
		if err != nil {
			lggr.SystemErr(errors.E(op, err))
			return c.Render(errors.Kind(err), eng.JSON(errors.KindText(err)))
		}

		return c.Render(http.StatusOK, eng.String(strings.Join(versions, "\n")))
	}
}
