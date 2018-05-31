package storage

// Deleter deletes module metadata and its source from underlying storage
type Deleter interface {
	// Delete must return ErrNotFound if the module/version are not found
	Delete(module, vsn string) error
}
