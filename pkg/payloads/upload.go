package payloads

// Upload is used to send a module (zip and mod file) via POST request to the storage backend and save it there.
type Upload struct {
	Module []byte `json:"module"`
	Zip    []byte `json:"zip"`
	Info   []byte `json:"info"`
}

// Module is used by proxy to send info about cache miss to Olympus
type Module struct {
	Name    string `json:"name"`
	Version string `json:"version"`
}
