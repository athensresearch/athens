package payloads

import "github.com/gomods/athens/pkg/eventlog"

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
