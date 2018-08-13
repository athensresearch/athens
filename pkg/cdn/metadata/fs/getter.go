package fs

import (
	"encoding/json"
	"path/filepath"

	"github.com/gomods/athens/pkg/cdn/metadata"
	"github.com/gomods/athens/pkg/errors"
)

// Get retrieves the cdn base URL for a module
func (s *fsStore) Get(module string) (string, error) {
	const op errors.Op = "fsCDN.Get"
	p := filepath.Join(s.rootDir, module, metadataFileName)
	f, err := s.filesystem.Open(p)
	if err != nil {
		return "", errors.E(op, err)
	}
	defer f.Close()
	dec := json.NewDecoder(f)
	md := &metadata.CDNMetadataEntry{}
	err = dec.Decode(md)
	if err != nil {
		return "", errors.E(op, err)
	}
	return md.RedirectURL, nil
}
