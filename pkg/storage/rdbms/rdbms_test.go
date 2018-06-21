package rdbms

import (
	"context"
	"io/ioutil"
)

func (rd *RDBMSTestSuite) TestGetSaveListRoundTrip() {
	r := rd.Require()
	err := rd.storage.Save(context.Background(), module, version, mod, zip, info)
	r.NoError(err)
	listedVersions, err := rd.storage.List(module)
	r.NoError(err)
	r.Equal(1, len(listedVersions))
	retVersion := listedVersions[0]
	r.Equal(version, retVersion)
	gotten, err := rd.storage.Get(module, version)
	r.NoError(err)
	defer gotten.Zip.Close()
	// TODO: test the time
	r.Equal(gotten.Mod, mod)
	zipContent, err := ioutil.ReadAll(gotten.Zip)
	r.NoError(err)
	r.Equal(zipContent, zip)
	r.Equal(gotten.Info, info)
}

func (rd *RDBMSTestSuite) TestNewRDBMSStorage() {
	r := rd.Require()
	e := "development"
	getterSaver := NewRDBMSStorage(e)
	getterSaver.Connect()

	r.NotNil(getterSaver.conn)
	r.Equal(getterSaver.connectionName, e)
}
