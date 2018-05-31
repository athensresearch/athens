package cdn

// Getter gets the details about a given baseURL & module and returns the base
// URL of the module metadata and content. For example, if
// gomods.io/my/module is requested by the 'vgo get' command, this might return
// 'https://mycdn.com/gomods.io/my/module'
//
// Then, the following URLs would be available:
//
// - https://mycdn.com/gomods.io/my/module/@v/list
// - https://mycdn.com/gomods.io/my/module/@v/{version}.info
// - https://mycdn.com/gomods.io/my/module/@v/{version}.mod
// - https://mycdn.com/gomods.io/my/module/@v/{version}.zip
type Getter interface {
	// Get gets the base URL for the given module.
	// Returns ErrNotFound if the module isn't found, and another
	// non-nil error on any other error encountered
	Get(module string) (string, error)
}
