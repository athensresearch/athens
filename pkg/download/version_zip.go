package download

import (
	"fmt"
	"io"
	"net/http"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/render"
	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/log"
	"github.com/gomods/athens/pkg/paths"
	"github.com/gomods/athens/pkg/storage"
	"github.com/sirupsen/logrus"
)

// PathVersionZip URL.
const PathVersionZip = "/{module:.+}/@v/{version}.zip"

// VersionZipHandler implements GET baseURL/module/@v/version.zip
func VersionZipHandler(getter storage.Getter, eng *render.Engine, lggr *log.Logger) func(c buffalo.Context) error {
	const op errors.Op = "download.VersionZipHandler"

	return func(c buffalo.Context) error {
		sp := buffet.SpanFromContext(c)
		sp.SetOperationName("versionZipHandler")
		params, err := paths.GetAllParams(c)
		if err != nil {
			lggr.SystemErr(errors.E(op, err))
			c.Render(http.StatusInternalServerError, nil) // 500 because handler should not be called in the first place.
			return nil
		}

		version, err := getter.Get(params.Module, params.Version)
		if err != nil {
			lvl := logrus.ErrorLevel
			status := http.StatusInternalServerError
			msg := http.StatusText(status)
			// TODO: move this function to pkg/errors
			if storage.IsNotFoundError(err) {
				lvl = logrus.DebugLevel
				msg = fmt.Sprintf("%v not found", config.FmtModVer(params.Module, params.Version))
				status = http.StatusNotFound
			}

			lggr.SystemErr(errors.E(op, errors.M(params.Module), errors.V(params.Version), lvl, err))
			return c.Render(status, eng.JSON(msg))
		}
		defer version.Zip.Close()

		status := http.StatusOK

		_, err = io.Copy(c.Response(), version.Zip)
		if err != nil {
			status = http.StatusInternalServerError
			lggr.SystemErr(errors.E(op, errors.M(params.Module), errors.V(params.Version), err))
		}

		c.Response().WriteHeader(status)

		return nil
	}
}
