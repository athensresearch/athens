package main

import (
	"bytes"

	"github.com/pierrre/archivefile/zip"
)

type file struct {
	Name string
	Body string
}

// the dir must end with a "/"
func makeZip(dir string) ([]byte, error) {
	buf := new(bytes.Buffer)
	if err := zip.Archive(dir, buf, nil); err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}
