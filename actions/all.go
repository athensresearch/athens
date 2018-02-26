package actions

import (
	"net/http"

	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/storage"
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
