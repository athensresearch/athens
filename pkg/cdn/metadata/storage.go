package metadata

// Storage is a complete cdn metadata storage backend (i.e. file system, database) implementation - a getter and saver
type Storage interface {
	Getter
	Saver
}
