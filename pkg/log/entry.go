package log

import (
	"github.com/sirupsen/logrus"
)

// Entry is an abstraction to the
// Logger and the logrus.Entry
// so that *Logger always creates
// an Entry copy which ensures no
// Fields are being overwritten.
type Entry interface {
	// Basic Logging Operation
	Debugf(format string, args ...interface{})
	Infof(format string, args ...interface{})
	Warnf(format string, args ...interface{})
	Errorf(format string, args ...interface{})

	// Attach contextual information to the logging entry
	WithFields(fields map[string]interface{}) Entry

	// SystemErr is a method that disects the error
	// and logs the appropriate level and fields for it.
	// TODO(marwan-at-work): When we have our own Error struct
	// this method will be very useful.
	SystemErr(err error)
}

type entry struct {
	*logrus.Entry
}

func (e *entry) WithFields(fields map[string]interface{}) Entry {
	ent := e.Entry.WithFields(fields)
	return &entry{ent}
}

func (e *entry) SystemErr(err error) {
	e.Entry.Error(err)
}
