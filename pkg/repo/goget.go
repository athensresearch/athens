package repo

import (
	"bytes"
	"errors"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"

	"github.com/spf13/afero"
)

const (
	tmpRepoDir = "%s-%s" // owner-repo-ref
)

var (
	// ErrLimitExceeded signals that github.com refused to serve the request due to exceeded quota
	ErrLimitExceeded = errors.New("github limit exceeded")
)

type genericFetcher struct {
	fs      afero.Fs
	repoURI string
	version string
	dirName string
}

// NewGenericFetcher creates fetcher which uses go get tool to fetch sources
// returns path of directory containing vx.y.z.(zip|info|mod)
func NewGenericFetcher(fs afero.Fs, repoURI, version string) (Fetcher, error) {
	if !isVgoInstalled() {
		return nil, errors.New("vgo not installed")
	}

	if repoURI == "" {
		return nil, errors.New("invalid repository identifier")
	}

	return &genericFetcher{
		fs:      fs,
		repoURI: repoURI,
		version: version,
	}, nil
}

// Fetch downloads the sources and returns path where it can be found
func (g *genericFetcher) Fetch() (string, error) {
	escapedURI := strings.Replace(g.repoURI, "/", "-", -1)
	repoDirName := fmt.Sprintf(tmpRepoDir, escapedURI, g.version)

	gopath, repoRoot, err := setupTmp(g.fs, repoDirName)
	if err != nil {
		return "", err
	}
	g.dirName = repoRoot

	prepareStructure(g.fs, repoRoot)

	dirName, err := getSources(g.fs, gopath, repoRoot, g.repoURI, g.version)

	return dirName, err
}

// Clear removes all downloaded data
func (g *genericFetcher) Clear() error {
	if g.dirName == "" {
		return nil
	}

	return g.fs.RemoveAll(g.dirName)
}

func isVgoInstalled() bool {
	cmd := exec.Command("vgo")
	err := cmd.Run() // this will fail even if vgo is installed

	if err != nil {
		_, ok := err.(*exec.ExitError) // if vgo is installed it will return ExitError, otherwise normal error
		return ok == true
	}

	return false
}

func setupTmp(fs afero.Fs, repoDirName string) (string, string, error) {
	gopathDir, err := afero.TempDir(fs, "", "")
	if err != nil {
		return "", "", err
	}

	path := filepath.Join(gopathDir, "src", repoDirName)

	return gopathDir, path, fs.MkdirAll(path, os.ModeDir|os.ModePerm)
}

// Hacky thing makes vgo not to complain
func prepareStructure(fs afero.Fs, repoRoot string) error {
	// vgo expects go.mod file present with module statement or .go file with import comment
	gomodPath := filepath.Join(repoRoot, "go.mod")
	gomodContent := []byte("module \"mod\"")
	if err := afero.WriteFile(fs, gomodPath, gomodContent, 0666); err != nil {
		return err
	}

	sourcePath := filepath.Join(repoRoot, "mod.go")
	sourceContent := []byte(`package mod // import "mod"`)
	return afero.WriteFile(fs, sourcePath, sourceContent, 0666)
}

func getSources(fs afero.Fs, gopath, repoRoot, repoURI, version string) (string, error) {
	version = strings.TrimPrefix(version, "@")
	if !strings.HasPrefix(version, "v") {
		version = "v" + version
	}
	uri := strings.TrimSuffix(repoURI, "/")

	fullURI := fmt.Sprintf("%s@%s", uri, version)

	gopathEnv := fmt.Sprintf("GOPATH=%s", gopath)
	disableCgo := "CGO_ENABLED=0"

	cmd := exec.Command("vgo", "get", fullURI)
	cmd.Env = os.Environ()
	cmd.Env = append(cmd.Env, gopathEnv, disableCgo)
	cmd.Dir = repoRoot

	packagePath := filepath.Join(gopath, "src", "mod", "cache", repoURI, "@v")

	o, err := cmd.CombinedOutput()
	if err != nil {
		switch {
		case isLimitHit(o):
			// github quota exceeded
			return packagePath, ErrLimitExceeded
		case checkFiles(fs, packagePath, version) == nil:
			// some compilation error
			return packagePath, nil
		default:
			return packagePath, err
		}
	}

	return packagePath, err
}

func checkFiles(fs afero.Fs, path, version string) error {
	if _, err := fs.Stat(filepath.Join(path, version+".mod")); err != nil {
		return errors.New("go.mod not found")
	}

	if _, err := fs.Stat(filepath.Join(path, version+".zip")); err != nil {
		return errors.New("zip package not found")
	}

	if _, err := fs.Stat(filepath.Join(path, version+".info")); err != nil {
		return errors.New("info file not found")
	}

	return nil
}

func isLimitHit(o []byte) bool {
	return bytes.Contains(o, []byte("403 response from api.github.com"))
}
