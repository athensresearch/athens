package mongo

import (
	"context"

	"github.com/globalsign/mgo/bson"
	"github.com/gomods/athens/pkg/errors"
	opentracing "github.com/opentracing/opentracing-go"
)

// Exists checks for a specific version of a module
func (s *ModuleStore) Exists(ctx context.Context, module, vsn string) (bool, error) {
	var op errors.Op = "storage.mongo.Exists"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.mongo.Exists")
	defer sp.Finish()
	c := s.s.DB(s.d).C(s.c)
	count, err := c.Find(bson.M{"module": module, "version": vsn}).Count()
	if err != nil {
		return false, errors.E(op, errors.M(module), errors.V(vsn), err)
	}
	return count > 0, nil
}
