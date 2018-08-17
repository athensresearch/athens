package env

import "github.com/gobuffalo/envy"

const defaultConfigurationFileName = "filter.conf"

// FilterConfigurationFileName specifies file name for include exclude private filter
// If no filename is specified it fallbacks to 'filter.conf'
func FilterConfigurationFileName() string {
	return envy.Get("ATHENS_FILTER_FILENAME", defaultConfigurationFileName)
}

// FilterOff checks PROXY_FILTER_OFF env and returns
// true of it's equal to "true", otherwise false always.
// It defaults to "true" until Olympus is the default
// place to grab modules before the Proxy.
func FilterOff() bool {
	return envy.Get("PROXY_FILTER_OFF", "true") == "true"
}
