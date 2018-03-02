package actions

import (
	"fmt"
	"strings"

	"github.com/gobuffalo/buffalo"
)

func GoGet() buffalo.MiddlewareFunc {
	return func(next buffalo.Handler) buffalo.Handler {
		return func(c buffalo.Context) error {
			if strings.Contains(c.Request().URL.Query().Get("go-get"), "1") {
				return goGetMeta(c)
			}
			return next(c)
		}
	}
}

// stubbed for now, look up package stuff
func goGetMeta(c buffalo.Context) error {
	sp, err := getStandardParams(c)
	if err != nil {
		return err
	}
	meta := fmt.Sprintf("<!DOCTYPE html><meta name='go-import' content='%s/%s mod https://gomods.io/%s/%s", sp.baseURL, sp.module, sp.baseURL, sp.module)
	_, err = c.Response().Write([]byte(meta))
	return err
}
