package download

import (
	"net/http"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/render"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/log"
)

// PathVersionInfo URL.
const PathVersionInfo = "/{module:.+}/@v/{version}.info"

// VersionInfoHandler implements GET baseURL/module/@v/version.info
func VersionInfoHandler(dp Protocol, lggr log.Entry, eng *render.Engine) buffalo.Handler {
	const op errors.Op = "download.versionInfoHandler"
	return func(c buffalo.Context) error {
		sp := buffet.SpanFromContext(c).SetOperationName("versionInfoHandler")
		defer sp.Finish()
		mod, ver, err := getModuleParams(op, c)
		if err != nil {
			lggr.SystemErr(err)
			return c.Render(errors.Kind(err), nil)
		}
		info, err := dp.Info(c, mod, ver)
		if err != nil {
			lggr.SystemErr(errors.E(op, err, errors.M(mod), errors.V(ver)))
			return c.Render(errors.Kind(err), nil)
		}

		return c.Render(http.StatusOK, eng.String(string(info)))
	}
}
