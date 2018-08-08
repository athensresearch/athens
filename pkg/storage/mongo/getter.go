package mongo

import (
	"context"
	"io"
	"strings"

	"github.com/globalsign/mgo/bson"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/storage"
	opentracing "github.com/opentracing/opentracing-go"
)

// Info implements storage.Getter
func (s *ModuleStore) Info(ctx context.Context, module, vsn string) ([]byte, error) {
	const op errors.Op = "mongo.Info"
	sp, _ := opentracing.StartSpanFromContext(ctx, "storage.mongo.Info")
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

	return result.Info, nil
}

// GoMod implements storage.Getter
func (s *ModuleStore) GoMod(ctx context.Context, module, vsn string) ([]byte, error) {
	const op errors.Op = "mongo.GoMod"
	sp, _ := opentracing.StartSpanFromContext(ctx, "storage.mongo.GoMod")
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

	return result.Mod, nil
}

// Zip implements storage.Getter
func (s *ModuleStore) Zip(ctx context.Context, module, vsn string) (io.ReadCloser, error) {
	const op errors.Op = "mongo.Zip"
	sp, _ := opentracing.StartSpanFromContext(ctx, "storage.mongo.Zip")
	defer sp.Finish()

	zipName := s.gridFileName(module, vsn)
	fs := s.s.DB(s.d).GridFS("fs")
	f, err := fs.Open(zipName)
	if err != nil {
		return nil, errors.E(op, err)
	}

	return f, nil
}
