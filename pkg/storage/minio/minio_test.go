package minio

import (
	"io/ioutil"
)

func (d *MinioTests) TestGetSaveListRoundTrip() {
	r := d.Require()
	r.NoError(d.storage.Save(module, version, mod, zip))
	listedVersions, err := d.storage.List(module)
	r.NoError(err)
	r.Equal(1, len(listedVersions))
	retVersion := listedVersions[0]
	r.Equal(version, retVersion)
	gotten, err := d.storage.Get(module, version)
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
