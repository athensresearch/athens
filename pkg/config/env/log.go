package env

import (
	"github.com/gobuffalo/envy"
	"github.com/sirupsen/logrus"
)

// LogLevel returns the system's
// exposure to internal logs. Defaults
// to debug.
func LogLevel() (logrus.Level, error) {
	lvlStr := envy.Get("ATHENS_LOG_LEVEL", "debug")
	return logrus.ParseLevel(lvlStr)
}

// BuffaloLogLevel returns the log level for logs
// emitted by Buffalo itself. This is different from our own
// LogLevel in this file because you might want info level
// for our codebase, but panic level for buffalo.
func BuffaloLogLevel() (logrus.Level, error) {
	lvlStr := envy.Get("BUFFALO_LOG_LEVEL", "debug")
	return logrus.ParseLevel(lvlStr)
}
