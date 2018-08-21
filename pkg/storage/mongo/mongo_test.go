package mongo

import "github.com/gomods/athens/pkg/config/env"

func (m *MongoTests) TestNewMongoStorage() {
	r := m.Require()
	muri, err := env.MongoConnectionString()
	certPath := env.MongoCertPath()
	getterSaver, err := NewStorageWithCert(muri, certPath)

	r.NoError(err)
	r.NotNil(getterSaver.c)
	r.NotNil(getterSaver.d)
	r.NotNil(getterSaver.s)
	r.Equal(getterSaver.url, muri)
}
