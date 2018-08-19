package env

import (
	"os"
)

// ProxyForceSSL returns true if the PROXY_FORCE_SSL
// env is set to "true", otherwise defaults to false.
// This is used to make sure the Proxy would redirect
// to https on any request.
func ProxyForceSSL() bool {
	return os.Getenv("PROXY_FORCE_SSL") == "true"
}
