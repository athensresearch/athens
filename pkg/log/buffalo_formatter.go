package log

import (
	"fmt"

	"github.com/fatih/color"
	"github.com/sirupsen/logrus"
)

type buffaloFormatter struct{}

func (buffaloFormatter) Format(entry *logrus.Entry) ([]byte, error) {
	if entry.Level == logrus.ErrorLevel {
		// buffalo does not pass request params when an error occurs: pass params
		// when https://github.com/gobuffalo/buffalo/issues/1171 is resolved.
		return fmtBuffaloErr(entry.Message), nil
	}

	statusCode, _ := entry.Data["status"].(int)
	status := fmt.Sprint(statusCode)
	switch {
	case statusCode < 400:
		status = color.GreenString("%v", status)
	case statusCode >= 400 && statusCode < 500:
		status = color.HiYellowString("%v", status)
	default:
		status = color.HiRedString("%v", status)
	}
	method, hasMethod := entry.Data["method"].(string)
	path, hasPath := entry.Data["path"].(string)
	if !hasMethod || !hasPath {
		return fmtBuffaloMsg(entry.Message), nil
	}

	return fmtBuffaloRequest(method, path, status), nil
}

func fmtBuffaloRequest(method, path, status string) []byte {
	return []byte(fmt.Sprintf(
		"%v %v %v [%v]\n",
		color.CyanString("handler:"),
		method,
		path,
		status,
	))
}

func fmtBuffaloMsg(msg string) []byte {
	return []byte(fmt.Sprintf("%s %s\n", color.CyanString("buffalo:"), msg))
}

func fmtBuffaloErr(msg string) []byte {
	return []byte(fmt.Sprintf("%s %s\n", color.HiRedString("buffalo:"), msg))
}
