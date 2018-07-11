package rdbms

func (rd *RDBMSTestSuite) TestNewRDBMSStorage() {
	r := rd.Require()
	e := "development"
	getterSaver := NewRDBMSStorage(e)
	getterSaver.Connect()

	r.NotNil(getterSaver.conn)
	r.Equal(getterSaver.connectionName, e)
}
