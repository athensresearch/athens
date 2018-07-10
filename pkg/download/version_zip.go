package download

import (
	"fmt"
	"io"
	"net/http"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/paths"
	"github.com/gomods/athens/pkg/storage"
)

// PathVersionZip URL.
const PathVersionZip = "/{module:.+}/@v/{version}.zip"

// VersionZipHandler implements GET baseURL/module/@v/version.zip
func VersionZipHandler(getter storage.Getter) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		sp := buffet.SpanFromContext(c)
		sp.SetOperationName("versionZipHandler")
		params, err := paths.GetAllParams(c)
		if err != nil {
			return err
		}
		version, err := getter.Get(params.Module, params.Version)
		if storage.IsNotFoundError(err) {
			return c.Error(http.StatusNotFound, fmt.Errorf("%s@%s not found", params.Module, params.Version))
		} else if err != nil {
			return err
		}

		defer version.Zip.Close()

		c.Response().WriteHeader(http.StatusOK)

		_, err = io.Copy(c.Response(), version.Zip)
		return err
	}
}
