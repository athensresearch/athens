package actions

import (
	"net/http"
	"time"

	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/eventlog"
	"github.com/gomods/athens/pkg/payloads"
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

func cachemissHandler(l eventlog.Appender) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		cm := &payloads.Module{}
		if err := c.Bind(cm); err != nil {
			return err
		}
		e := eventlog.Event{Module: cm.Name, Version: cm.Version, Time: time.Now()}
		id, err := l.Append(e)
		if err != nil {
			return err
		}
		e.ID = id
		return c.Render(http.StatusOK, renderEng.JSON(e))
	}
}
