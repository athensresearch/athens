package download

import (
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/render"
	"github.com/gomods/athens/pkg/paths"
	"github.com/gomods/athens/pkg/storage"
)

// PathVersionInfo URL.
const PathVersionInfo = "/{module:.+}/@v/{version}.info"

// VersionInfoHandler implements GET baseURL/module/@v/version.info
func VersionInfoHandler(getter storage.Getter, eng *render.Engine) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		sp := buffet.SpanFromContext(c)
		sp.SetOperationName("versionInfoHandler")
		params, err := paths.GetAllParams(c)
		if err != nil {
			return err
		}
		version, err := getter.Get(params.Module, params.Version)
		if storage.IsNotFoundError(err) {
			msg := fmt.Sprintf("%s@%s not found", params.Module, params.Version)
			return c.Render(http.StatusNotFound, eng.JSON(msg))
		} else if err != nil {
			return err
		}
		var revInfo storage.RevInfo
		err = json.Unmarshal(version.Info, &revInfo)
		if err != nil {
			return err
		}
		return c.Render(http.StatusOK, eng.JSON(revInfo))
	}
}
