package mem

import (
	"bytes"
	"context"
	"io/ioutil"
	"testing"

	"github.com/stretchr/testify/suite"
)

const (
	module  = "testmodule"
	version = "v1.0.0"
)

var (
	// TODO: put these values inside of the suite, and generate longer values.
	// This should help catch edge cases, like https://github.com/gomods/athens/issues/38
	//
	// Also, consider doing something similar to what testing/quick does
	// with the Generator interface (https://godoc.org/testing/quick#Generator).
	// The rough, simplified idea would be to run a single test case multiple
	// times over different (increasing) values.
	mod  = []byte("123")
	zip  = []byte("456")
	info = []byte("789")
)

type MemTests struct {
	suite.Suite
}

func TestMemStorage(t *testing.T) {
	suite.Run(t, new(MemTests))
}

func (d *MemTests) TestGetSaveListRoundTrip() {
	r := d.Require()
	// create new in mem storage
	storage, err := NewStorage()
	d.Require().NoError(err)

	// save and list modules
	r.NoError(storage.Save(context.Background(), module, version, mod, bytes.NewReader(zip), info))
	listedVersions, err := storage.List(module)
	r.NoError(err)
	r.Equal(1, len(listedVersions))
	retVersion := listedVersions[0]
	r.Equal(version, retVersion)

	// create new in-mem storage - we should be able to get the saved module
	storage, err = NewStorage()
	d.Require().NoError(err)

	gotten, err := storage.Get(module, version)
	r.NoError(err)
	defer gotten.Zip.Close()
	// TODO: test the time
	r.Equal(gotten.Mod, mod)
	zipContent, err := ioutil.ReadAll(gotten.Zip)
	r.NoError(err)
	r.Equal(zipContent, zip)
	r.Equal(gotten.Info, info)
}
