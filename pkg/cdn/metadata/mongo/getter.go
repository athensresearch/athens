package mongo

import (
	"github.com/globalsign/mgo/bson"
	"github.com/gomods/athens/pkg/cdn/metadata"
)

// Get retrieves the cdn base URL for a module
func (s *MetadataStore) Get(module string) (string, error) {
	coll := s.session.DB(s.db).C(s.col)
	params := bson.M{"module": module}
	entry := metadata.CDNMetadataEntry{}
	err := coll.Find(params).One(&entry)
	return entry.RedirectURL, err
}
