package actions

import (
	"net/http"

	"github.com/arschles/vgoprox/pkg/storage"
	"github.com/gobuffalo/buffalo"
)

func allHandler(lister storage.Lister) func(buffalo.Context) error {
	return func(c buffalo.Context) error {
		revInfos, err := lister.All()
		if err != nil {
			return err
		}
		return c.Render(http.StatusOK, r.JSON(&revInfos))
	}
}
