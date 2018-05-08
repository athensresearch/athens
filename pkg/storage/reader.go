package storage

// Reader lists all module versions and gets a specific one from the underlying backend.
type Reader struct {
	Lister
	Getter
	Checker
}
