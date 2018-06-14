package metadata

// StorageConnector is a regular storage Storage with Connect functionality
type StorageConnector interface {
	Storage
	Connect() error
}

type noOpConnectedStorage struct {
	s Storage
}

// NoOpStorageConnector wraps storage Storage with Connect functionality
func NoOpStorageConnector(s Storage) StorageConnector {
	return noOpConnectedStorage{s: s}
}

func (n noOpConnectedStorage) Connect() error {
	return nil
}

func (n noOpConnectedStorage) Get(module string) (string, error) {
	return n.s.Get(module)
}

func (n noOpConnectedStorage) Save(module, redirectURL string) error {
	return n.s.Save(module, redirectURL)
}
