package memory

func (m *MemoryTests) TestNew() {
	r := m.Require()
	getterSaverIface := New()
	getterSaver, ok := getterSaverIface.(*getterSaverImpl)
	r.True(ok)
	r.NotNil(getterSaver.versions)
	r.NotNil(getterSaver.RWMutex)
}
