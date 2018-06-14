package cdn

// Deleter deletes a module data from the CDN. It does not touch the module
// DB key/value store, use MetadataSaver for that
type Deleter interface {
	Delete(module, version string) error
}
