package storage

// Checker is the interface that checks if the version of the module exists
type Checker interface {
	// Exists checks whether or not module in specified version is present
	// in the backing storage
	Exists(module, version string) bool
}
