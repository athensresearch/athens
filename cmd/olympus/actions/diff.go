package actions

import (
	"net/http"

	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/eventlog"
	"github.com/gomods/athens/pkg/storage"
)

type dbDiff struct {
	Added      []eventlog.Event `json:"added"`
	Deleted    []eventlog.Event `json:"deleted"`
	Deprecated []eventlog.Event `json:"deprecated"`
}

func diffHandler(stg storage.Backend, eLog eventlog.Reader) func(buffalo.Context) error {
	return func(c buffalo.Context) error {
		lastID := c.Param("lastID")
		events, err := eLog.ReadFrom(lastID)
		if err != nil {
			return err
		}

		diff, err := buildDiff(events)
		if err != nil {
			return err
		}
		return c.Render(http.StatusOK, renderEng.JSON(diff))
	}
}

func buildDiff(events []eventlog.Event) (*dbDiff, error) {
	ret := &dbDiff{}
	for _, evt := range events {
		if evt.Op == eventlog.OpAdd {
			ret.Added = append(ret.Added, evt)
		} else if evt.Op == eventlog.OpDel {
			ret.Deleted = append(ret.Deleted, evt)
		} else if evt.Op == eventlog.OpDep {
			ret.Deprecated = append(ret.Deprecated, evt)
		}
	}
	return ret, nil
}
