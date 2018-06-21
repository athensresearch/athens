package olympus

import (
	"context"
)

// Save stores a module in olympus.
// This actually does not store anything just reports cache miss
func (s *ModuleStore) Save(_ context.Context, module, version string, _, _, _ []byte) error {
	// dummy implementation so Olympus Store can be used everywhere as Backend iface
	return nil
}
