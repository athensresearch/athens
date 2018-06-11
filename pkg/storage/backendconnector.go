package storage

// BackendConnector is a regular storage backend with Connect functionality
type BackendConnector interface {
	Backend
	Connect() error
}

type noOpConnectedBackend struct {
	backend Backend
}

// NoOpBackendConnector wraps storage backend with Connect functionality
func NoOpBackendConnector(b Backend) BackendConnector {
	return noOpConnectedBackend{backend: b}
}

func (n noOpConnectedBackend) Connect() error {
	return nil
}

func (n noOpConnectedBackend) Exists(module, version string) bool {
	return n.backend.Exists(module, version)
}

func (n noOpConnectedBackend) Get(module, vsn string) (*Version, error) {
	return n.backend.Get(module, vsn)
}
func (n noOpConnectedBackend) List(module string) ([]string, error) {
	return n.backend.List(module)
}
func (n noOpConnectedBackend) Save(module, version string, mod, zip, info []byte) error {
	return n.backend.Save(module, version, mod, zip, info)
}
