package storage

import "io"

type Version struct {
	RevInfo RevInfo
	Mod     []byte
	Zip     io.ReadCloser
}
