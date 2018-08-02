package module

import (
	"bytes"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"

	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/paths"
	"github.com/spf13/afero"
)

type goGetFetcher struct {
	fs           afero.Fs
	goBinaryName string
}

// NewGoGetFetcher creates fetcher which uses go get tool to fetch modules
func NewGoGetFetcher(goBinaryName string, fs afero.Fs) (Fetcher, error) {
	const op errors.Op = "module.NewGoGetFetcher"
	if err := validGoBinary(goBinaryName); err != nil {
		return nil, errors.E(op, err)
	}
	return &goGetFetcher{
		fs:           fs,
		goBinaryName: goBinaryName,
	}, nil
}

// Fetch downloads the sources and returns path where it can be found. Make sure to call Clear
// on the returned Ref when you are done with it
func (g *goGetFetcher) Fetch(mod, ver string) (Ref, error) {
	const op errors.Op = "goGetFetcher.Fetch"
	// setup the GOPATH
	goPathRoot, err := afero.TempDir(g.fs, "", "athens")
	if err != nil {
		return nil, errors.E(op, err)
	}
	sourcePath := filepath.Join(goPathRoot, "src")
	modPath := filepath.Join(sourcePath, getRepoDirName(mod, ver))
	if err := g.fs.MkdirAll(modPath, os.ModeDir|os.ModePerm); err != nil {
		clearFiles(g.fs, goPathRoot)
		return nil, errors.E(op, err)
	}

	// setup the module with barebones stuff
	if err := Dummy(g.fs, modPath); err != nil {
		clearFiles(g.fs, goPathRoot)
		return nil, errors.E(op, err)
	}

	err = getSources(g.goBinaryName, g.fs, goPathRoot, modPath, mod, ver)
	if err != nil {
		clearFiles(g.fs, goPathRoot)
		return nil, errors.E(op, err)
	}

	return newDiskRef(g.fs, goPathRoot, mod, ver), nil
}

// Dummy Hacky thing makes vgo not to complain
func Dummy(fs afero.Fs, repoRoot string) error {
	const op errors.Op = "module.Dummy"
	// vgo expects go.mod file present with module statement or .go file with import comment
	gomodPath := filepath.Join(repoRoot, "go.mod")
	gomodContent := []byte("module mod")
	if err := afero.WriteFile(fs, gomodPath, gomodContent, 0666); err != nil {
		return errors.E(op, err)
	}
	sourcePath := filepath.Join(repoRoot, "mod.go")
	sourceContent := []byte("package mod")
	if err := afero.WriteFile(fs, sourcePath, sourceContent, 0666); err != nil {
		return errors.E(op, err)
	}
	return nil
}

// given a filesystem, gopath, repository root, module and version, runs 'vgo get'
// on module@version from the repoRoot with GOPATH=gopath, and returns a non-nil error if anything went wrong.
func getSources(goBinaryName string, fs afero.Fs, gopath, repoRoot, module, version string) error {
	const op errors.Op = "module.getSources"
	uri := strings.TrimSuffix(module, "/")

	fullURI := fmt.Sprintf("%s@%s", uri, version)

	gopathEnv := fmt.Sprintf("GOPATH=%s", gopath)
	cacheEnv := fmt.Sprintf("GOCACHE=%s", filepath.Join(gopath, "cache"))
	disableCgo := "CGO_ENABLED=0"
	enableGoModules := "GO111MODULE=on"

	cmd := exec.Command(goBinaryName, "get", fullURI)
	// PATH is needed for vgo to recognize vcs binaries
	// this breaks windows.
	cmd.Env = []string{"PATH=" + os.Getenv("PATH"), gopathEnv, cacheEnv, disableCgo, enableGoModules}
	cmd.Dir = repoRoot
	o, err := cmd.CombinedOutput()
	if err != nil {
		errMsg := fmt.Sprintf("%v : %s", err, o)
		// github quota exceeded
		if isLimitHit(o) {
			return errors.E(op, errMsg, errors.KindRateLimit)
		}
		// another error in the output
		return errors.E(op, errMsg)
	}
	// make sure the expected files exist
	encmod, err := paths.EncodePath(module)
	if err != nil {
		return errors.E(op, err)
	}
	packagePath := getPackagePath(gopath, encmod)
	err = checkFiles(fs, packagePath, version)
	if err != nil {
		return errors.E(op, err)
	}

	return nil
}

func checkFiles(fs afero.Fs, path, version string) error {
	const op errors.Op = "module.checkFiles"
	if _, err := fs.Stat(filepath.Join(path, version+".mod")); err != nil {
		return errors.E(op, fmt.Sprintf("%s.mod not found in %s", version, path))
	}

	if _, err := fs.Stat(filepath.Join(path, version+".zip")); err != nil {
		return errors.E(op, fmt.Sprintf("%s.mod not found in %s", version, path))
	}

	if _, err := fs.Stat(filepath.Join(path, version+".info")); err != nil {
		return errors.E(op, fmt.Sprintf("%s.mod not found in %s", version, path))
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

// getPackagePath returns the path to the module cache given the gopath and module name
func getPackagePath(gopath, module string) string {
	return filepath.Join(gopath, "src", "mod", "cache", "download", module, "@v")
}

func validGoBinary(name string) error {
	const op errors.Op = "module.validGoBinary"
	err := exec.Command(name).Run()
	_, ok := err.(*exec.ExitError)
	if err != nil && !ok {
		return errors.E(op, err)
	}
	return nil
}
