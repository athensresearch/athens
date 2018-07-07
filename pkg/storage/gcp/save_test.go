package gcp

import (
	"bytes"
	"context"

	"cloud.google.com/go/storage"
	"github.com/gomods/athens/pkg/config"
	"google.golang.org/api/option"
)

func (g *GcpTests) TestNewStorage() {
	r := g.Require()
	store, err := New(g.context, g.options)
	r.NoError(err)
	r.NotNil(store.bucket)
}

func (g *GcpTests) TestSave() {
	r := g.Require()
	store, err := New(g.context, g.options)
	r.NoError(err)
	err = store.Save(g.context, g.module, g.version, mod, bytes.NewReader(zip), info)
	r.NoError(err)

	err = exists(g.context, g.options, g.bucket, g.module, g.version)
	r.NoError(err)
}

func exists(ctx context.Context, cred option.ClientOption, bucket, mod, ver string) error {
	client, err := storage.NewClient(ctx, cred)
	if err != nil {
		return err
	}
	bkt := client.Bucket(bucket)

	if _, err := bkt.Object(config.PackageVersionedName(mod, ver, "mod")).Attrs(ctx); err != nil {
		return err
	}
	if _, err := bkt.Object(config.PackageVersionedName(mod, ver, "info")).Attrs(ctx); err != nil {
		return err
	}
	if _, err := bkt.Object(config.PackageVersionedName(mod, ver, "zip")).Attrs(ctx); err != nil {
		return err
	}
	return nil
}
