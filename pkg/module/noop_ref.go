package module

import (
	"fmt"

	"github.com/gomods/athens/pkg/storage"
)

type noopRef struct{}

func (n noopRef) Read() (*storage.Version, error) {
	return nil, fmt.Errorf("noop ref doesn't have a storage.Version")
}
