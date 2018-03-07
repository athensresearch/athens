package bson

// Current state of the JSON tag fallback option. 
var useJSONTagFallback = false

// SetJSONTagFallback enables or disables the JSON-tag fallback for structure tagging. When this is enabled, structures
// without BSON tags on a field will fall-back to using the JSON tag (if present).
func SetJSONTagFallback(state bool) {
	useJSONTagFallback = state
}

// JSONTagFallbackState returns the current status of the JSON tag fallback compatability option. See SetJSONTagFallback
// for more information.
func JSONTagFallbackState() bool {
	return useJSONTagFallback
}
