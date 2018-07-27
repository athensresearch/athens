package mongo

import (
	"context"

	"github.com/globalsign/mgo/bson"
	"github.com/gomods/athens/pkg/storage"
	opentracing "github.com/opentracing/opentracing-go"
)

// Delete removes a specific version of a module
func (s *ModuleStore) Delete(ctx context.Context, module, version string) error {
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.mongo.Delete")
	defer sp.Finish()
	if !s.Exists(ctx, module, version) {
		return storage.ErrVersionNotFound{
			Module:  module,
			Version: version,
		}
	}
	c := s.s.DB(s.d).C(s.c)
	return c.Remove(bson.M{"module": module, "version": version})
}
