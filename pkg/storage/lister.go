package storage

// Lister is the interface that lists versions of a specific baseURL & module
type Lister interface {
	// List gets all the versions for the given baseURL & module.
	// It returns ErrNotFound if baseURL/module isn't found
	List(baseURL, module string) ([]string, error)
}
