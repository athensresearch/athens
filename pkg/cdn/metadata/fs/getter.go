package fs

import (
	"encoding/json"
	"path/filepath"

	"github.com/gomods/athens/pkg/cdn/metadata"
)

// Get retrieves the cdn base URL for a module
func (s *fsStore) Get(module string) (string, error) {
	p := filepath.Join(s.rootDir, module, metadataFileName)
	f, err := s.filesystem.Open(p)
	if err != nil {
		return "", err
	}
	defer f.Close()
	dec := json.NewDecoder(f)
	md := &metadata.CDNMetadataEntry{}
	err = dec.Decode(md)
	if err != nil {
		return "", err
	}
	return md.RedirectURL, nil
}
