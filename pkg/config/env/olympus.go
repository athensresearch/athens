package env

import "github.com/gobuffalo/envy"

// OlympusGlobalEndpointWithDefault returns Olympus global endpoint defined by OLYMPUS_GLOBAL_ENDPOINT.
func OlympusGlobalEndpointWithDefault(value string) string {
	return envy.Get("OLYMPUS_GLOBAL_ENDPOINT", value)
}
