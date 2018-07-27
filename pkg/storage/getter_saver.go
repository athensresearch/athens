package storage

// GetterSaver is a getter and a saver composed into one type
type GetterSaver interface {
	Getter
	Saver
}
