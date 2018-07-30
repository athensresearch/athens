package rdbms

import (
	"bytes"
	"context"
	"database/sql"
	"io/ioutil"

	"github.com/gomods/athens/pkg/storage"
	"github.com/gomods/athens/pkg/storage/rdbms/models"
	opentracing "github.com/opentracing/opentracing-go"
	"github.com/pkg/errors"
)

// Get a specific version of a module
func (r *ModuleStore) Get(ctx context.Context, module, vsn string) (*storage.Version, error) {
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.rdbms.Get")
	defer sp.Finish()
	result := models.Module{}
	query := r.conn.Where("module = ?", module).Where("version = ?", vsn)
	if err := query.First(&result); err != nil {
		if errors.Cause(err) == sql.ErrNoRows {
			return nil, storage.ErrVersionNotFound{Module: module, Version: vsn}
		}
		return nil, err
	}
	return &storage.Version{
		Mod:  result.Mod,
		Zip:  ioutil.NopCloser(bytes.NewReader(result.Zip)),
		Info: result.Info,
	}, nil
}
