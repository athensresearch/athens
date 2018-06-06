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
	m := &metadata.CDNMetadataEntry{
		ID:          id,
		Module:      module,
		RedirectURL: redirectURL,
		CreatedAt:   time.Now().UTC(),
		UpdatedAt:   time.Now().UTC(),
	}

	c := s.session.DB(s.db).C(s.col)
	return c.Insert(m)
}
