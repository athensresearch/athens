package storage

// Saver saves module metadata and its source to underlying storage
type Saver interface {
	Save(module, version string, mod, zip []byte) error
}
