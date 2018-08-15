package rdbms

func (rd *RDBMSTestSuite) TestNewRDBMSStorage() {
	r := rd.Require()
	e := "development"
	getterSaver, err := NewRDBMSStorage(e)
	r.NoError(err)

	r.NotNil(getterSaver.conn)
	r.Equal(getterSaver.connectionName, e)
}
