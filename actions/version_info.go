package actions

import (
	"fmt"
	"net/http"

	"github.com/arschles/vgoprox/pkg/storage"
	"github.com/gobuffalo/buffalo"
)

func versionInfoHandler(versioner storage.Versioner) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		stdParams, err := getStandardParams(c)
		if err != nil {
			return err
		}
		version := c.Param("version")
		if version == "" {
			return fmt.Errorf("version not found")
		}
		revInfo, err := versioner.Info(stdParams.baseURL, stdParams.module, version)
		if err != nil {
			return err
		}
		return c.Render(http.StatusOK, r.JSON(revInfo))
	}
}
