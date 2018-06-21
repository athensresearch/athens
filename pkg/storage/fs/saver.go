package fs

import (
	"context"
	"os"
	"path/filepath"

	"github.com/spf13/afero"
)

func (s *storageImpl) Save(_ context.Context, module, vsn string, mod, zip, info []byte) error {
	dir := s.versionLocation(module, vsn)
	// TODO: 777 is not the best filemode, use something better

	// make the versioned directory to hold the go.mod and the zipfile
	if err := s.filesystem.MkdirAll(dir, os.ModeDir|os.ModePerm); err != nil {
		return err
	}

	// write the go.mod file
	if err := afero.WriteFile(s.filesystem, filepath.Join(dir, "go.mod"), mod, os.ModePerm); err != nil {
		return err
	}

	// write the zipfile
	if err := afero.WriteFile(s.filesystem, filepath.Join(dir, "source.zip"), zip, os.ModePerm); err != nil {
		return err
	}

	// write the info file
	return afero.WriteFile(s.filesystem, filepath.Join(dir, vsn+".info"), info, os.ModePerm)
}
