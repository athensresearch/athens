package gcp

import (
	"context"
	"fmt"
	"log"
	"strings"
	"time"

	"cloud.google.com/go/storage"
	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/config/env"
	"google.golang.org/api/option"
)

// Storage implements the Saver interface
// (./pkg/storage).Saver
type Storage struct {
	bucket *storage.BucketHandle
}

// New returns a new Storage instance authenticated using the provided
// ClientOptions. The bucket name to be used will be loaded from the
// environment variable ATHENS_STORAGE_GCP_BUCKET.
//
// The ClientOptions should provide permissions sufficient to create objects
// in google cloud storage for your project.
func New(ctx context.Context, cred option.ClientOption) (*Storage, error) {
	client, err := storage.NewClient(ctx, cred)
	if err != nil {
		return nil, fmt.Errorf("could not create new client: %s", err)
	}
	bucketname, err := env.GcpBucketName()
	if err != nil {
		return nil, err
	}
	bkt := client.Bucket(bucketname)
	return &Storage{bucket: bkt}, nil
}

// Save uploads the module .mod, .zip and .info files for a given version.
// It expects a context, which can be provided using context.Background
// from the standard library.
//
// Please note the following limitations which will eventually be configurable:
//
// All three uploads share a time out currently set to
// 300 seconds. After which all ongoing uploads will cancel.
//
// Uploaded files are publicly accessable in the storage bucket as per
// an ACL rule.
func (s *Storage) Save(ctx context.Context, module, version string, mod, zip, info []byte) error {
	errs := make(chan error, 3)
	// create a context that will time out after 300 seconds / 5 minutes
	ctxWT, cancelCTX := context.WithTimeout(ctx, 300*time.Second)
	defer cancelCTX()

	// dispatch go routine for each file to upload
	go upload(ctxWT, errs, s.bucket, module, version, "mod", mod)
	go upload(ctxWT, errs, s.bucket, module, version, "zip", zip)
	go upload(ctxWT, errs, s.bucket, module, version, "info", info)

	errsOut := make([]string, 0, 3)
	// wait for each routine above to send a value
	for count := 0; count < 3; count++ {
		err := <-errs
		if err != nil {
			errsOut = append(errsOut, err.Error())
		}
	}
	close(errs)

	// return concatenated error string if there is anything to report
	if len(errsOut) > 0 {
		return fmt.Errorf("one or more errors occured saving %s/@v/%s: %s", module, version, strings.Join(errsOut, ", "))
	}
	return nil
}

// upload waits for either writeToBucket to complete or the context expires
func upload(ctx context.Context, errs chan<- error, bkt *storage.BucketHandle, module, version, ext string, file []byte) {
	select {
	case errs <- writeToBucket(ctx, bkt, config.PackageVersionedName(module, version, ext), file):
		return
	case <-ctx.Done():
		errs <- fmt.Errorf("WARNING: context deadline exceeded during write of %s version %s", module, version)
	}
}

// writeToBucket performs the actual write to a gcp storage bucket
func writeToBucket(ctx context.Context, bkt *storage.BucketHandle, filename string, file []byte) error {
	wc := bkt.Object(filename).NewWriter(ctx)
	defer func(w *storage.Writer) {
		if err := w.Close(); err != nil {
			log.Printf("WARNING: failed to close storage object writer: %v", err)
		}
	}(wc)
	wc.ContentType = "application/octet-stream"
	// TODO: have this configurable to allow for mixed public/private modules
	wc.ACL = []storage.ACLRule{{Entity: storage.AllUsers, Role: storage.RoleReader}}
	if _, err := wc.Write(file); err != nil {
		return err
	}
	return nil
}
