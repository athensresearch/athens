package actions

import (
	"net/http"
	"strings"

	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/paths"
	"github.com/gomods/athens/pkg/storage"
	errs "github.com/pkg/errors"
)

func listHandler(lister storage.Lister) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		mod, err := paths.GetModule(c)
		if err != nil {
			return err
		}
		versions, err := lister.List(mod)
		if storage.IsNotFoundError(err) {
			return c.Error(http.StatusNotFound, err)
		} else if err != nil {
			return errs.WithStack(err)
		}
		return c.Render(http.StatusOK, proxy.String(strings.Join(versions, "\n")))
	}
}
