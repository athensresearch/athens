package download

import (
	"encoding/json"
	"net/http"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/render"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/log"
	"github.com/gomods/athens/pkg/storage"
)

// PathVersionInfo URL.
const PathVersionInfo = "/{module:.+}/@v/{version}.info"

// VersionInfoHandler implements GET baseURL/module/@v/version.info
func VersionInfoHandler(dp Protocol, lggr *log.Logger, eng *render.Engine) buffalo.Handler {
	const op errors.Op = "download.versionInfoHandler"
	return func(c buffalo.Context) error {
		sp := buffet.SpanFromContext(c).SetOperationName("versionInfoHandler")
		defer sp.Finish()
		mod, ver, verInfo, err := getModuleVersion(c, lggr, dp)
		if err != nil {
			err := errors.E(op, errors.M(mod), errors.V(ver), err)
			lggr.SystemErr(err)
			return c.Render(errors.Kind(err), nil)
		}
		verInfo.Zip.Close()
		var revInfo storage.RevInfo
		if err := json.Unmarshal(verInfo.Info, &revInfo); err != nil {
			return err
		}
		return c.Render(http.StatusOK, eng.JSON(revInfo))
	}
}
