package log

import (
	"github.com/sirupsen/logrus"
)

// Logger is the main struct that any athens
// internal service should use to communicate things.
type Logger struct {
	*logrus.Logger
}

// New constructs a new logger based on the
// environment and the cloud platform it is
// running on. TODO: take cloud arg and env
// to construct the correct JSON formatter.
func New(cloudProvider, level string) *Logger {
	l := logrus.New()
	switch cloudProvider {
	case "GCP":
		l.Formatter = getGCPFormatter()
	case "none":
		l.Formatter = getDevFormatter()
	default:
		l.Formatter = getDefaultFormatter()
	}
	logLevel, err := logrus.ParseLevel(level)
	if err != nil {
		panic(err)
	}
	l.Level = logLevel

	return &Logger{Logger: l}
}

// SystemErr Entry implementation.
func (l *Logger) SystemErr(err error) {
	e := &entry{Entry: logrus.NewEntry(l.Logger)}
	e.SystemErr(err)
}

// WithFields Entry implementation.
func (l *Logger) WithFields(fields map[string]interface{}) Entry {
	e := l.Logger.WithFields(fields)

	return &entry{e}
}
