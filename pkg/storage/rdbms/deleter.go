package rdbms

import (
	"github.com/gomods/athens/pkg/storage/rdbms/models"
)

// Delete removes a specific version of a module
func (r *ModuleStore) Delete(module, vsn string) error {
	result := models.Module{}
	query := r.conn.Where("module = ?", module).Where("version = ?", vsn)
	if err := query.First(&result); err != nil {
		return err
	}
	return r.conn.Destroy(result)
}
