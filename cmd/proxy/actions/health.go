package actions

import (
	"github.com/gobuffalo/buffalo"
)

func healthHandler(c buffalo.Context) error {
	return c.Render(200, nil)
}
