package actions

import (
	"bytes"
	"net/http"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/paths"
	"github.com/gomods/athens/pkg/payloads"
	"github.com/gomods/athens/pkg/storage"
	"github.com/pkg/errors"
)

func uploadHandler(store storage.Saver) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		sp := buffet.SpanFromContext(c)
		sp.SetOperationName("uploadHandler")
		mod, err := paths.GetModule(c)
		if err != nil {
			return errors.WithStack(err)
		}
		version := c.Param("version")
		payload := new(payloads.Upload)
		if c.Bind(payload); err != nil {
			return errors.WithStack(err)
		}
		saveErr := store.Save(c, mod, version, payload.Module, bytes.NewReader(payload.Zip), payload.Info)
		if storage.IsVersionAlreadyExistsErr(saveErr) {
			return c.Error(http.StatusConflict, saveErr)
		} else if err != nil {
			return errors.WithStack(err)
		}
		return c.Render(http.StatusOK, proxy.JSON(nil))
	}
}
