package metadata

// Saver saves the module's base URL
// of the module metadata and content. For example:
// module: gomods.io/my/module - URL: 'https://mycdn.com/gomods.io/my/module'
type Saver interface {
	// Save saves the module and base URL pair.
	Save(module, redirectURL string) error
}
