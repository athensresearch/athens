package mongo

import (
	"github.com/globalsign/mgo/bson"
	"github.com/gomods/athens/pkg/storage"
)

// Delete removes a specific version of a module
func (s *ModuleStore) Delete(module, version string) error {
	if !s.Exists(module, version) {
		return storage.ErrVersionNotFound{
			Module:  module,
			Version: version,
		}
	}
	c := s.s.DB(s.d).C(s.c)
	return c.Remove(bson.M{"module": module, "version": version})
}
