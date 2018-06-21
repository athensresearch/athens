package fs

import (
	"context"
	"io/ioutil"
	"path/filepath"
)

func (d *FsTests) TestLocationFuncs() {
	r := d.Require()
	storage := d.storage.(*storageImpl)
	moduleLoc := storage.moduleLocation(module)
	r.Equal(filepath.Join(d.rootDir, module), moduleLoc)
	versionedLoc := storage.versionLocation(module, version)
	r.Equal(filepath.Join(d.rootDir, module, version), versionedLoc)
}

func (d *FsTests) TestGetSaveListRoundTrip() {
	r := d.Require()
	r.NoError(d.storage.Save(context.Background(), module, version, mod, zip, info))
	listedVersions, err := d.storage.List(module)
	r.NoError(err)
	r.Equal(1, len(listedVersions))
	retVersion := listedVersions[0]
	r.Equal(version, retVersion)
	gotten, err := d.storage.Get(module, version)
	r.NoError(err)
	defer gotten.Zip.Close()
	// TODO: test the time
	r.Equal(gotten.Mod, mod)
	zipContent, err := ioutil.ReadAll(gotten.Zip)
	r.NoError(err)
	r.Equal(zipContent, zip)
	r.Equal(gotten.Info, info)
}
