package mongo

import (
	"context"

	"github.com/globalsign/mgo"
	"github.com/globalsign/mgo/bson"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/storage"
	opentracing "github.com/opentracing/opentracing-go"
)

// List lists all versions of a module
func (s *ModuleStore) List(ctx context.Context, module string) ([]string, error) {
	const op errors.Op = "mongo.List"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.mongo.List")
	defer sp.Finish()
	c := s.s.DB(s.d).C(s.c)
	result := make([]storage.Module, 0)
	err := c.Find(bson.M{"module": module}).All(&result)
	if err != nil {
		kind := errors.KindUnexpected
		if err == mgo.ErrNotFound {
			kind = errors.KindNotFound
		}
		return nil, errors.E(op, kind, errors.M(module), err)
	}

	versions := make([]string, len(result))
	for i, r := range result {
		versions[i] = r.Version
	}

	return versions, nil
}
