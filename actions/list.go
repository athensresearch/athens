package actions

import (
	"net/http"
	"path/filepath"
	"strings"

	"github.com/gobuffalo/buffalo"
	git "gopkg.in/src-d/go-git.v4"
)

func listHandler(c buffalo.Context) error {
	params, err := getStandardParams(c)
	if err != nil {
		return err
	}
	gitRepo, err := git.PlainOpen(filepath.Join(gopath, "src", params.baseURL, params.module))
	if err != nil {
		return err
	}
	tags, err := gitRepo.Tags()
	if err != nil {
		return err
	}
	ret := []string{}
	for {
		ref, err := tags.Next()
		if err != nil || ref == nil {
			break
		}
		ret = append(ret, ref.String())
	}
	c.Render(http.StatusOK, r.String(strings.Join(ret, "\n")))
}
