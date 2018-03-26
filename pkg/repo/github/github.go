package github

import (
	"archive/tar"
	"compress/gzip"
	"errors"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"

	"github.com/gomods/athens/pkg/repo"
)

const (
	fetchRepoURI string = "https://api.github.com/repos/%s/%s/tarball/%s"
	tmpFileName         = "%s-%s-%s" // owner-repo-ref
)

type gitCrawler struct {
	owner    string
	repoName string
	ref      string
}

// NewGitCrawler creates a new Crawler for repositories hosted on github
func NewGitCrawler(owner string, repoName string, ref string) (repo.Crawler, error) {
	if owner == "" || repoName == "" {
		return nil, errors.New("invalid repository identifier")
	}

	return &gitCrawler{
		owner:    owner,
		repoName: repoName,
		ref:      ref,
	}, nil
}

// Fetches a tarball of a repo and untars it into a temp dir which is used later in the workflow.
func (g gitCrawler) DownloadRepo() (string, error) {
	uri := fmt.Sprintf(fetchRepoURI, g.owner, g.repoName, g.ref)

	resp, err := http.Get(uri)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	tmpSubDir := fmt.Sprintf(tmpFileName, g.owner, g.repoName, g.ref)
	tmpDir := filepath.Join(os.TempDir(), tmpSubDir)
	if err := untar(resp.Body, tmpDir); err != nil {
		os.Remove(tmpDir)
		return "", err
	}

	return tmpDir, nil
}

func untar(content io.Reader, tmpDir string) error {
	gzr, err := gzip.NewReader(content)
	defer gzr.Close()
	if err != nil {
		return err
	}
	tr := tar.NewReader(gzr)

	for {
		hdr, err := tr.Next()
		switch {
		case err == io.EOF:
			break

		case err != nil:
			return err

		case hdr == nil:
			continue
		}

		target := filepath.Join(tmpDir, hdr.Name)

		switch hdr.Typeflag {

		case tar.TypeDir:
			if _, err := os.Stat(target); err != nil {
				if err := os.MkdirAll(target, 0755); err != nil {
					return err
				}
			}

		case tar.TypeReg:
			f, err := os.OpenFile(target, os.O_CREATE|os.O_RDWR, os.FileMode(hdr.Mode))
			if err != nil {
				return err
			}
			defer f.Close()

			if _, err := io.Copy(f, tr); err != nil {
				return err
			}
		}
	}
}
