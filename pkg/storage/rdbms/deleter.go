package rdbms

import (
	"context"

	"github.com/gomods/athens/pkg/storage"
	"github.com/gomods/athens/pkg/storage/rdbms/models"
	opentracing "github.com/opentracing/opentracing-go"
)

// Delete removes a specific version of a module.
func (r *ModuleStore) Delete(ctx context.Context, module, version string) error {
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.rdbms.Delete")
	defer sp.Finish()
	if !r.Exists(ctx, module, version) {
		return storage.ErrVersionNotFound{
			Module:  module,
			Version: version,
		}
	}
	result := &models.Module{}
	query := r.conn.Where("module = ?", module).Where("version = ?", version)
	if err := query.First(result); err != nil {
		return err
	}
	return r.conn.Destroy(result)
}
