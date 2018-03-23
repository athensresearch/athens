package actions

import (
	"fmt"
	"net/http"

	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/storage"
)

func versionModuleHandler(getter storage.Getter) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		params, err := getAllPathParams(c)
		if err != nil {
			return err
		}

		version, err := getter.Get(params.module, params.version)
		if storage.IsNotFoundError(err) {
			return c.Error(http.StatusNotFound, fmt.Errorf("%s@%s not found", params.module, params.version))
		} else if err != nil {
			return err
		}

		c.Response().WriteHeader(http.StatusOK)
		_, err = c.Response().Write(version.Mod)
		return err
	}
}
