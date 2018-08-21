package env

import (
	"runtime"
	"strconv"

	"github.com/gobuffalo/envy"
)

const (
	// OlympusGlobalEndpoint is a default olympus DNS address
	OlympusGlobalEndpoint = "http://localhost:3001"
)

// OlympusGlobalEndpointWithDefault returns Olympus global endpoint defined by OLYMPUS_GLOBAL_ENDPOINT.
func OlympusGlobalEndpointWithDefault(value string) string {
	return envy.Get("OLYMPUS_GLOBAL_ENDPOINT", value)
}

// GetOlympusEndpoint returns global endpoint with override in mind
func GetOlympusEndpoint() string {
	return OlympusGlobalEndpointWithDefault(OlympusGlobalEndpoint)
}

// AthensMaxConcurrency retrieves maximal level of concurrency based on ATHENS_MAX_CONCURRENCY.
// Defaults to number of cores if env is not set.
func AthensMaxConcurrency() int {
	defaultMaxConcurrency := runtime.NumCPU()
	maxConcurrencyEnv, err := envy.MustGet("ATHENS_MAX_CONCURRENCY")
	if err != nil {
		return defaultMaxConcurrency
	}

	mc, err := strconv.Atoi(maxConcurrencyEnv)
	if err != nil {
		return defaultMaxConcurrency
	}

	return mc
}

// WorkerMaxFails retrieves maximal level of concurrency based on ATHENS_WORKER_MAX_FAILS.
// Defaults to 5.
func WorkerMaxFails() uint {
	defaultMaxFails := uint(5)
	maxFailsEnv, err := envy.MustGet("ATHENS_WORKER_MAX_FAILS")
	if err != nil {
		return defaultMaxFails
	}

	mc, err := strconv.Atoi(maxFailsEnv)
	if err != nil {
		return defaultMaxFails
	}

	return uint(mc)
}
