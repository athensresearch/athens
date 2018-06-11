package mongo

import (
	"github.com/globalsign/mgo/bson"
)

// Exists checks for a specific version of a module
func (s *ModuleStore) Exists(module, vsn string) bool {
	c := s.s.DB(s.d).C(s.c)
	count, err := c.Find(bson.M{"module": module, "version": vsn}).Count()
	return err == nil && count > 0
}
