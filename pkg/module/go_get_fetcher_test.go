package module

import (
	"fmt"
	"io/ioutil"
	"log"

	"github.com/gomods/athens/pkg/config/env"
	"github.com/stretchr/testify/assert"

	"github.com/spf13/afero"
)

func (s *ModuleSuite) TestNewGoGetFetcher() {
	r := s.Require()
	fetcher, err := NewGoGetFetcher(s.goBinaryName, s.fs)
	r.NoError(err)
	_, ok := fetcher.(*goGetFetcher)
	r.True(ok)
}

func (s *ModuleSuite) TestGoGetFetcherError() {
	fetcher, err := NewGoGetFetcher("invalidpath", afero.NewOsFs())

	assert.Nil(s.T(), fetcher)
	assert.EqualError(s.T(), err, "exec: \"invalidpath\": executable file not found in $PATH")
}

func (s *ModuleSuite) TestGoGetFetcherFetch() {
	r := s.Require()
	// we need to use an OS filesystem because fetch executes vgo on the command line, which
	// always writes to the filesystem
	fetcher, err := NewGoGetFetcher(s.goBinaryName, afero.NewOsFs())
	r.NoError(err)
	ref, err := fetcher.Fetch(repoURI, version)
	r.NoError(err, "fetch shouldn't error")
	ver, err := ref.Read()
	r.NoError(err)
	defer ver.Zip.Close()

	r.True(len(ver.Info) > 0)

	r.True(len(ver.Mod) > 0)

	zipBytes, err := ioutil.ReadAll(ver.Zip)
	r.NoError(err)
	r.True(len(zipBytes) > 0)

	// close the version's zip file (which also cleans up the underlying diskref's GOPATH) and expect it to fail again
	r.NoError(ver.Zip.Close())
	ver, err = ref.Read()
	r.NotNil(err)
	r.Nil(ver)
}

func ExampleFetcher() {
	repoURI := "github.com/arschles/assert"
	version := "v1.0.0"
	goBinaryName := env.GoBinPath()
	fetcher, err := NewGoGetFetcher(goBinaryName, afero.NewOsFs())
	if err != nil {
		log.Fatal(err)
	}
	ref, err := fetcher.Fetch(repoURI, version)
	// handle errors if any
	if err != nil {
		return
	}
	versionData, err := ref.Read()
	// Close the handle to versionData.Zip once done
	// This will also handle cleanup so it's important to call Close
	defer versionData.Zip.Close()
	if err != nil {
		return
	}
	// Do something with versionData
	fmt.Println(string(versionData.Mod))
	// Output: module github.com/arschles/assert
}
