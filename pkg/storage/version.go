package storage

import "io"

// Version represents a version of a module and contains .mod file and zip of a specific version
type Version struct {
	RevInfo RevInfo
	Mod     []byte
	Zip     io.ReadCloser
}
