package mongo

import (
	"context"
	"strings"

	"github.com/globalsign/mgo/bson"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/storage"
	opentracing "github.com/opentracing/opentracing-go"
)

// Get a specific version of a module
func (s *ModuleStore) Get(ctx context.Context, module, vsn string) (*storage.Version, error) {
	const op errors.Op = "mongo.Get"
	sp, _ := opentracing.StartSpanFromContext(ctx, "storage.mongo.Get")
	defer sp.Finish()
	c := s.s.DB(s.d).C(s.c)
	result := &storage.Module{}
	err := c.Find(bson.M{"module": module, "version": vsn}).One(result)
	if err != nil {
		if strings.Contains(err.Error(), "not found") {
			err = errors.E(op, errors.M(module), errors.V(vsn), errors.KindNotFound)
		}
		return nil, err
	}

	zipName := s.gridFileName(module, vsn)
	fs := s.s.DB(s.d).GridFS("fs")
	f, err := fs.Open(zipName)
	if err != nil {
		return nil, errors.E(op, err)
	}

	return &storage.Version{
		Mod:  result.Mod,
		Zip:  f,
		Info: result.Info,
	}, nil
}
