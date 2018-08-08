package rdbms

import (
	"bytes"
	"context"
	"database/sql"
	"io"
	"io/ioutil"

	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/storage"
	"github.com/gomods/athens/pkg/storage/rdbms/models"
	opentracing "github.com/opentracing/opentracing-go"
	pkgerrors "github.com/pkg/errors"
)

// Get a specific version of a module
func (r *ModuleStore) Get(ctx context.Context, module, vsn string) (*storage.Version, error) {
	const op errors.Op = "rdbms.Get"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.rdbms.Get")
	defer sp.Finish()
	result := models.Module{}
	query := r.conn.Where("module = ?", module).Where("version = ?", vsn)
	if err := query.First(&result); err != nil {
		if pkgerrors.Cause(err) == sql.ErrNoRows { // can we just say err == sql.ErrNoRows?
			return nil, errors.E(op, errors.M(module), errors.V(vsn), errors.KindNotFound)
		}
		return nil, errors.E(op, err)
	}
	return &storage.Version{
		Mod:  result.Mod,
		Zip:  ioutil.NopCloser(bytes.NewReader(result.Zip)),
		Info: result.Info,
	}, nil
}

// Info will panic.
func (r *ModuleStore) Info(ctx context.Context, module, vsn string) ([]byte, error) {
	panic("")
}

// GoMod will panic.
func (r *ModuleStore) GoMod(ctx context.Context, module, vsn string) ([]byte, error) {
	panic("")
}

// Zip will panic.
func (r *ModuleStore) Zip(ctx context.Context, module, vsn string) (io.ReadCloser, error) {
	panic("")
}
