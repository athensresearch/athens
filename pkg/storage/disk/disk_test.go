package disk

import (
	"io/ioutil"
	"path/filepath"
)

func (d *DiskTests) TestLocationFuncs() {
	r := d.Require()
	storage := d.storage.(*storageImpl)
	moduleLoc := storage.moduleDiskLocation(baseURL, module)
	r.Equal(filepath.Join(d.rootDir, baseURL, module), moduleLoc)
	versionedLoc := storage.versionDiskLocation(baseURL, module, version)
	r.Equal(filepath.Join(d.rootDir, baseURL, module, version), versionedLoc)
}

func (d *DiskTests) TestGetSaveListRoundTrip() {
	r := d.Require()
	r.NoError(d.storage.Save(baseURL, module, version, mod, zip))
	listedVersions, err := d.storage.List(baseURL, module)
	r.NoError(err)
	r.Equal(1, len(listedVersions))
	retVersion := listedVersions[0]
	r.Equal(version, retVersion)
	gotten, err := d.storage.Get(baseURL, module, version)
	r.NoError(err)
	defer gotten.Zip.Close()
	r.Equal(version, gotten.RevInfo.Version)
	r.Equal(version, gotten.RevInfo.Name)
	r.Equal(version, gotten.RevInfo.Short)
	// TODO: test the time
	r.Equal(gotten.Mod, mod)
	zipContent, err := ioutil.ReadAll(gotten.Zip)
	r.NoError(err)
	r.Equal(zipContent, zip)
}
