package storage

import (
	"time"
)

type RevInfo struct {
	Version string    `json:"Version"` // version string
	Name    string    `json:"Name"`    // complete ID in underlying repository
	Short   string    `json:"Short"`   // shortened ID, for use in pseudo-version
	Time    time.Time `json:"Time"`    // commit time
}
