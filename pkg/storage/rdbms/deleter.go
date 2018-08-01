package rdbms

import (
	"context"

	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/storage/rdbms/models"
	opentracing "github.com/opentracing/opentracing-go"
)

// Delete removes a specific version of a module.
func (r *ModuleStore) Delete(ctx context.Context, module, version string) error {
	const op errors.Op = "rdbms.Delete"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.rdbms.Delete")
	defer sp.Finish()
	if !r.Exists(ctx, module, version) {
		return errors.E(op, errors.M(module), errors.V(version), errors.KindNotFound)
	}
	result := &models.Module{}
	query := r.conn.Where("module = ?", module).Where("version = ?", version)
	if err := query.First(result); err != nil {
		return errors.E(op, err)
	}
	return r.conn.Destroy(result)
}
