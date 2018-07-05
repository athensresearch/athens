package actions

import (
	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/eventlog"
	"github.com/gomods/athens/pkg/eventlog/mongo"
)

// GetEventLog returns implementation of eventlog.EventLog
func GetEventLog() (eventlog.Eventlog, error) {
	mongoURI, err := env.MongoURI()
	if err != nil {
		return nil, err
	}
	l, err := mongo.NewLog(mongoURI)
	return l, err
}

func newCacheMissesLog() (eventlog.Appender, error) {
	mongoURI, err := env.MongoURI()
	if err != nil {
		return nil, err
	}
	l, err := mongo.NewLogWithCollection(mongoURI, "cachemisseslog")
	return l, err
}
