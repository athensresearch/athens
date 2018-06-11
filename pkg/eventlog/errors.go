package eventlog

// ErrUseNewOlympus is error raise on redirect from global dns entry to specific deployment
type ErrUseNewOlympus struct {
	Endpoint string
}

func (e *ErrUseNewOlympus) Error() string {
	return e.Endpoint
}
