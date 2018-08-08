package modulestorage

// The goal of this package is to provide common testing ground for all
// storage tests which can be extracted to achieve more DRY code.
// This package should contain black box tests such as Save-Get roundtrip,
// Error checking, etc.
// More detailed tests verifying internal behavior should be still implemented
// as part of specific storage packages.

import (
	"bytes"
	"context"
	"io/ioutil"
	"testing"
	"time"

	"github.com/gobuffalo/suite"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/storage"
	"github.com/gomods/athens/pkg/storage/fs"
	"github.com/gomods/athens/pkg/storage/mem"
	"github.com/gomods/athens/pkg/storage/minio"
	"github.com/gomods/athens/pkg/storage/mongo"
	// "github.com/gomods/athens/pkg/storage/rdbms"
)

type TestSuites struct {
	*suite.Model
	storages []storage.TestSuite
	module   string
	version  string
	mod      []byte
	zip      []byte
	info     []byte
}

func (d *TestSuites) SetupTest() {
	ra := d.Require()

	//
	fsTests, err := fs.NewTestSuite(d.Model)
	ra.NoError(err)
	d.storages = append(d.storages, fsTests)

	// mem
	memStore, err := mem.NewTestSuite(d.Model)
	ra.NoError(err)
	d.storages = append(d.storages, memStore)

	// minio
	minioStorage, err := minio.NewTestSuite(d.Model)
	ra.NoError(err)
	d.storages = append(d.storages, minioStorage)

	// mongo
	mongoStore, err := mongo.NewTestSuite(d.Model)
	ra.NoError(err)
	d.storages = append(d.storages, mongoStore)

	// rdbms
	// rdbmsStore, err := rdbms.NewTestSuite(d.Model)
	// d.Model.SetupTest()
	// d.storages = append(d.storages, rdbmsStore)

	d.module = "testmodule"
	d.version = "v1.0.0"
	d.mod = []byte("123")
	d.zip = []byte("456")
	d.info = []byte("789")

}

func TestModuleStorages(t *testing.T) {
	suite.Run(t, &TestSuites{Model: suite.NewModel()})
}

func (d *TestSuites) TestStorages() {
	for _, store := range d.storages {
		d.testNotFound(store)
		d.testGetSaveListRoundTrip(store)
		d.testList(store)
		d.testDelete(store)

		// TODO: more tests to come

		store.Cleanup()
	}
}

func (d *TestSuites) testNotFound(ts storage.TestSuite) {
	_, err := ts.Storage().Info(context.Background(), "some", "unknown")
	d.Require().Equal(true, errors.IsNotFoundErr(err), "Invalid error type for %s: %#v", ts.StorageHumanReadableName(), err)
}

func (d *TestSuites) testList(ts storage.TestSuite) {
	r := d.Require()
	hrn := ts.StorageHumanReadableName()
	versions := []string{"v1.1.0", "v1.2.0", "v1.3.0"}
	for _, version := range versions {
		r.NoError(ts.Storage().Save(context.Background(), d.module, version, d.mod, bytes.NewReader(d.zip), d.info), "Save for storage %s failed", hrn)
	}
	// append version from save-get roundtrip
	versions = append([]string{d.version}, versions...)
	retVersions, err := ts.Storage().List(context.Background(), d.module)
	r.NoError(err, hrn)
	r.Equal(versions, retVersions, hrn)
}

func (d *TestSuites) testGetSaveListRoundTrip(ts storage.TestSuite) {
	r := d.Require()
	hrn := ts.StorageHumanReadableName()
	ts.Storage().Save(context.Background(), d.module, d.version, d.mod, bytes.NewReader(d.zip), d.info)
	listedVersions, err := ts.Storage().List(context.Background(), d.module)
	r.NoError(err, hrn)
	r.Equal(1, len(listedVersions), hrn)
	retVersion := listedVersions[0]
	r.Equal(d.version, retVersion, hrn)
	info, err := ts.Storage().Info(context.Background(), d.module, d.version)
	r.NoError(err, hrn)
	r.Equal(d.info, info, hrn)
	mod, err := ts.Storage().GoMod(context.Background(), d.module, d.version)
	r.NoError(err, hrn)
	r.Equal(d.mod, mod, hrn)
	zip, err := ts.Storage().Zip(context.Background(), d.module, d.version)
	r.NoError(err, hrn)
	zipContent, err := ioutil.ReadAll(zip)
	r.NoError(err, hrn)
	r.Equal(d.zip, zipContent, hrn)
}

func (d *TestSuites) testDelete(ts storage.TestSuite) {
	r := d.Require()
	version := "delete" + time.Now().String()
	err := ts.Storage().Save(context.Background(), d.module, version, d.mod, bytes.NewReader(d.zip), d.info)
	r.NoError(err)

	tests := []struct {
		module  string
		version string
		want    int
	}{
		{
			module:  "does/not/exist",
			version: "v1.0.0",
			want:    errors.KindNotFound,
		},
		{
			module:  d.module,
			version: version,
			want:    errors.KindUnexpected,
		},
	}
	ctx := context.Background()
	for _, test := range tests {
		err := ts.Storage().Delete(ctx, test.module, test.version)
		r.Equal(test.want, errors.Kind(err), ts.StorageHumanReadableName())
		exists := ts.Storage().Exists(ctx, test.module, test.version)
		r.Equal(false, exists, ts.StorageHumanReadableName())
	}
}
