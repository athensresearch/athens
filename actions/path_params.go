package actions

import (
	"fmt"

	"github.com/gobuffalo/buffalo"
)

type standardPathParams struct {
	baseURL string
	module  string
}

func getStandardParams(c buffalo.Context) (*standardPathParams, error) {
	baseURL := c.Param("base_url")
	module := c.Param("module")
	if baseURL == "" {
		return nil, fmt.Errorf("baseURL missing")
	}
	if module == "" {
		return nil, fmt.Errorf("module missing")
	}
	return &standardPathParams{baseURL: baseURL, module: module}, nil
}

type allPathParams struct {
	*standardPathParams
	version string
}

func getAllPathParams(c buffalo.Context) (*allPathParams, error) {
	stdParams, err := getStandardParams(c)
	if err != nil {
		return nil, err
	}
	version := c.Param("version")
	if version == "" {
		return nil, fmt.Errorf("version not found")
	}
	return &allPathParams{standardPathParams: stdParams, version: version}, nil
}
