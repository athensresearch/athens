package payloads

import "github.com/gomods/athens/pkg/eventlog"

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

// PushNotification is used to notify other Olympus instances about a new event
type PushNotification struct {
	Events    []eventlog.Event `json:"events"`
	OriginURL string           `json:"originURL"`
}
