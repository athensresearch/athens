package mongo

import (
	"time"

	"github.com/gobuffalo/uuid"
	"github.com/gomods/athens/pkg/cdn/metadata"
)

// Save stores a module in mongo storage.
func (s *MetadataStore) Save(module, redirectURL string) error {
	id, err := uuid.NewV4()
	if err != nil {
		return err
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
	return c.Insert(m)
}
