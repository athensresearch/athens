package fs

import (
	"testing"

	"github.com/gomods/athens/pkg/cdn/metadata"
	"github.com/spf13/afero"
	"github.com/stretchr/testify/suite"
)

const (
	module      = "gomods.io/my/testmodule"
	redirectURL = "https://mycdn.com/gomods.io/my/testmodule"
)

type FsTests struct {
	suite.Suite
	storage metadata.StorageConnector
	rootDir string
	fs      afero.Fs
}

func TestFs(t *testing.T) {
	fs := afero.NewOsFs()
	root, _ := afero.TempDir(fs, "", "athens-fs-tests")
	stg := NewStorage(fs, root)
	suite.Run(t, &FsTests{storage: metadata.NoOpStorageConnector(stg), rootDir: root, fs: fs})
}

func (fst *FsTests) TearDownTest() {
	fst.Require().NoError(fst.fs.RemoveAll(fst.rootDir))
}

func (fst *FsTests) TestGetSaveListRoundTrip() {
	r := fst.Require()
	err := fst.storage.Save(module, redirectURL)
	r.NoError(err)

	gotten, err := fst.storage.Get(module)
	r.NoError(err)
	r.Equal(gotten, redirectURL)
}

func (fst *FsTests) TestSaveErrorIfMetadataFileAlreadyExists() {
	r := fst.Require()
	err := fst.storage.Save(module, redirectURL)
	r.NoError(err)

	err = fst.storage.Save(module, redirectURL)
	r.Error(err)
}
