package actions

import (
	"fmt"

	"github.com/gobuffalo/envy"
	"github.com/gomods/athens/pkg/eventlog"
	"github.com/gomods/athens/pkg/eventlog/mongo"
)

// GetEventLog returns implementation of eventlog.EventLog
func GetEventLog() (eventlog.Eventlog, error) {
	mongoURI, err := envy.MustGet("ATHENS_MONGO_STORAGE_URL")
	if err != nil {
		return nil, fmt.Errorf("missing mongo URL (%s)", err)
	}
	l, err := mongo.NewLog(mongoURI)
	return l, err
}

func newCacheMissesLog() (eventlog.Appender, error) {
	mongoURI, err := envy.MustGet("ATHENS_MONGO_STORAGE_URL")
	if err != nil {
		return nil, fmt.Errorf("missing mongo URL (%s)", err)
	}
	l, err := mongo.NewLogWithCollection(mongoURI, "cachemisseslog")
	return l, err
}
