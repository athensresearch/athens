package rdbms

import (
	"context"
	"io"
	"io/ioutil"

	"github.com/gomods/athens/pkg/storage/rdbms/models"
	opentracing "github.com/opentracing/opentracing-go"
)

// Save stores a module in rdbms storage.
func (r *ModuleStore) Save(ctx context.Context, module, version string, mod []byte, zip io.Reader, info []byte) error {
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.rdbms.Save")
	defer sp.Finish()
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
