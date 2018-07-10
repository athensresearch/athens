package download

import (
	"fmt"
	"net/http"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/paths"
	"github.com/gomods/athens/pkg/storage"
)

// PathVersionModule URL.
const PathVersionModule = "/{module:.+}/@v/{version}.mod"

// VersionModuleHandler implements GET baseURL/module/@v/version.mod
func VersionModuleHandler(getter storage.Getter) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		sp := buffet.SpanFromContext(c)
		sp.SetOperationName("versionModuleHandler")
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

		c.Response().WriteHeader(http.StatusOK)
		_, err = c.Response().Write(version.Mod)
		return err
	}
}
