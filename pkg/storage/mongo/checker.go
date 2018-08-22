package mongo

import (
	"context"

	"github.com/globalsign/mgo/bson"
	opentracing "github.com/opentracing/opentracing-go"
)

// Exists checks for a specific version of a module
func (s *ModuleStore) Exists(ctx context.Context, module, vsn string) (bool, error) {
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.mongo.Exists")
	defer sp.Finish()
	c := s.s.DB(s.d).C(s.c)
	count, err := c.Find(bson.M{"module": module, "version": vsn}).Count()
	return err == nil && count > 0, err
}
