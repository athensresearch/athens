package log

import (
	"github.com/sirupsen/logrus"
)

func getGCPFormatter() logrus.Formatter {
	return &logrus.JSONFormatter{
		FieldMap: logrus.FieldMap{
			logrus.FieldKeyLevel: "severity",
			logrus.FieldKeyMsg:   "message",
			logrus.FieldKeyTime:  "timestamp",
		},
	}
}

func getDevFormatter() logrus.Formatter {
	return &logrus.TextFormatter{}
}

func getDefaultFormatter() logrus.Formatter {
	return &logrus.JSONFormatter{}
}
