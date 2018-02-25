package actions

import (
	"net/http"
	"strings"

	"github.com/arschles/vgoprox/pkg/storage"
	"github.com/gobuffalo/buffalo"
	errs "github.com/pkg/errors"
)

func listHandler(lister storage.Lister) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		params, err := getStandardParams(c)
		if err != nil {
			return err
		}
		versions, err := lister.List(params.baseURL, params.module)
		if storage.IsNotFoundError(err) {
			return c.Error(http.StatusNotFound, err)
		} else if err != nil {
			return errs.WithStack(err)
		}
		return c.Render(http.StatusOK, r.String(strings.Join(versions, "\n")))
	}
}
