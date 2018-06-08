package fs

import (
	"github.com/gomods/athens/pkg/cdn/metadata"
	"github.com/spf13/afero"
)

const metadataFileName = "cdn_metadata.json"

type fsStore struct {
	rootDir    string
	filesystem afero.Fs
}

// NewStorage creates a new metadata storage backed by file system
func NewStorage(fs afero.Fs, rootDir string) metadata.Storage {
	return &fsStore{rootDir: rootDir, filesystem: fs}
}
