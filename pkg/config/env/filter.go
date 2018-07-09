package env

import "github.com/gobuffalo/envy"

const defaultConfigurationFileName = "filter.conf"

// IncludeExcludeFileName specifies file name for include exclude filter
// If no filename is specified it fallbacks to 'filter.conf'
func IncludeExcludeFileName() string {
	return envy.Get("ATHENS_FILTER_FILENAME", defaultConfigurationFileName)
}
