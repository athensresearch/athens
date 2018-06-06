package actions

import (
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/paths"
	"github.com/gomods/athens/pkg/storage"
)

func versionInfoHandler(getter storage.Getter) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
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

		var revInfo storage.RevInfo
		err = json.Unmarshal(version.Info, &revInfo)
		if err != nil {
			return err
		}

		return c.Render(http.StatusOK, proxy.JSON(revInfo))
	}
}
