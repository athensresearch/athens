package download

import (
	"net/http"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/render"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/log"
)

// PathVersionModule URL.
const PathVersionModule = "/{module:.+}/@v/{version}.mod"

// VersionModuleHandler implements GET baseURL/module/@v/version.mod
func VersionModuleHandler(dp Protocol, lggr *log.Logger, eng *render.Engine) buffalo.Handler {
	const op errors.Op = "download.VersionModuleHandler"
	return func(c buffalo.Context) error {
		sp := buffet.SpanFromContext(c).SetOperationName("VersionModuleHandler")
		defer sp.Finish()
		mod, ver, verInfo, err := getModuleVersion(c, lggr, dp)
		if err != nil {
			err = errors.E(op, errors.M(mod), errors.V(ver), err)
			lggr.SystemErr(err)
			c.Render(errors.Kind(err), nil)
		}
		verInfo.Zip.Close()
		status := http.StatusOK
		_, err = c.Response().Write(verInfo.Mod)
		if err != nil {
			err = errors.E(op, errors.M(mod), errors.V(ver), err)
			status = http.StatusInternalServerError
			lggr.SystemErr(err)
		}

		c.Response().WriteHeader(status)
		return nil
	}
}
