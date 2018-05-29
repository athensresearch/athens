package storage

import "io"

// Version represents a version of a module and contains .mod file, a .info file and zip file of a specific version
type Version struct {
	Mod  []byte
	Zip  io.ReadCloser
	Info []byte
}
