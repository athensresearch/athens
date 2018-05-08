package olympus

import "github.com/gomods/athens/pkg/eventlog"

// Log represents event log fetched from remote olympus server
type Log struct {
	uri string
}

// NewLog creates log reader from remote olympus log
func NewLog(uri string) eventlog.Eventlog {
	return &Log{uri: uri}
}

// Read reads all events in event log.
func (o *Log) Read() ([]eventlog.Event, error) {
	// TODO: implement read from endpoint
	return nil, nil
}

// ReadFrom reads all events from the log starting at event with specified id (excluded).
// If id is not found behaves like Read().
func (o *Log) ReadFrom(id string) ([]eventlog.Event, error) {
	// TODO: implement read from endpoint
	return nil, nil
}

// Append appends Event to event log and returns its ID.
func (o *Log) Append(event eventlog.Event) (string, error) {
	// TODO: implement cache miss reporting
	return "", nil
}

// Clear is a method for clearing entire state of event log
func (o *Log) Clear(id string) error {
	// Do not implement, we cannot clear remote olympus
	return nil
}
