package mongo

import (
	"github.com/globalsign/mgo/bson"
)

// Delete removes a specific version of a module
func (s *ModuleStore) Delete(module, vsn string) error {
	c := s.s.DB(s.d).C(s.c)
	return c.Remove(bson.M{"module": module, "version": vsn})
}
