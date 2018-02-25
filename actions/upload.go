package actions

import (
	"net/http"

	"github.com/arschles/vgoprox/pkg/payloads"
	"github.com/arschles/vgoprox/pkg/storage"
	"github.com/gobuffalo/buffalo"
	"github.com/pkg/errors"
)

func uploadHandler(store storage.Saver) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		stdParams, err := getStandardParams(c)
		if err != nil {
			return errors.WithStack(err)
		}
		version := c.Param("ver")
		payload := new(payloads.Upload)
		if c.Bind(payload); err != nil {
			return errors.WithStack(err)
		}
		if err := store.Save(
			stdParams.baseURL,
			stdParams.module,
			version,
			payload.Module,
			payload.Zip,
		); err != nil {
			return errors.WithStack(err)
		}
		return c.Render(http.StatusOK, r.JSON(nil))
	}
}
