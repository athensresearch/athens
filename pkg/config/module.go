package config

import "fmt"

// PackageVersionedName return package full name used in storage.
// E.g athens/@v/v1.0/go.mod
func PackageVersionedName(module, version, ext string) string {
	return fmt.Sprintf("%s/@v/%s.%s", module, version, ext)
}

// FmtModVer is a helper function that can take
// pkg/a/b and v2.3.1 and returns pkg/a/b@v2.3.1
func FmtModVer(mod, ver string) string {
	return fmt.Sprintf("%s@%s", mod, ver)
}
