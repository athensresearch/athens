package gcp

import (
	"context"
	"strings"

	"github.com/gomods/athens/pkg/storage"
)

// List implements the (./pkg/storage).Lister interface
// It returns a list of versions, if any, for a given module
func (s *Storage) List(module string) ([]string, error) {
	ctx := context.Background()
	paths, err := s.bucket.List(ctx, module)
	if err != nil {
		return nil, err
	}
	versions := extractVersions(paths)
	if len(versions) < 1 {
		return nil, storage.ErrNotFound{Module: module}
	}
	return versions, nil
}

func extractVersions(paths []string) []string {
	versions := []string{}
	for _, p := range paths {
		if strings.HasSuffix(p, ".info") {
			segments := strings.Split(p, "/")
			// version should be last segment w/ .info suffix
			last := segments[len(segments)-1]
			version := strings.TrimSuffix(last, ".info")
			versions = append(versions, version)
		}
	}
	return versions
}
