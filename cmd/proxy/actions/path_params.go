package actions

import (
	"fmt"

	"github.com/gobuffalo/buffalo"
)

func getModule(c buffalo.Context) (string, error) {
	module := c.Param("module")
	if module == "" {
		return "", fmt.Errorf("module missing")
	}
	return module, nil
}

type allPathParams struct {
	module  string
	version string
}

func getAllPathParams(c buffalo.Context) (*allPathParams, error) {
	mod, err := getModule(c)
	if err != nil {
		return nil, err
	}
	version := c.Param("version")
	if version == "" {
		return nil, fmt.Errorf("version not found")
	}
	return &allPathParams{module: mod, version: version}, nil
}
