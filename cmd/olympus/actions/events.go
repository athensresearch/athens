package actions

import (
	"net/http"

	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/eventlog"
)

func eventlogHandler(r eventlog.Reader) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		seqID := c.Param("sequence_id")

		var events []eventlog.Event
		var err error
		if seqID == "" {
			events, err = r.Read()
		} else {
			events, err = r.ReadFrom(seqID)
		}
		if err != nil {
			return err
		}
		return c.Render(http.StatusOK, renderEng.JSON(events))
	}
}
