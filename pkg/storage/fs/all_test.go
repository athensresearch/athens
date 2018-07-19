package fs

import (
	"testing"

	"github.com/gomods/athens/pkg/storage"
	"github.com/spf13/afero"
	"github.com/stretchr/testify/suite"
)

const (
	module  = "testmodule"
	version = "v1.0.0"
)

type FsTests struct {
	suite.Suite
	storage storage.Backend
	rootDir string
	fs      afero.Fs
}

func (d *FsTests) SetupTest() {
	memFs := afero.NewOsFs()
	r, err := afero.TempDir(memFs, "", "athens-fs-tests")
	d.Require().NoError(err)
	d.storage, err = NewStorage(r, memFs)
	d.Require().NoError(err)
	d.rootDir = r
	d.fs = memFs
}

func (d *FsTests) TearDownTest() {
	d.Require().NoError(d.fs.RemoveAll(d.rootDir))
}

func TestDiskStorage(t *testing.T) {
	suite.Run(t, new(FsTests))
}
