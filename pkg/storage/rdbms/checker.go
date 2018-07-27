package rdbms

import (
	"context"

	"github.com/gomods/athens/pkg/storage/rdbms/models"
	opentracing "github.com/opentracing/opentracing-go"
)

// Exists checks for a specific version of a module
func (r *ModuleStore) Exists(ctx context.Context, module, vsn string) bool {
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.rdbms.Exists")
	defer sp.Finish()
	result := models.Module{}
	query := r.conn.Where("module = ?", module).Where("version = ?", vsn)
	count, err := query.Count(&result)

	return err == nil && count > 0
}
