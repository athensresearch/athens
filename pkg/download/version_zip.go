package download

import (
	"io"
	"net/http"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/render"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/log"
)

// PathVersionZip URL.
const PathVersionZip = "/{module:.+}/@v/{version}.zip"

// VersionZipHandler implements GET baseURL/module/@v/version.zip
func VersionZipHandler(dp Protocol, lggr *log.Logger, eng *render.Engine) buffalo.Handler {
	const op errors.Op = "download.VersionZipHandler"

	return func(c buffalo.Context) error {
		sp := buffet.SpanFromContext(c)
		sp.SetOperationName("versionZipHandler")
		defer sp.Finish()
		mod, ver, verInfo, err := getModuleVersion(c, lggr, dp)
		if err != nil {
			err := errors.E(op, errors.M(mod), errors.V(ver), err)
			lggr.SystemErr(err)
			return c.Render(http.StatusInternalServerError, nil)
		}

		status := http.StatusOK
		_, err = io.Copy(c.Response(), verInfo.Zip)
		if err != nil {
			status = http.StatusInternalServerError
			lggr.SystemErr(errors.E(op, errors.M(mod), errors.V(ver), err))
		}

		c.Response().WriteHeader(status)
		return nil
	}
}
