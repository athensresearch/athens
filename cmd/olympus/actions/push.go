package actions

import (
	"context"
	"encoding/json"
	"errors"

	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/worker"
	"github.com/gomods/athens/pkg/eventlog"
	"github.com/gomods/athens/pkg/payloads"
	"github.com/gomods/athens/pkg/storage"
)

func pushNotificationHandler(w worker.Worker) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		p := &payloads.PushNotification{}
		if err := c.Bind(p); err != nil {
			return err
		}
		pj, err := json.Marshal(p)
		if err != nil {
			return err
		}

		return w.Perform(worker.Job{
			Queue:   workerQueue,
			Handler: PushNotificationHandlerName,
			Args: worker.Args{
				workerPushNotificationKey: string(pj),
			},
		})
	}
}

// GetProcessPushNotificationJob processes queue of push notifications
func GetProcessPushNotificationJob(storage storage.Backend, eLog eventlog.Eventlog) worker.Handler {
	return func(args worker.Args) (err error) {
		// TODO: background for now
		ctx := context.Background()
		pn, err := parsePushNotificationJobArgs(args)
		if err != nil {
			return err
		}
		diff, err := buildDiff(pn.Events)
		if err != nil {
			return err
		}
		return mergeDB(ctx, pn.OriginURL, *diff, eLog, storage)
	}
}

func parsePushNotificationJobArgs(args worker.Args) (*payloads.PushNotification, error) {
	pn, ok := args[workerPushNotificationKey].(string)
	if !ok {
		return nil, errors.New("push notification not found")
	}
	p := &payloads.PushNotification{}
	b := []byte(pn)
	if err := json.Unmarshal(b, p); err != nil {
		return nil, err
	}
	return p, nil
}
