package actions

import (
	"net/http"

	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/storage"
)

func feedHandler(s storage.Backend) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		if _, err := getSyncPoint(c); err != nil {
			return err
		}

		feed := make(map[string][]string)

		return c.Render(http.StatusOK, olympus.JSON(feed))
	}
}
