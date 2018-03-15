package memory

func (m *MemoryTests) TestList() {
	r := m.Require()
	versions := []string{"v1.0.0", "v1.1.0", "v1.1.1"}
	for _, version := range versions {
		r.NoError(m.mem.Save(baseURL, module, version, mod, zip))
	}
	retVersions, err := m.mem.List(baseURL, module)
	r.NoError(err)
	r.Equal(len(versions), len(retVersions))
	for i, expectedVersion := range retVersions {
		r.Equal(expectedVersion, versions[i])
	}
}
