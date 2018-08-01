package gcp

import (
	"bytes"
	"context"

	"io/ioutil"
	"testing"
	"time"

	"github.com/gomods/athens/pkg/errors"
)

func (g *GcpTests) TestSaveGetListExistsRoundTrip() {
	r := g.Require()

	g.T().Run("Save to storage", func(t *testing.T) {
		err := g.store.Save(g.context, g.module, g.version, mod, bytes.NewReader(zip), info)
		r.NoError(err)
	})

	g.T().Run("Get from storage", func(t *testing.T) {
		version, err := g.store.Get(context.Background(), g.module, g.version)
		r.NoError(err)
		r.Equal(mod, version.Mod)
		r.Equal(info, version.Info)

		gotZip, err := ioutil.ReadAll(version.Zip)
		r.NoError(version.Zip.Close())
		r.NoError(err)
		r.Equal(zip, gotZip)
	})

	g.T().Run("List module versions", func(t *testing.T) {
		versionList, err := g.store.List(g.context, g.module)
		r.NoError(err)
		r.Equal(1, len(versionList))
		r.Equal(g.version, versionList[0])
	})

	g.T().Run("Module exists", func(t *testing.T) {
		r.Equal(true, g.store.Exists(g.context, g.module, g.version))
	})

	g.T().Run("Resources closed", func(t *testing.T) {
		r.Equal(true, g.bucket.ReadClosed())
		r.Equal(true, g.bucket.WriteClosed())
	})
}

func (g *GcpTests) TestDeleter() {
	r := g.Require()

	version := "delete" + time.Now().String()
	err := g.store.Save(g.context, g.module, version, mod, bytes.NewReader(zip), info)
	r.NoError(err)

	err = g.store.Delete(g.context, g.module, version)
	r.NoError(err)

	exists := g.store.Exists(g.context, g.module, version)
	r.Equal(false, exists)
}

func (g *GcpTests) TestNotFounds() {
	r := g.Require()

	g.T().Run("Get module version not found", func(t *testing.T) {
		_, err := g.store.Get(context.Background(), "never", "there")
		r.True(errors.IsNotFoundErr(err))
	})

	g.T().Run("Exists module version not found", func(t *testing.T) {
		r.Equal(false, g.store.Exists(g.context, "never", "there"))
	})

	g.T().Run("List not found", func(t *testing.T) {
		_, err := g.store.List(g.context, "nothing/to/see/here")
		r.True(errors.IsNotFoundErr(err))
	})
}
