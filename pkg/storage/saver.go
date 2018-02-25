package storage

type Saver interface {
	Save(baseURL, module, version string, mod, zip []byte) error
}
