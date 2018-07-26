package module

import (
	"bytes"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"

	"github.com/gomods/athens/pkg/errors"
	pkgerrors "github.com/pkg/errors"
	"github.com/spf13/afero"
)

type goGetFetcher struct {
	fs           afero.Fs
	goBinaryName string
}

// NewGoGetFetcher creates fetcher which uses go get tool to fetch modules
func NewGoGetFetcher(goBinaryName string, fs afero.Fs) (Fetcher, error) {
	return &goGetFetcher{
		fs:           fs,
		goBinaryName: goBinaryName,
	}, nil
}

// Fetch downloads the sources and returns path where it can be found. Make sure to call Clear
// on the returned Ref when you are done with it
func (g *goGetFetcher) Fetch(mod, ver string) (Ref, error) {
	ref := noopRef{}

	// setup the GOPATH
	goPathRoot, err := afero.TempDir(g.fs, "", "athens")
	if err != nil {
		// TODO: return a ref for cleaning up the goPathRoot
		// https://github.com/gomods/athens/issues/329
		ref.Clear()
		return ref, err
	}
	sourcePath := filepath.Join(goPathRoot, "src")
	modPath := filepath.Join(sourcePath, getRepoDirName(mod, ver))
	if err := g.fs.MkdirAll(modPath, os.ModeDir|os.ModePerm); err != nil {
		// TODO: return a ref for cleaning up the goPathRoot
		// https://github.com/gomods/athens/issues/329
		ref.Clear()
		return ref, err
	}

	// setup the module with barebones stuff
	if err := prepareStructure(g.fs, modPath); err != nil {
		// TODO: return a ref for cleaning up the goPathRoot
		// https://github.com/gomods/athens/issues/329
		ref.Clear()
		return ref, err
	}

	cachePath, err := getSources(g.goBinaryName, g.fs, goPathRoot, modPath, mod, ver)
	if err != nil {
		// TODO: return a ref that cleans up the goPathRoot
		// https://github.com/gomods/athens/issues/329
		ref.Clear()
		return nil, err
	}
	// TODO: make sure this ref also cleans up the goPathRoot
	// https://github.com/gomods/athens/issues/329
	return newDiskRef(g.fs, cachePath, ver), err
}

// Hacky thing makes vgo not to complain
func prepareStructure(fs afero.Fs, repoRoot string) error {
	// vgo expects go.mod file present with module statement or .go file with import comment
	gomodPath := filepath.Join(repoRoot, "go.mod")
	gomodContent := []byte("module mod")
	if err := afero.WriteFile(fs, gomodPath, gomodContent, 0666); err != nil {
		return err
	}

	sourcePath := filepath.Join(repoRoot, "mod.go")
	sourceContent := []byte("package mod")
	return afero.WriteFile(fs, sourcePath, sourceContent, 0666)
}

// given a filesystem, gopath, repository root, module and version, runs 'vgo get'
// on module@version from the repoRoot with GOPATH=gopath, and returns the location
// of the module cache. returns a non-nil error if anything went wrong. always returns
// the location of the module cache so you can delete it if necessary
func getSources(goBinaryName string, fs afero.Fs, gopath, repoRoot, module, version string) (string, error) {
	uri := strings.TrimSuffix(module, "/")

	fullURI := fmt.Sprintf("%s@%s", uri, version)

	gopathEnv := fmt.Sprintf("GOPATH=%s", gopath)
	cacheEnv := fmt.Sprintf("GOCACHE=%s", filepath.Join(gopath, "cache"))
	disableCgo := "CGO_ENABLED=0"

	cmd := exec.Command(goBinaryName, "get", fullURI)
	// PATH is needed for vgo to recognize vcs binaries
	// this breaks windows.
	cmd.Env = []string{"PATH=" + os.Getenv("PATH"), gopathEnv, cacheEnv, disableCgo}
	cmd.Dir = repoRoot

	packagePath := filepath.Join(gopath, "src", "mod", "cache", "download", module, "@v")

	o, err := cmd.CombinedOutput()
	if err != nil {
		// github quota exceeded
		if isLimitHit(o) {
			return packagePath, errors.E("module.getSources", err, errors.KindRateLimit)
		}
		// another error in the output
		return packagePath, err
	}
	// make sure the expected files exist
	return packagePath, checkFiles(fs, packagePath, version)
}

func checkFiles(fs afero.Fs, path, version string) error {
	if _, err := fs.Stat(filepath.Join(path, version+".mod")); err != nil {
		return pkgerrors.WithMessage(err, fmt.Sprintf("%s.mod not found in %s", version, path))
	}

	if _, err := fs.Stat(filepath.Join(path, version+".zip")); err != nil {
		return pkgerrors.WithMessage(err, fmt.Sprintf("%s.zip not found in %s", version, path))
	}

	if _, err := fs.Stat(filepath.Join(path, version+".info")); err != nil {
		return pkgerrors.WithMessage(err, fmt.Sprintf("%s.info not found in %s", version, path))
	}

	return nil
}

func isLimitHit(o []byte) bool {
	return bytes.Contains(o, []byte("403 response from api.github.com"))
}

// getRepoDirName takes a raw repository URI and a version and creates a directory name that the
// repository contents can be put into
func getRepoDirName(repoURI, version string) string {
	escapedURI := strings.Replace(repoURI, "/", "-", -1)
	return fmt.Sprintf("%s-%s", escapedURI, version)
}
