package storage

// BackendConnector is a regular storage backend with Connect functionality
type BackendConnector interface {
	Backend
	Connect() error
}

type noOpConnectedBackend struct {
	s Backend
}

// NoOpBackendConnector wraps storage backend with Connect functionality
func NoOpBackendConnector(s Backend) BackendConnector {
	return noOpConnectedBackend{s: s}
}

func (n noOpConnectedBackend) Connect() error {
	return nil
}

func (n noOpConnectedBackend) Get(module, vsn string) (*Version, error) {
	return n.s.Get(module, vsn)
}
func (n noOpConnectedBackend) List(module string) ([]string, error) {
	return n.s.List(module)
}
func (n noOpConnectedBackend) Save(module, version string, mod, zip []byte) error {
	return n.s.Save(module, version, mod, zip)
}
