package env

import "github.com/gobuffalo/envy"

const defaultConfigurationFileName = "filter.conf"

// FilterConfigurationFileName specifies file name for include exclude private filter
// If no filename is specified it fallbacks to 'filter.conf'
func FilterConfigurationFileName() string {
	return envy.Get("ATHENS_FILTER_FILENAME", defaultConfigurationFileName)
}
