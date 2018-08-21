package rdbms

import (
	"github.com/gobuffalo/envy"
)

func (rd *RDBMSTestSuite) TestGetSaveRoundTrip() {
	r := rd.Require()
	err := rd.storage.Save(module, redirectURL)
	r.NoError(err)

	gotten, err := rd.storage.Get(module)
	r.NoError(err)
	r.Equal(gotten, redirectURL)
}

func (rd *RDBMSTestSuite) TestNewRDBMSStorage() {
	r := rd.Require()
	e := envy.Get("GO_ENV", "test")
	getterSaver := NewStorage(e)
	getterSaver.Connect()

	r.NotNil(getterSaver.conn)
	r.Equal(getterSaver.connectionName, e)
}
