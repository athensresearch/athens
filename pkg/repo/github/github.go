// Package github is working version only
// We need to (re-)consider
// - multiple go.mod files in one repo e.g (owner/repo/cli/go.mod owner/repo/web/go.mod)
// - v2 (or any other vX) folder
// - something else which I cannot think of right now because I had too many cakes today
package github

import (
	"archive/tar"
	"compress/gzip"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"

	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/gomod/file"
	"github.com/gomods/athens/pkg/module"
	"github.com/gomods/athens/pkg/repo"
	"github.com/spf13/afero"
)

const (
	fetchRepoURI string = "https://api.github.com/repos/%s/%s/tarball/%s"
)

type gitFetcher struct {
	fs       afero.Fs
	owner    string
	repoName string
	tag      string
	dirName  string
}

// NewGitFetcher creates a new Fetcher for repositories hosted on github
func NewGitFetcher(fs afero.Fs, owner string, repoName string, tag string) (repo.Fetcher, error) {
	if owner == "" || repoName == "" {
		return nil, errors.New("invalid repository identifier")
	}

	gf := &gitFetcher{
		owner:    owner,
		repoName: repoName,
		tag:      tag,
		fs:       fs,
	}
	return gf, nil
}

// Fetches a tarball of a repo and untars it into a temp dir which is used later in the workflow.
func (g gitFetcher) Fetch() (string, error) {
	uri := fmt.Sprintf(fetchRepoURI, g.owner, g.repoName, g.tag)

	client := http.Client{Timeout: env.Timeout()}
	resp, err := client.Get(uri)

	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	tmpDir, err := afero.TempDir(g.fs, "", "")
	if err != nil {
		return "", err
	}
	g.dirName, err = untar(g.fs, resp.Body, tmpDir)
	if err != nil {
		g.fs.RemoveAll(tmpDir)
		return "", err
	}

	// Get module name from go.mod
	gomodPath := filepath.Join(g.dirName, "go.mod")
	parser := file.NewFileParser(g.fs, gomodPath)

	moduleName, err := parser.ModuleName()
	if err != nil {
		g.fs.RemoveAll(tmpDir)
		return g.dirName, err
	}

	// Generate zip
	if err := g.generateZip(moduleName); err != nil {
		g.fs.RemoveAll(tmpDir)
		return g.dirName, err
	}

	// Rename go.mod
	verModPath := filepath.Join(g.dirName, g.tag+".mod")
	if err := g.fs.Rename(gomodPath, verModPath); err != nil {
		g.fs.RemoveAll(tmpDir)
		return g.dirName, err
	}

	// Generate info
	if err := g.generateInfo(); err != nil {
		g.fs.RemoveAll(tmpDir)
		return g.dirName, err
	}

	return g.dirName, nil
}

// Clear removes all downloaded data
func (g *gitFetcher) Clear() error {
	if g.dirName == "" {
		return nil
	}

	return g.fs.RemoveAll(g.dirName)
}

func (g *gitFetcher) generateZip(moduleName string) error {
	zipReader := module.MakeZip(g.fs, g.dirName, moduleName, g.tag)

	zipPath := filepath.Join(g.dirName, g.tag+".zip")
	f, err := g.fs.OpenFile(zipPath, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, os.ModePerm)
	if err != nil {
		return err
	}
	_, err = io.Copy(f, zipReader)
	return err
}

func (g *gitFetcher) generateInfo() error {
	info, err := getCommitInfo(g.owner, g.repoName, g.tag)
	if err != nil {
		return err
	}

	infoContent, err := json.Marshal(info)
	if err != nil {
		return err
	}

	goinfoPath := filepath.Join(g.dirName, g.tag+".info")
	return afero.WriteFile(g.fs, goinfoPath, infoContent, os.ModePerm)
}

func untar(fs afero.Fs, content io.Reader, tmpDir string) (string, error) {
	gzr, err := gzip.NewReader(content)
	if err != nil {
		return "", err
	}
	defer gzr.Close()

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

			if _, err := fs.Stat(target); err != nil {
				if err := fs.MkdirAll(target, 0755); err != nil {
					return "", err
				}
			}

		case tar.TypeReg:
			f, err := fs.OpenFile(target, os.O_CREATE|os.O_RDWR, os.FileMode(hdr.Mode))
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
