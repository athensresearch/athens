package storage

// Lister is the interface that lists versions of a specific baseURL & module
type Lister interface {
	List(baseURL, module string) ([]string, error)
}
