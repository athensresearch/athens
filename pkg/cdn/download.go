package cdn

import (
	"github.com/gomods/athens/pkg/storage"
)

// Download downloads the module/version from url. Returns a storage.Version
// representing the downloaded module/version or a non-nil error if something
// went wrong
func Download(url, module, version string) (*storage.Version, error) {
	// TODO
	return nil, nil
}
