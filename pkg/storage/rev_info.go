package storage

import (
	"time"
)

type RevInfo struct {
	Version string    // version string
	Name    string    // complete ID in underlying repository
	Short   string    // shortened ID, for use in pseudo-version
	Time    time.Time // commit time
}
