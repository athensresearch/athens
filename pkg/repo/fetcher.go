package repo

// Fetcher is interface used for fetching sources from VCS or Services
type Fetcher interface {
	Fetch() (string, error)
	Clear() error
}
