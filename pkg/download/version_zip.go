package download

import (
	"io"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/render"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/log"
)

// PathVersionZip URL.
const PathVersionZip = "/{module:.+}/@v/{version}.zip"

// VersionZipHandler implements GET baseURL/module/@v/version.zip
func VersionZipHandler(dp Protocol, lggr log.Entry, eng *render.Engine) buffalo.Handler {
	const op errors.Op = "download.VersionZipHandler"

	return func(c buffalo.Context) error {
		sp := buffet.SpanFromContext(c).SetOperationName("versionZipHandler")
		defer sp.Finish()
		mod, ver, err := getModuleParams(op, c)
		if err != nil {
			lggr.SystemErr(err)
			return c.Render(errors.Kind(err), nil)
		}
		zip, err := dp.Zip(c, mod, ver)
		if err != nil {
			lggr.SystemErr(err)
			return c.Render(errors.Kind(err), nil)
		}
		defer zip.Close()

		// Calling c.Response().Write will write the header directly
		// and we would get a 0 status in the buffalo logs.
		c.Render(200, nil)
		_, err = io.Copy(c.Response(), zip)
		if err != nil {
			lggr.SystemErr(errors.E(op, errors.M(mod), errors.V(ver), err))
		}

		return nil
	}
}
