package mongo

func (m *MongoTests) TestList() {
	r := m.Require()
	versions := []string{"v1.0.0", "v1.1.0", "v1.2.0"}
	for _, version := range versions {
		m.storage.Save(baseURL, module, version, mod, zip)
	}
	retVersions, err := m.storage.List(baseURL, module)
	r.NoError(err)
	r.Equal(versions, retVersions)
}
