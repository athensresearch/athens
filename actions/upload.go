package actions

import (
	"net/http"

	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/payloads"
	"github.com/gomods/athens/pkg/storage"
	"github.com/pkg/errors"
)

func uploadHandler(store storage.Saver) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		mod, err := getModule(c)
		if err != nil {
			return errors.WithStack(err)
		}
		version := c.Param("version")
		payload := new(payloads.Upload)
		if c.Bind(payload); err != nil {
			return errors.WithStack(err)
		}
		saveErr := store.Save(mod, version, payload.Module, payload.Zip)
		if storage.IsVersionAlreadyExistsErr(saveErr) {
			return c.Error(http.StatusConflict, saveErr)
		} else if err != nil {
			return errors.WithStack(err)
		}
		return c.Render(http.StatusOK, proxy.JSON(nil))
	}
}
