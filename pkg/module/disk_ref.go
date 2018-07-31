package module

import (
	"fmt"
	"io"
	"os"
	"path/filepath"

	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/paths"
	"github.com/gomods/athens/pkg/storage"
	"github.com/spf13/afero"
)

// diskRef is a Ref implementation for modules on disk. It is not safe to use concurrently.
//
// Do not create this struct directly. use newDiskRef
type diskRef struct {
	root    string
	module  string
	fs      afero.Fs
	version string
}

type zipReadCloser struct {
	zip io.ReadCloser
	ref *diskRef
}

// Close closes the zip file handle and clears up disk space used by the underlying disk ref
// It is the caller's responsibility to call this method to free up utilized disk space
func (rc *zipReadCloser) Close() error {
	rc.zip.Close()
	return clearFiles(rc.ref.fs, rc.ref.root)
}

func (rc *zipReadCloser) Read(p []byte) (n int, err error) {
	return rc.zip.Read(p)
}

func newDiskRef(fs afero.Fs, root, module, version string) *diskRef {
	return &diskRef{
		fs:      fs,
		root:    root,
		module:  module,
		version: version,
	}
}

// clearFiles deletes all data from the given fs at path root
// This function must be called when zip is closed to cleanup the entire GOPATH created by the diskref
func clearFiles(fs afero.Fs, root string) error {
	const op errors.Op = "clearFiles"
	// This is required because vgo ensures dependencies are read-only
	// See https://github.com/golang/go/issues/24111 and
	// https://go-review.googlesource.com/c/vgo/+/96978
	walkFn := func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		return fs.Chmod(path, 0770)
	}
	err := afero.Walk(fs, root, walkFn)
	if err != nil {
		return errors.E(op, err)
	}
	err = fs.RemoveAll(root)
	if err != nil {
		return errors.E(op, err)
	}
	return nil
}

// read is the Ref interface implementation.
func (d *diskRef) Read() (*storage.Version, error) {
	const op errors.Op = "diskRef.Read"
	var ver storage.Version

	encodedModulePath, err := paths.EncodePath(d.module)
	if err != nil {
		return nil, errors.E(op, err, errors.KindBadRequest)
	}
	packagePath := getPackagePath(d.root, encodedModulePath)

	infoFile := filepath.Join(packagePath, fmt.Sprintf("%s.info", d.version))
	info, err := afero.ReadFile(d.fs, infoFile)
	if err != nil {
		return nil, errors.E(op, err)
	}
	ver.Info = info

	modFile := filepath.Join(packagePath, fmt.Sprintf("%s.mod", d.version))
	mod, err := afero.ReadFile(d.fs, modFile)
	if err != nil {
		return nil, errors.E(op, err)
	}
	ver.Mod = mod

	sourceFile, err := d.fs.Open(filepath.Join(packagePath, fmt.Sprintf("%s.zip", d.version)))
	if err != nil {
		return nil, errors.E(op, err)
	}
	// note: don't close sourceFile here so that the caller can read directly from disk.
	//
	// if we close, then the caller will panic, and the alternative to make this work is
	// that we read into memory and return an io.ReadCloser that reads out of memory
	ver.Zip = &zipReadCloser{sourceFile, d}

	return &ver, nil
}
