package actions

import (
	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/eventlog"
	"github.com/gomods/athens/pkg/eventlog/mongo"
)

// GetEventLog returns implementation of eventlog.EventLog
func GetEventLog() (eventlog.Eventlog, error) {
	connectionString, err := env.MongoConnectionString()
	if err != nil {
		return nil, err
	}
	certPath := env.MongoCertPath()
	l, err := mongo.NewLog(connectionString, certPath)
	return l, err
}

// NewCacheMissesLog returns impl. of eventlog.Appender
func NewCacheMissesLog() (eventlog.Appender, error) {
	connectionString, err := env.MongoConnectionString()
	if err != nil {
		return nil, err
	}
	certPath := env.MongoCertPath()
	l, err := mongo.NewLogWithCollection(connectionString, certPath, "cachemisseslog")
	return l, err
}
