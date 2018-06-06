package rdbms

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
	e := "development"
	getterSaver := NewStorage(e)
	getterSaver.Connect()

	r.NotNil(getterSaver.conn)
	r.Equal(getterSaver.connectionName, e)
}
