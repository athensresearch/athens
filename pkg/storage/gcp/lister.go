package gcp

import (
	"context"
	"fmt"
	"strings"

	"cloud.google.com/go/storage"
	athensStorage "github.com/gomods/athens/pkg/storage"
	"google.golang.org/api/iterator"
)

// List implements the (./pkg/storage).Lister interface
// It returns a list of versions, if any, for a given module
func (s *Storage) List(module string) ([]string, error) {
	ctx := context.Background()
	// get object iterator for module matches
	it := s.bucket.Objects(ctx, &storage.Query{Prefix: module})

	versions := make([]string, 0, 10)
	for {
		attrs, err := it.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			return nil, fmt.Errorf("could not iterate over query: %s", err)
		}

		// kinda hacky looking at this time
		if strings.HasSuffix(attrs.Name, ".info") {
			segments := strings.Split(attrs.Name, "/")
			// version should be last segment w/ .info suffix
			last := segments[len(segments)-1]
			version := strings.TrimSuffix(last, ".info")
			versions = append(versions, version)
		}
	}

	if len(versions) == 0 {
		return nil, athensStorage.ErrNotFound{Module: module}
	}

	return versions, nil
}
