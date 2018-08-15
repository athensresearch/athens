package storage

import (
	"time"
)

// RevInfo is json-encodable into the response body for
// GET baseURL/module/@v/version.info
//
// This struct is taken directly from https://research.swtch.com/vgo-module
// (see "Download Protocol" header)
type RevInfo struct {
	Version string    `json:"Version"` // version string
	Time    time.Time `json:"Time"`    // commit time
}
