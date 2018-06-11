package storage

// Backend is a complete storage backend (i.e. file system, database) implementation - a lister, reader and saver
type Backend interface {
	Lister
	Getter
	Checker
	Saver
}
