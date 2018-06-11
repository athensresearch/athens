package storage

// Connector connects storage to its backing engine
type Connector interface {
	// Connect connects storage to its backing engine
	Connect() error
}
