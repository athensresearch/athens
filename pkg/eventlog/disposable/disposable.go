package disposable

import (
	"github.com/gomods/athens/pkg/eventlog"
)

// Log is event log fetched from olympus server
type Log struct {
	e eventlog.Eventlog
}

// NewLog creates log reader from remote log, log gets cleared after each read
func NewLog(log eventlog.Eventlog) (*Log, error) {
	return &Log{e: log}, nil
}

// Read reads all events in event log.
func (c *Log) Read() ([]eventlog.Event, error) {
	ee, err := c.e.Read()
	if err != nil {
		return ee, err
	}

	if len(ee) > 0 {
		last := ee[len(ee)-1]
		return ee, c.e.Clear(last.ID)
	}

	return ee, nil
}

// ReadFrom reads all events from the log starting at event with specified id (excluded).
// If id is not found behaves like Read().
func (c *Log) ReadFrom(id string) ([]eventlog.Event, error) {
	ee, err := c.e.ReadFrom(id)
	if err != nil {
		return ee, err
	}

	if len(ee) > 0 {
		return ee, c.e.Clear(id)
	}

	return ee, nil
}

// Append appends Event to event log and returns its ID.
func (c *Log) Append(event eventlog.Event) (string, error) {
	return c.e.Append(event)
}
