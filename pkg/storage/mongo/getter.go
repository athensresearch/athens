package mongo

import (
	"bytes"
	"context"
	"io/ioutil"
	"strings"

	"github.com/globalsign/mgo/bson"
	"github.com/gomods/athens/pkg/storage"
	opentracing "github.com/opentracing/opentracing-go"
)

// Get a specific version of a module
func (s *ModuleStore) Get(module, vsn string) (*storage.Version, error) {
	sp, _ := opentracing.StartSpanFromContext(context.TODO(), "storage.mongo.Get")
	defer sp.Finish()
	c := s.s.DB(s.d).C(s.c)
	result := &storage.Module{}
	err := c.Find(bson.M{"module": module, "version": vsn}).One(result)
	if err != nil {
		if strings.Contains(err.Error(), "not found") {
			err = storage.ErrVersionNotFound{Module: module, Version: vsn}
		}
		return nil, err
	}
	return &storage.Version{
		Mod:  result.Mod,
		Zip:  ioutil.NopCloser(bytes.NewReader(result.Zip)),
		Info: result.Info,
	}, nil
}
