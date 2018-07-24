package gcp

import (
	"bytes"
	"io/ioutil"
	"testing"
	"time"

	athensStorage "github.com/gomods/athens/pkg/storage"
)

func (g *GcpTests) TestNewWithCredentials() {
	r := g.Require()
	store, err := NewWithCredentials(g.context, g.options)
	r.NoError(err)
	r.NotNil(store.bucket)
}

func (g *GcpTests) TestSaveGetListExistsRoundTrip() {
	r := g.Require()
	store, err := NewWithCredentials(g.context, g.options)
	r.NoError(err)

	g.T().Run("Save to storage", func(t *testing.T) {
		err = store.Save(g.context, g.module, g.version, mod, bytes.NewReader(zip), info)
		r.NoError(err)
	})

	g.T().Run("Get from storage", func(t *testing.T) {
		version, err := store.Get(g.module, g.version)
		r.NoError(err)
		defer version.Zip.Close()

		r.Equal(mod, version.Mod)
		r.Equal(info, version.Info)

		gotZip, err := ioutil.ReadAll(version.Zip)
		r.NoError(version.Zip.Close())
		r.NoError(err)
		r.Equal(zip, gotZip)
	})

	g.T().Run("List module versions", func(t *testing.T) {
		versionList, err := store.List(g.context, g.module)
		r.NoError(err)
		r.Equal(1, len(versionList))
		r.Equal(g.version, versionList[0])
	})

	g.T().Run("Module exists", func(t *testing.T) {
		r.Equal(true, store.Exists(g.module, g.version))
	})
}

func (g *GcpTests) TestDeleter() {
	r := g.Require()
	store, err := NewWithCredentials(g.context, g.options)
	r.NoError(err)

	version := "delete" + time.Now().String()
	err = store.Save(g.context, g.module, version, mod, bytes.NewReader(zip), info)
	r.NoError(err)

	err = store.Delete(g.module, version)
	r.NoError(err)

	exists := store.Exists(g.module, version)
	r.Equal(false, exists)
}

func (g *GcpTests) TestNotFounds() {
	r := g.Require()
	store, err := NewWithCredentials(g.context, g.options)
	r.NoError(err)

	g.T().Run("Get module version not found", func(t *testing.T) {
		_, err = store.Get("never", "there")
		versionNotFoundErr := athensStorage.ErrVersionNotFound{Module: "never", Version: "there"}
		r.EqualError(versionNotFoundErr, err.Error())
	})

	g.T().Run("Exists module version not found", func(t *testing.T) {
		r.Equal(false, store.Exists("never", "there"))
	})

	g.T().Run("List not found", func(t *testing.T) {
		_, err = store.List(g.context, "nothing/to/see/here")
		modNotFoundErr := athensStorage.ErrNotFound{Module: "nothing/to/see/here"}
		r.EqualError(modNotFoundErr, err.Error())
	})
}
