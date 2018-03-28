package actions

import (
	"github.com/gobuffalo/buffalo/render"
	"github.com/gobuffalo/packr"
)

var olympus *render.Engine
var assetsBox = packr.NewBox("../public")

func init() {
	olympus = render.New(render.Options{
		// HTML layout to be used for all HTML requests:
		HTMLLayout:       "application.html",
		JavaScriptLayout: "application.js",

		// Box containing all of the templates:
		TemplatesBox: packr.NewBox("../templates/olympus"),
		AssetsBox:    assetsBox,

		// Add template helpers here:
		Helpers: render.Helpers{},
	})
}
