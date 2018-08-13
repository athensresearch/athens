package mongo

import (
	"time"

	"github.com/gobuffalo/uuid"
	"github.com/gomods/athens/pkg/cdn/metadata"
	"github.com/gomods/athens/pkg/errors"
)

// Save stores a module in mongo storage.
func (s *MetadataStore) Save(module, redirectURL string) error {
	const op errors.Op = "mongoCDN.Save"
	id, err := uuid.NewV4()
	if err != nil {
		return errors.E(op, err)
	}
	t := time.Now().UTC()
	m := &metadata.CDNMetadataEntry{
		ID:          id,
		Module:      module,
		RedirectURL: redirectURL,
		CreatedAt:   t,
		UpdatedAt:   t,
	}

	c := s.session.DB(s.db).C(s.col)
	err = c.Insert(m)
	if err != nil {
		return errors.E(op, err)
	}
	return nil
}
