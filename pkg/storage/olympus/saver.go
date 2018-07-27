package olympus

import (
	"context"
	"io"

	"github.com/opentracing/opentracing-go"
)

// Save stores a module in olympus.
// This actually does not store anything just reports cache miss
func (s *ModuleStore) Save(ctx context.Context, module, version string, _ []byte, _ io.ReadSeeker, _ []byte) error {
	// dummy implementation so Olympus Store can be used everywhere as Backend iface
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.olympus.Save")
	defer sp.Finish()
	return nil
}
