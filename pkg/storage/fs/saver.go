package fs

import (
	"context"
	"io"
	"os"
	"path/filepath"

	"github.com/gomods/athens/pkg/errors"
	opentracing "github.com/opentracing/opentracing-go"
	"github.com/spf13/afero"
)

func (s *storageImpl) Save(ctx context.Context, module, version string, mod []byte, zip io.Reader, info []byte) error {
	const op errors.Op = "fs.Save"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.fs.Save")
	defer sp.Finish()
	dir := s.versionLocation(module, version)
	// TODO: 777 is not the best filemode, use something better

	// make the versioned directory to hold the go.mod and the zipfile
	if err := s.filesystem.MkdirAll(dir, os.ModeDir|os.ModePerm); err != nil {
		return errors.E(op, err, errors.M(module), errors.V(version))
	}

	// write the go.mod file
	if err := afero.WriteFile(s.filesystem, filepath.Join(dir, "go.mod"), mod, os.ModePerm); err != nil {
		return errors.E(op, err, errors.M(module), errors.V(version))
	}

	// write the zipfile
	f, err := s.filesystem.OpenFile(filepath.Join(dir, "source.zip"), os.O_WRONLY|os.O_CREATE|os.O_TRUNC, os.ModePerm)
	if err != nil {
		return errors.E(op, err, errors.M(module), errors.V(version))
	}
	defer f.Close()
	_, err = io.Copy(f, zip)
	if err != nil {
		return errors.E(op, err, errors.M(module), errors.V(version))
	}

	// write the info file
	err = afero.WriteFile(s.filesystem, filepath.Join(dir, version+".info"), info, os.ModePerm)
	if err != nil {
		return errors.E(op, err)
	}
	return nil
}
