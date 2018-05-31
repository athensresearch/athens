package grifts

import (
	"github.com/markbates/grift/grift"
)

var _ = grift.Namespace("eventlog-sync", func() {

	grift.Desc("eventlog-sync", "Runs continuously to sync event logs from 1 or more other Olympus server")
	grift.Add("eventlog-sync", func(c *grift.Context) error {
		// TODO: add event log syncing here
		return nil
	})

})
