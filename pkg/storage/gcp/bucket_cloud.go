package gcp

import (
	"context"
	"io"

	"cloud.google.com/go/storage"
	"github.com/gomods/athens/pkg/errors"
	"google.golang.org/api/iterator"
)

// gcpBucket implements the (./pkg/storage/gcp).Bucket interface
type gcpBucket struct {
	*storage.BucketHandle
}

func (b *gcpBucket) Delete(ctx context.Context, path string) error {
	const op errors.Op = "gcpBucket.Delete"
	err := b.Object(path).Delete(ctx)
	if err != nil {
		return errors.E(op, err)
	}

	return nil
}

func (b *gcpBucket) Open(ctx context.Context, path string) (io.ReadCloser, error) {
	const op errors.Op = "gcpBucket.Open"
	rc, err := b.Object(path).NewReader(ctx)
	if err != nil {
		return rc, errors.E(op, err, errors.M(path))
	}
	return rc, nil
}

func (b *gcpBucket) Write(ctx context.Context, path string) io.WriteCloser {
	return b.Object(path).NewWriter(ctx)
}

func (b *gcpBucket) List(ctx context.Context, prefix string) ([]string, error) {
	const op errors.Op = "gcpBucket.List"
	it := b.Objects(ctx, &storage.Query{Prefix: prefix})

	res := []string{}
	for {
		attrs, err := it.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			return nil, errors.E(op, err)
		}
		res = append(res, attrs.Name)
	}

	return res, nil
}

func (b *gcpBucket) Exists(ctx context.Context, path string) bool {
	_, err := b.Object(path).Attrs(ctx)
	if err == nil {
		return true
	} else if err == storage.ErrObjectNotExist {
		return false
	}

	// TODO: this means that something errored out and we should return false, err
	return false
}
