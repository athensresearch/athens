package actions

import (
	"time"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
)

func proxyHomeHandler(c buffalo.Context) error {
	return c.Render(200, proxy.HTML("index.html"))
}

func homeHandler(c buffalo.Context) error {
	slow(c)
	return c.Render(200, proxy.HTML("index.html"))
}

func slow(c buffalo.Context) {
	sp := buffet.ChildSpan("slow", c)
	defer sp.Finish()
	time.Sleep(1 * time.Millisecond)

}
