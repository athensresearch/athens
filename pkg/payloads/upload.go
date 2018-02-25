package payloads

type Upload struct {
	Module []byte `json:"module_base64"`
	Zip    []byte `json:"zip_base64"`
}
