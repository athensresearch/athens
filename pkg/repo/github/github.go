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
	tag      string
}

// NewGitCrawler creates a new Crawler for repositories hosted on github
func NewGitCrawler(owner string, repoName string, tag string) (repo.Crawler, error) {
	if owner == "" || repoName == "" {
		return nil, errors.New("invalid repository identifier")
	}

	return &gitCrawler{
		owner:    owner,
		repoName: repoName,
		tag:      tag,
	}, nil
}

// Fetches a tarball of a repo and untars it into a temp dir which is used later in the workflow.
func (g gitCrawler) DownloadRepo() (string, error) {
	uri := fmt.Sprintf(fetchRepoURI, g.owner, g.repoName, g.tag)

	resp, err := http.Get(uri)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	tmpDir := os.TempDir()
	dirName, err := untar(resp.Body, tmpDir)
	if err != nil {
		os.Remove(tmpDir)
		return "", err
	}

	return dirName, nil
}

func untar(content io.Reader, tmpDir string) (string, error) {
	gzr, err := gzip.NewReader(content)
	defer gzr.Close()
	if err != nil {
		return "", err
	}
	tr := tar.NewReader(gzr)
	var dirName string

	for {
		hdr, err := tr.Next()

		if err != nil {
			if err == io.EOF {
				break
			}

			return "", err
		}

		if hdr == nil {
			continue
		}

		target := filepath.Join(tmpDir, hdr.Name)

		switch hdr.Typeflag {

		case tar.TypeDir:
			if dirName == "" {
				dirName = target
			}

			if _, err := os.Stat(target); err != nil {
				if err := os.MkdirAll(target, 0755); err != nil {
					return "", err
				}
			}

		case tar.TypeReg:
			f, err := os.OpenFile(target, os.O_CREATE|os.O_RDWR, os.FileMode(hdr.Mode))
			if err != nil {
				return "", err
			}
			defer f.Close()

			if _, err := io.Copy(f, tr); err != nil {
				return "", err
			}
		}
	}

	return dirName, nil
}
