package download

import (
	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/render"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/log"
)

// PathVersionModule URL.
const PathVersionModule = "/{module:.+}/@v/{version}.mod"

// VersionModuleHandler implements GET baseURL/module/@v/version.mod
func VersionModuleHandler(dp Protocol, lggr log.Entry, eng *render.Engine) buffalo.Handler {
	const op errors.Op = "download.VersionModuleHandler"
	return func(c buffalo.Context) error {
		sp := buffet.SpanFromContext(c).SetOperationName("VersionModuleHandler")
		defer sp.Finish()
		mod, ver, err := getModuleParams(op, c)
		if err != nil {
			lggr.SystemErr(err)
			return c.Render(errors.Kind(err), nil)
		}
		modBts, err := dp.GoMod(c, mod, ver)
		if err != nil {
			err = errors.E(op, errors.M(mod), errors.V(ver), err)
			lggr.SystemErr(err)
			return c.Render(errors.Kind(err), nil)
		}

		// Calling c.Response().Write will write the header directly
		// and we would get a 0 status in the buffalo logs.
		return c.Render(200, eng.String(string(modBts)))
	}
}
