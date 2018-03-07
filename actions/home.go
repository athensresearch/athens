package actions

import (
	"github.com/gobuffalo/buffalo"
)

func homeHandler(c buffalo.Context) error {
	c.Flash().Add("info", "Hello")
	return c.Render(200, r.HTML("index.html"))
}
