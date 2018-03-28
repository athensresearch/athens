package actions

import (
	"github.com/gobuffalo/buffalo"
)

func getSyncPoint(c buffalo.Context) (string, error) {
	syncpoint := c.Param("syncpoint")
	if syncpoint == "" {
		return "", nil
	}
	return syncpoint, nil
}
