package storage

type Getter interface {
	// must return NotFoundErr if the coordinates are not found
	Get(baseURL, module, vsn string) (*Version, error)
}
