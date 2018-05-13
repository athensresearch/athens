package repo

import (
	"path/filepath"
	"testing"

	"github.com/spf13/afero"
)

func Test_Download(t *testing.T) {
	version := "v0.1.8"
	gitURI := "github.com/bketelsen/captainhook"
	fs := afero.NewOsFs()
	fetcher, err := NewGenericFetcher(fs, gitURI, version)
	if err != nil {
		t.Error(err)
		t.Fail()
	}

	path, err := fetcher.Fetch()

	if err == ErrLimitExceeded {
		t.Log("Skipped due to exceeded github quota")
		t.Skip()
	}

	if err != nil {
		t.Error(err)
		t.Fail()
	}
	if path == "" {
		t.Error("path null")
		t.Fail()
	}

	if _, err := fs.Stat(filepath.Join(path, version+".mod")); err != nil {
		t.Error(err)
		t.Fail()
	}

	if _, err := fs.Stat(filepath.Join(path, version+".zip")); err != nil {
		t.Error(err)
		t.Fail()
	}

	if _, err := fs.Stat(filepath.Join(path, version+".info")); err != nil {
		t.Error(err)
		t.Fail()
	}

	t.Log(path)
	err = fetcher.Clear()
	if err != nil {
		t.Error(err)
	}
}
