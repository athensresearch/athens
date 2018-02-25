package storage

type Versioner interface {
	// must return NotFoundErr if the coordinates are not found
	Info(baseURL, module, version string) (*RevInfo, error)
}
