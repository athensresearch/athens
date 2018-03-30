package github

import (
	"os"
	"testing"
)

func Test_Download(t *testing.T) {
	owner := "bketelsen"
	repo := "captainhook"
	version := "v0.1.8"

	fetcher, err := NewGitCrawler(owner, repo, version)
	if err != nil {
		t.Error(err)
	}

	path, err := fetcher.DownloadRepo()
	if err != nil {
		t.Error(err)
	}
	if path == "" {
		t.Error("path null")
	}
	t.Log(path)
	os.RemoveAll(path)
}
