package payloads

// Upload is used to send a module (zip and mod file) via POST request to the storage backend and save it there.
type Upload struct {
	Module []byte `json:"module"`
	Zip    []byte `json:"zip"`
}
