package log

import (
	"bytes"
	"fmt"
	"strings"
	"testing"
	"time"

	"github.com/stretchr/testify/require"

	"github.com/sirupsen/logrus"
)

type input struct {
	name          string
	cloudProvider string
	level         logrus.Level
	fields        logrus.Fields
	logFunc       func(e Entry)
	output        string
}

var testCases = []input{
	{
		name:          "gcp_debug",
		cloudProvider: "GCP",
		level:         logrus.DebugLevel,
		fields:        logrus.Fields{},
		logFunc:       func(e Entry) { e.Infof("info message") },
		output:        `{"message":"info message","severity":"info","timestamp":"%v"}` + "\n",
	},
	{
		name:          "gcp_error",
		cloudProvider: "GCP",
		level:         logrus.DebugLevel,
		fields:        logrus.Fields{},
		logFunc:       func(e Entry) { e.Errorf("err message") },
		output:        `{"message":"err message","severity":"error","timestamp":"%v"}` + "\n",
	},
	{
		name:          "gcp_empty",
		cloudProvider: "GCP",
		level:         logrus.ErrorLevel,
		fields:        logrus.Fields{},
		logFunc:       func(e Entry) { e.Infof("info message") },
		output:        ``,
	},
	{
		name:          "gcp_fields",
		cloudProvider: "GCP",
		level:         logrus.DebugLevel,
		fields:        logrus.Fields{"field1": "value1", "field2": 2},
		logFunc:       func(e Entry) { e.Debugf("debug message") },
		output:        `{"field1":"value1","field2":2,"message":"debug message","severity":"debug","timestamp":"%v"}` + "\n",
	},
	{
		name:          "gcp_logs",
		cloudProvider: "GCP",
		level:         logrus.DebugLevel,
		fields:        logrus.Fields{},
		logFunc:       func(e Entry) { e.Warnf("warn message") },
		output:        `{"message":"warn message","severity":"warning","timestamp":"%v"}` + "\n",
	},
	{
		name:          "default",
		cloudProvider: "default",
		level:         logrus.DebugLevel,
		fields:        logrus.Fields{"xyz": "abc", "abc": "xyz"},
		logFunc:       func(e Entry) { e.Warnf("warn message") },
		output:        `{"abc":"xyz","level":"warning","msg":"warn message","time":"%v","xyz":"abc"}` + "\n",
	},
}

func TestCloudLogger(t *testing.T) {
	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			lggr := New(tc.cloudProvider, tc.level)
			var buf bytes.Buffer
			lggr.Out = &buf
			e := lggr.WithFields(tc.fields)
			tc.logFunc(e)
			out := buf.String()
			expected := tc.output
			if strings.Contains(expected, "%v") {
				expected = fmt.Sprintf(expected, time.Now().Format(time.RFC3339))
			}

			require.Equal(t, expected, out, "expected the logged entry to match the testCase output")
		})
	}
}
