package storage

// StorageConnector is a regular storage with Connect functionality
type StorageConnector interface {
	Storage
	Connect() error
}

type noOpConnectedStorage struct {
	s Storage
}

// NoOpStorageConnector wraps storage with Connect functionality
func NoOpStorageConnector(s Storage) StorageConnector {
	return noOpConnectedStorage{s: s}
}

func (n noOpConnectedStorage) Connect() error {
	return nil
}

func (n noOpConnectedStorage) Get(baseURL, module, vsn string) (*Version, error) {
	return n.s.Get(baseURL, module, vsn)
}
func (n noOpConnectedStorage) List(baseURL, module string) ([]string, error) {
	return n.s.List(baseURL, module)
}
func (n noOpConnectedStorage) Save(baseURL, module, version string, mod, zip []byte) error {
	return n.s.Save(baseURL, module, version, mod, zip)
}
