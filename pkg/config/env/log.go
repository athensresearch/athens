package env

import (
	"github.com/gobuffalo/envy"
)

// LogLevel returns the system's
// exposure to internal logs. Defaults
// to debug.
func LogLevel() string {
	return envy.Get("ATHENS_LOG_LEVEL", "debug")
}
