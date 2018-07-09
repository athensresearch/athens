package env

import "github.com/gobuffalo/envy"

// OlympusGlobalEndpointWithDefault returns Olympus global endpoint defined by OLYMPUS_GLOBAL_ENDPOINT.
func OlympusGlobalEndpointWithDefault(value string) string {
	return envy.Get("OLYMPUS_GLOBAL_ENDPOINT", value)
}

// OlympusHTTPPort returns the port that the olympus server is running on;
// should default to 3001.
func OlympusHTTPPort(value string) string {
	return envy.Get("OLYMPUS_HTTP_PORT", value)
}
