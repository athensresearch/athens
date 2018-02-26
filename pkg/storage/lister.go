package storage

// Lister is the interface that lists versions of a specific baseURL & module
type Lister interface {
	// must return NotFoundErr if baseURL / module isn't found
	List(baseURL, module string) ([]string, error)
	All() (map[string][]*RevInfo, error)
}
