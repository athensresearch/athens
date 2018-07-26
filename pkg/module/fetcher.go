package module

// Fetcher fetches module from an upstream source
type Fetcher interface {
	// Fetch fetches the module and puts it somewhere addressable by ModuleRef.
	// returns a non-nil error on failure.
	//
	// The caller should call moduleRef.Clear() after they're done with the module
	Fetch(mod, ver string) (Ref, error)
}
