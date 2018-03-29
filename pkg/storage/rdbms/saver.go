package rdbms

import (
	"github.com/gomods/athens/pkg/storage/rdbms/models"
)

// Save stores a module in rdbms storage.
func (r *ModuleStore) Save(module, version string, mod, zip []byte) error {
	m := &models.Module{
		Module:  module,
		Version: version,
		Mod:     mod,
		Zip:     zip,
	}

	return r.conn.Create(m)
}
