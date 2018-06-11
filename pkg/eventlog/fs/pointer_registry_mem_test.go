package fs

import (
	"testing"

	"github.com/gomods/athens/pkg/eventlog"
	"github.com/spf13/afero"
	"github.com/stretchr/testify/suite"
)

type FsRegistryTests struct {
	suite.Suite
	registry *Registry
}

func TestRegistry(t *testing.T) {
	suite.Run(t, new(FsRegistryTests))
}

func (fs *FsRegistryTests) SetupTest() {
	store := NewRegistry("/tmp", afero.NewMemMapFs())

	fs.registry = store
}

func (fs *FsRegistryTests) TestLookupPointer() {
	r := fs.Require()
	err := fs.registry.SetPointer("deployment1", "location1")
	r.NoError(err)
	gotten, err := fs.registry.LookupPointer("deployment1")
	r.NoError(err)
	r.Equal("location1", gotten)

	err = fs.registry.SetPointer("deployment1", "location2")
	r.NoError(err)
	gotten, err = fs.registry.LookupPointer("deployment1")
	r.NoError(err)
	r.Equal("location2", gotten)

	gotten, err = fs.registry.LookupPointer("doesnt-exist")
	r.Equal(eventlog.ErrDeploymentNotFound, err)
}

func (fs *FsRegistryTests) TestNewRegistry() {
	r := fs.Require()
	root := "/tmp"
	registry := NewRegistry(root, afero.NewMemMapFs())

	r.NotNil(registry.rootDir)
	r.NotNil(registry.fs)
	// TODO: check fs current directory is rootDir?
}
