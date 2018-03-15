package storage

// Storage is a complete storage implementation - a lister, reader and saver
type Storage interface {
	Lister
	Getter
	Saver
}
