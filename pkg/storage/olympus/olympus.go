package olympus

import (
	"net/http"
	"time"
)

// ModuleStore represents a mongo backed storage backend.
type ModuleStore struct {
	url    string
	client http.Client
}

// NewStorage returns a remote Olympus store
func NewStorage(url string) *ModuleStore {
	client := http.Client{
		Timeout: 180 * time.Second,
	}
	return &ModuleStore{url: url, client: client}
}
