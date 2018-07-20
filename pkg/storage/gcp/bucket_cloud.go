package gcp

import (
	"context"
	"fmt"
	"io"

	"cloud.google.com/go/storage"
	"google.golang.org/api/iterator"
)

// gcpBucket implements the (./pkg/storage/gcp).Bucket interface
type gcpBucket struct {
	*storage.BucketHandle
}

func (b *gcpBucket) Delete(ctx context.Context, path string) error {
	return b.Object(path).Delete(ctx)
}

func (b *gcpBucket) Open(ctx context.Context, path string) (io.ReadCloser, error) {
	return b.Object(path).NewReader(ctx)
}

func (b *gcpBucket) Write(ctx context.Context, path string) io.WriteCloser {
	return b.Object(path).NewWriter(ctx)
}

func (b *gcpBucket) List(ctx context.Context, prefix string) ([]string, error) {
	it := b.Objects(ctx, &storage.Query{Prefix: prefix})

	res := []string{}
	for {
		attrs, err := it.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			return nil, fmt.Errorf("could not iterate over query: %s", err)
		}
		res = append(res, attrs.Name)
	}

	return res, nil
}

func (b *gcpBucket) Exists(ctx context.Context, path string) bool {
	_, err := b.Object(path).Attrs(ctx)
	return err == nil
}
