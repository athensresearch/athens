package rdbms

import "github.com/gomods/athens/pkg/cdn/metadata"

// Get retrieves the cdn base URL for a module
func (s *MetadataStore) Get(module string) (string, error) {
	result := metadata.CDNMetadataEntry{}
	query := s.conn.Where("module = ?", module)
	err := query.First(&result)
	return result.RedirectURL, err
}
