package mongo

import (
	"context"
	"io"
	"io/ioutil"

	"github.com/gomods/athens/pkg/storage"
	opentracing "github.com/opentracing/opentracing-go"
)

// Save stores a module in mongo storage.
func (s *ModuleStore) Save(ctx context.Context, module, version string, mod []byte, zip io.Reader, info []byte) error {
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.mongo.Save")
	defer sp.Finish()
	zipBytes, err := ioutil.ReadAll(zip)
	if err != nil {
		return err
	}
	m := &storage.Module{
		Module:  module,
		Version: version,
		Mod:     mod,
		Zip:     zipBytes,
		Info:    info,
	}

	c := s.s.DB(s.d).C(s.c)
	return c.Insert(m)
}
