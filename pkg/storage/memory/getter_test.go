package memory

func (m *MemoryTests) TestGet() {
	mem := m.mem
	r := m.Require()
	const version = "v1.0.0"
	r.NoError(mem.Save(baseURL, module, version, mod, zip))
	vsn, err := mem.Get(baseURL, module, version)
	r.NoError(err)
	r.Equal(version, vsn.RevInfo.Version)
	r.Equal(mod, vsn.Mod)
	r.Equal(zip, vsn.Zip)
}
