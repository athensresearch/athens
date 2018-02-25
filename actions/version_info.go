package actions

import (
	"net/http"

	"github.com/arschles/vgoprox/pkg/storage"
	"github.com/gobuffalo/buffalo"
)

func versionInfoHandler(getter storage.Getter) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		params, err := getAllPathParams(c)
		if err != nil {
			return err
		}
		version, err := getter.Get(params.baseURL, params.module, params.version)
		if err != nil {
			return err
		}
		return c.Render(http.StatusOK, r.JSON(version.RevInfo))
	}
}
