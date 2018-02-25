package actions

import (
	"github.com/gobuffalo/buffalo"
)

func homeHandler(c buffalo.Context) error {
	return c.Render(200, r.HTML("index.html"))
}
