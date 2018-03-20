package storage

type Getter interface {
	// must return ErrNotFound if the coordinates are not found
	Get(baseURL, module, vsn string) (*Version, error)
}
