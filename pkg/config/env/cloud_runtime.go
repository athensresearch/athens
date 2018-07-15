package env

import (
	"github.com/gobuffalo/envy"
)

// CloudRuntime returns the Cloud Provider
// underneath which the Proxy/Registry is running.
func CloudRuntime() string {
	return envy.Get("ATHENS_CLOUD_RUNTIME", "none")
}
