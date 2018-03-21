package actions

import (
	"github.com/gobuffalo/buffalo"
)

func proxyHomeHandler(c buffalo.Context) error {
	return c.Render(200, proxy.HTML("index.html"))
}

func homeHandler(c buffalo.Context) error {
	return c.Render(200, registry.HTML("index.html"))
}
