package actions

import (
	"fmt"
	"net/http"
	"os"
	"path/filepath"

	"github.com/bketelsen/buffet"
	"github.com/gobuffalo/buffalo"
	parser "github.com/gomods/athens/pkg/gomod/file"
	"github.com/gomods/athens/pkg/module"
	"github.com/gomods/athens/pkg/repo/github"
	"github.com/gomods/athens/pkg/storage"
	"github.com/pkg/errors"
	"github.com/spf13/afero"
)

// /admin/fetch/{module:[a-zA-Z./]+}/{owner}/{repo}/{ref}/{version}
func fetchHandler(store storage.Saver) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		sp := buffet.SpanFromContext(c)
		sp.SetOperationName("fetchHandler")
		owner := c.Param("owner")
		repo := c.Param("repo")
		ref := c.Param("ref")
		version := c.Param("version")
		fs := afero.NewOsFs()

		git, err := github.NewGitFetcher(fs, owner, repo, ref)
		if err != nil {
			return err
		}

		path, err := git.Fetch()
		if err != nil {
			os.Remove(path)
			return err
		}

		modFilePath := filepath.Join(path, "go.mod")
		modBytes, err := afero.ReadFile(fs, modFilePath)
		if err != nil {
			return fmt.Errorf("couldn't find go.mod file (%s)", err)
		}

		gomodParser := parser.NewFileParser(fs, modFilePath)
		moduleName, err := gomodParser.ModuleName()
		if err != nil {
			return fmt.Errorf("couldn't parse go.mod file (%s)", err)
		}

		zipBytes := module.MakeZip(fs, path, moduleName, version)

		infoFilePath := filepath.Join(path, version+".info")
		infoBytes, err := afero.ReadFile(fs, infoFilePath)
		if err != nil {
			return fmt.Errorf("coudln't find .info file (%s)", err)
		}

		saveErr := store.Save(c, moduleName, version, modBytes, zipBytes, infoBytes)
		if storage.IsVersionAlreadyExistsErr(saveErr) {
			return c.Error(http.StatusConflict, saveErr)
		} else if err != nil {
			return errors.WithStack(err)
		}

		return c.Render(http.StatusOK, proxy.String(err.Error()))
	}
}
