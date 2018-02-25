package payloads

type Upload struct {
	Module []byte `json:"module"`
	Zip    []byte `json:"zip"`
}
