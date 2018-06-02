package eventlog

import "errors"

// ErrDeploymentNotFound is returned when the deployment ID is not found
// in a PointerRegistry.
var ErrDeploymentNotFound = errors.New("deployment ID not found")

// RegisteredEventlog stores the relationship between a deploymentID and
// the pointer to it's olympus server eventlog
type RegisteredEventlog struct {
	DeploymentID string `bson:"deployment"`
	Pointer      string `bson:"ptr"`
}

// PointerRegistry is a key/value store that stores an event log pointer for one
// or more Olympus deployments. It is used in proxies (Athens) and Olympus
// deployments as part of the event log sync process
type PointerRegistry interface {
	// LookupPointer returns an event log pointer for the given deployment ID.
	LookupPointer(deploymentID string) (string, error)
	// SetPointer records the current event log pointer for the given deployment ID.
	SetPointer(deploymentID, pointer string) error
}
