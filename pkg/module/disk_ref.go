package module

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/gomods/athens/pkg/storage"
	"github.com/pkg/errors"
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

func newDiskRef(fs afero.Fs, root, module, version string) *diskRef {
	return &diskRef{
		fs:      fs,
		root:    root,
		module:  module,
		version: version,
	}
}

// Clear is the Ref interface implementation. It deletes all module data from disk
//
// You should always call this function after you fetch a module into a DiskRef
func (d *diskRef) Clear() error {

	// This is required because vgo ensures dependencies are read-only
	// See https://github.com/golang/go/issues/24111 and
	// https://go-review.googlesource.com/c/vgo/+/96978
	walkFn := func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		return d.fs.Chmod(path, 0770)
	}
	err := afero.Walk(d.fs, d.root, walkFn)
	if err != nil {
		return err
	}
	return d.fs.RemoveAll(d.root)
}

// read is the Ref interface implementation.
func (d *diskRef) Read() (*storage.Version, error) {
	var ver storage.Version

	packagePath := getPackagePath(d.root, d.module)

	infoFile := filepath.Join(packagePath, fmt.Sprintf("%s.info", d.version))
	info, err := afero.ReadFile(d.fs, infoFile)
	if err != nil {
		return nil, errors.WithStack(err)
	}
	ver.Info = info

	modFile := filepath.Join(packagePath, fmt.Sprintf("%s.mod", d.version))
	mod, err := afero.ReadFile(d.fs, modFile)
	if err != nil {
		return nil, errors.WithStack(err)
	}
	ver.Mod = mod

	sourceFile, err := d.fs.Open(filepath.Join(packagePath, fmt.Sprintf("%s.zip", d.version)))
	if err != nil {
		return nil, errors.WithStack(err)
	}
	// note: don't close sourceFile here so that the caller can read directly from disk.
	//
	// if we close, then the caller will panic, and the alternative to make this work is
	// that we read into memory and return an io.ReadCloser that reads out of memory
	ver.Zip = sourceFile

	return &ver, nil
}
