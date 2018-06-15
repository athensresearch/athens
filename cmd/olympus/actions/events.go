package actions

import (
	"net/http"

	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/worker"
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

func cachemissHandler(l eventlog.Appender, w worker.Worker) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		cm := &payloads.Module{}
		if err := c.Bind(cm); err != nil {
			return err
		}

		return w.Perform(worker.Job{
			Queue:   workerQueue,
			Handler: DownloadWorkerName,
			Args: worker.Args{
				workerModuleKey:  cm.Name,
				workerVersionKey: cm.Version,
			},
		})
	}
}
