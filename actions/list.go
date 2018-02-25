package actions

import (
	"net/http"
	"strings"

	"github.com/arschles/vgoprox/pkg/storage/memory"
	"github.com/gobuffalo/buffalo"
	"github.com/pkg/errors"
)

var lister = &memory.Lister{}

func listHandler(c buffalo.Context) error {
	params, err := getStandardParams(c)
	if err != nil {
		return err
	}
	versions, err := lister.List(params.baseURL, params.module)
	if err != nil {
		return errors.WithStack(err)
	}
	return c.Render(http.StatusOK, r.String(strings.Join(versions, "\n")))
}
