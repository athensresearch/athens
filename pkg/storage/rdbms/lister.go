package rdbms

import (
	"context"

	"github.com/gomods/athens/pkg/storage/rdbms/models"
	opentracing "github.com/opentracing/opentracing-go"
)

// List lists all versions of a module
func (r *ModuleStore) List(ctx context.Context, module string) ([]string, error) {
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.rdbms.List")
	defer sp.Finish()
	result := make([]models.Module, 0)
	err := r.conn.Where("module = ?", module).All(&result)
	if err != nil {
		return nil, err
	}

	versions := make([]string, len(result))
	for i := range result {
		versions[i] = result[i].Version
	}

	return versions, nil
}
