package eventlog

import (
	"encoding/json"
	"time"
)

// EventOp holds an operation type on an event
type EventOp string

const (
	// OpAdd represents an add event
	OpAdd EventOp = "ADD"
	// OpDel represents a delete event
	OpDel EventOp = "DEL"
	// OpDep represents a deprecate event
	OpDep EventOp = "DEP"
)

// Event is entry of event log specifying demand for a module.
// It implements json.Marshaler
type Event struct {
	// ID is identifier, also used as a pointer reference target.
	ID string `json:"_id" bson:"_id,omitempty"`
	// Op is the operation on the event log. Valid values are "ADD"
	// TODO: add support for "DEL"
	Op EventOp `json:"op" bson:"op"`
	// Time is cache-miss created/handled time.
	Time time.Time `json:"time_created" bson:"time_created"`
	// Module is module name.
	Module string `json:"module" bson:"module"`
	// Version is version of a module e.g. "1.10", "1.10-deprecated"
	Version string `json:"version" bson:"version"`
}

// MarshalJSON conforms to json.Marshaler
func (e Event) MarshalJSON() ([]byte, error) {
	m := map[string]string{"module": e.Module, "version": e.Version}
	return json.Marshal(&m)
}
