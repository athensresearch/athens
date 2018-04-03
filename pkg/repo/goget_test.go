package repo

import (
	"os"
	"path/filepath"
	"testing"
)

func Test_Download(t *testing.T) {
	version := "v0.1.8"
	gitURI := "github.com/bketelsen/captainhook"

	fetcher, err := NewGenericFetcher(gitURI, version)
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

	if _, err := os.Stat(filepath.Join(path, version+".mod")); err != nil {
		t.Error(err)
		t.Fail()
	}

	if _, err := os.Stat(filepath.Join(path, version+".zip")); err != nil {
		t.Error(err)
		t.Fail()
	}

	if _, err := os.Stat(filepath.Join(path, version+".info")); err != nil {
		t.Error(err)
		t.Fail()
	}

	t.Log(path)
	err = fetcher.Clear()
	if err != nil {
		t.Error(err)
	}
}
