package actions

import (
	"github.com/gobuffalo/buffalo/render"
	"github.com/gobuffalo/packr"
)

var proxy *render.Engine
var assetsBox = packr.NewBox("../public")
