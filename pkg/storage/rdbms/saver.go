package rdbms

import (
	"context"
	"io"
	"io/ioutil"

	"github.com/gomods/athens/pkg/storage/rdbms/models"
)

// Save stores a module in rdbms storage.
func (r *ModuleStore) Save(_ context.Context, module, version string, mod []byte, zip io.Reader, info []byte) error {
	zipBytes, err := ioutil.ReadAll(zip)
	if err != nil {
		return err
	}
	m := &models.Module{
		Module:  module,
		Version: version,
		Mod:     mod,
		Zip:     zipBytes,
		Info:    info,
	}

	return r.conn.Create(m)
}
