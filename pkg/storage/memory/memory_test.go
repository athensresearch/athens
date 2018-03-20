package memory

func (m *MemoryTests) TestNewMemoryStorage() {
	r := m.Require()
	getterSaverIface := NewMemoryStorage()
	getterSaver, ok := getterSaverIface.(*getterSaverImpl)
	r.True(ok)
	r.NotNil(getterSaver.versions)
	r.NotNil(getterSaver.RWMutex)
}
