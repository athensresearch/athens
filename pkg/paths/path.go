package paths

import (
	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/errors"
)

// GetModule gets the module from the path of a ?go-get=1 request
func GetModule(c buffalo.Context) (string, error) {
	const op errors.Op = "paths.GetModule"

	module := c.Param("module")
	if module == "" {
		return "", errors.E(op, "missing module parameter")
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
	const op errors.Op = "paths.GetAllParams"
	mod, err := GetModule(c)
	if err != nil {
		return nil, errors.E(op, err)
	}
	version := c.Param("version")
	if version == "" {
		return nil, errors.E(op, "missing version paramater")
	}

	return &AllPathParams{Module: mod, Version: version}, nil
}
