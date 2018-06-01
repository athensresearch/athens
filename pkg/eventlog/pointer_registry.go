package eventlog

// PointerRegistry is a key/value store that stores an event log pointer for one
// or more Olympus deployments. It is used in proxies (Athens) and Olympus
// deployments as part of the event log sync process
type PointerRegistry interface {
	// LookupPointer returns an event log pointer for the given deployment ID.
	LookupPointer(deploymentID string) (string, error)
	// SetPointer records the current event log pointer for the given deployment ID.
	SetPointer(deploymentID, pointer string) error
}
