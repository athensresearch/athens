package mongo

func (m *MongoTests) TestNewMongoStorage() {
	r := m.Require()
	url := "mongodb://127.0.0.1:27017"
	getterSaver, err := NewStorage(url)

	r.NoError(err)
	r.NotNil(getterSaver.c)
	r.NotNil(getterSaver.d)
	r.NotNil(getterSaver.s)
	r.Equal(getterSaver.url, url)
}
