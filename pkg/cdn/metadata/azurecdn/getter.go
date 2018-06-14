package azurecdn

import (
	"github.com/gomods/athens/pkg/storage/azurecdn"
)

// Metadata implements the Getter interface for Azure CDN. It also composes
// a (github.com/gomods/athens/pkg/storages/azurecdn).Storage implementation
//
// for Azure CDN
type Metadata struct {
	azurecdn.Storage
}

// Get implements the Getter interface
func (m Metadata) Get(module string) (string, error) {
	return m.BaseURL().String(), nil
}
