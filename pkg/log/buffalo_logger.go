package log

import (
	"github.com/gobuffalo/buffalo"
	"github.com/sirupsen/logrus"
)

// Buffalo returns a more sane logging format
// than the default buffalo formatter.
// For the most part, we only care about
// the path, the method, and the status code.
// It's also good to note that internal logs
// from buffalo should only be allowed in development
// as our logging-system should be handled from our codebase.
// If the environment is production, then its a noop logger.
func Buffalo(lvl logrus.Level) buffalo.Logger {
	l := logrus.New()
	l.Formatter = &buffaloFormatter{}
	l.SetLevel(lvl)

	return &buffaloLogger{l}
}

type buffaloLogger struct{ logrus.FieldLogger }

func (bf *buffaloLogger) WithField(key string, val interface{}) buffalo.Logger {
	e := bf.FieldLogger.WithField(key, val)

	return &buffaloLogger{e}
}

func (bf *buffaloLogger) WithFields(fields map[string]interface{}) buffalo.Logger {
	e := bf.FieldLogger.WithFields(fields)
	return &buffaloLogger{e}
}
