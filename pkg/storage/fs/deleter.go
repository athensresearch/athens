package fs

// Delete removes a specific version of a module
func (v *storageImpl) Delete(module, version string) error {
	versionedPath := v.versionLocation(module, version)
	return v.filesystem.RemoveAll(versionedPath)
}
