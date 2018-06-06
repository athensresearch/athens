package paths

import (
	"fmt"

	"github.com/gobuffalo/buffalo"
)

// GetModule gets the module from the path of a ?go-get=1 request
func GetModule(c buffalo.Context) (string, error) {
	module := c.Param("module")
	if module == "" {
		return "", fmt.Errorf("module missing")
	}
	return module, nil
}

// AllPathParams holds the module and version in the path of a ?go-get=1
// request
type AllPathParams struct {
	Module  string
	Version string
}

// GetAllParams fetches the path patams from c and returns them
func GetAllParams(c buffalo.Context) (*AllPathParams, error) {
	mod, err := GetModule(c)
	if err != nil {
		return nil, err
	}
	version := c.Param("version")
	if version == "" {
		return nil, fmt.Errorf("version not found")
	}
	return &AllPathParams{Module: mod, Version: version}, nil
}
