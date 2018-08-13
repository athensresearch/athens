package mongo

import (
	"github.com/globalsign/mgo/bson"
	"github.com/gomods/athens/pkg/cdn/metadata"
	"github.com/gomods/athens/pkg/errors"
)

// Get retrieves the cdn base URL for a module
func (s *MetadataStore) Get(module string) (string, error) {
	const op errors.Op = "mongoCDN.Get"
	coll := s.session.DB(s.db).C(s.col)
	params := bson.M{"module": module}
	entry := metadata.CDNMetadataEntry{}
	err := coll.Find(params).One(&entry)
	if err != nil {
		return "", errors.E(op, err)
	}
	return entry.RedirectURL, nil
}
