package env

import "github.com/gobuffalo/envy"

const noProxyValidator = ""

// ValidatorHook specifies the url of the endpoint to validate the modules agains
// It returns the endpoint (if it was configured) and a bool meaning that the endpoint return value is valid
func ValidatorHook() (endpoint string, found bool) {
	endpoint = envy.Get("ATHENS_PROXY_VALIDATOR", noProxyValidator)
	if endpoint == noProxyValidator {
		found = false
		endpoint = ""
	} else {
		found = true
	}
	return
}
