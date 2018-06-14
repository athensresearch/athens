package olympus

import (
	"encoding/json"
	"errors"
	"fmt"
	"io/ioutil"
	"net/http"
	"time"

	"github.com/gomods/athens/pkg/eventlog"
)

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
	return o.ReadFrom("")
}

// ReadFrom reads all events from the log starting at event with specified id (excluded).
// If id is not found behaves like Read().
func (o *Log) ReadFrom(id string) ([]eventlog.Event, error) {
	eventlogURI := fmt.Sprintf("%s/eventlog/%s", o.uri, id)

	// fetch mod file
	client := http.Client{
		Timeout: 180 * time.Second,
		CheckRedirect: func(req *http.Request, via []*http.Request) error {
			return &eventlog.ErrUseNewOlympus{Endpoint: req.URL.String()}
		},
	}
	resp, err := client.Get(eventlogURI)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	el, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	var events []eventlog.Event
	err = json.Unmarshal(el, &events)

	return events, err
}

// ReadSingle gets the module metadata about the given module/version.
// If something went wrong doing the get operation, returns a non-nil error.
func (o *Log) ReadSingle(module, version string) (eventlog.Event, error) {
	return eventlog.Event{}, errors.New("TODO: implement")
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
