package paths

import (
	"net/url"
	"strings"
)

// IsGoGet returns true if u has ?go-get=1 in its query string
func IsGoGet(u *url.URL) bool {
	return strings.Contains(u.Query().Get("go-get"), "1")
}
