package env

import (
	"os"
)

// AthensPathPrefix returns whether the Proxy (or Olympus)
// should have a basepath. Certain proxies and services
// are distinguished based on subdomain, while others are based
// on path prefixes.
func AthensPathPrefix() string {
	return os.Getenv("ATHENS_PATH_PREFIX")
}
