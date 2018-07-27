// Package download provides buffalo handlers
// that implement vgo's Download Protocol.
// This is so that both Zeus and Olympus
// can share the same download protocol
// implementation.
package download

import (
	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/log"
	"github.com/gomods/athens/pkg/paths"
	"github.com/gomods/athens/pkg/storage"
	"github.com/pkg/errors"
)

func getModuleVersion(c buffalo.Context, lggr *log.Logger, dp Protocol) (string, string, *storage.Version, error) {
	params, err := paths.GetAllParams(c)
	if err != nil {
		lggr.SystemErr(err)
		panic(err)
	}

	versionInfo, err := dp.Version(c, params.Module, params.Version)
	if err != nil {
		return "", "", nil, errors.WithStack(err)
	}
	return params.Module, params.Version, versionInfo, nil
}
