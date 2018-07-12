package env

import (
	"strconv"

	"github.com/gobuffalo/envy"
)

// EnableCSRFProtection determines whether to enable CSRF protection
func EnableCSRFProtection() bool {
	boolStr := envy.Get("ATHENS_ENABLE_CSRF_PROTECTION", "false")
	enable, err := strconv.ParseBool(boolStr)
	if err != nil {
		return false
	}
	return enable
}
