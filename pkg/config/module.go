package config

import "fmt"

// PackageVersionedName return package full name used in storage.
// E.g athens/@v/v1.0/go.mod
func PackageVersionedName(module, version, ext string) string {
	return fmt.Sprintf("%s/@v/%s.%s", module, version, ext)
}
