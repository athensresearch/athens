package actions

import (
	"context"
	"encoding/json"
	"testing"
	"time"

	"github.com/gobuffalo/buffalo/worker"
	"github.com/gobuffalo/suite"
	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/eventlog"
	"github.com/gomods/athens/pkg/eventlog/mongo"
	"github.com/gomods/athens/pkg/payloads"
	"github.com/gomods/athens/pkg/storage/mem"
)

type ActionSuite struct {
	*suite.Action
}

type FakeWorker struct {
	job *worker.Job
}

func (f *FakeWorker) Start(ctx context.Context) error {
	return nil
}
func (f *FakeWorker) Stop() error {
	return nil
}
func (f *FakeWorker) Perform(j worker.Job) error {
	f.job = &j
	return nil
}
func (f *FakeWorker) PerformAt(j worker.Job, t time.Time) error {
	return nil
}
func (f *FakeWorker) PerformIn(j worker.Job, t time.Duration) error {
	return nil
}
func (f *FakeWorker) Register(n string, h worker.Handler) error {
	return nil
}

func Test_ActionSuite(t *testing.T) {
	worker := &FakeWorker{}
	stg, err := mem.NewStorage()
	if err != nil {
		t.Fatalf("error creating storage (%s)", err)
	}
	mURI, err := env.MongoURI()
	if err != nil {
		t.Fatalf("error getting mongo uri (%s)", err)
	}
	eLog, err := mongo.NewLog(mURI)
	if err != nil {
		t.Fatalf("error creating event log (%s)", err)
	}
	config := AppConfig{
		Storage:        stg,
		EventLog:       eLog,
		CacheMissesLog: eLog,
		Worker:         worker,
	}
	app := App(&config)
	as := &ActionSuite{suite.NewAction(app)}
	suite.Run(t, as)
}

func (as *ActionSuite) Test_Cache_Miss_Route() {
	mod := &payloads.Module{}
	mod.Name = "moduleName"
	mod.Version = "1.0.0"
	res := as.JSON("/cachemiss").Post(mod)

	fakeWorker, ok := as.App.Worker.(*FakeWorker)
	as.True(ok)
	module, ok := fakeWorker.job.Args[workerModuleKey].(string)
	as.True(ok)
	version, ok := fakeWorker.job.Args[workerVersionKey].(string)
	as.True(ok)

	as.Equal("moduleName", module)
	as.Equal("1.0.0", version)
	as.Equal(200, res.Code)
}

func (as *ActionSuite) Test_Push_Notification_Route() {
	p := &payloads.PushNotification{}
	p.OriginURL = "https://mycdn.com/"
	e := eventlog.Event{ID: "1", Module: "mymod", Version: "1.0.0", Time: time.Now(), Op: eventlog.OpAdd}
	p.Events = []eventlog.Event{e}
	res := as.JSON("/push").Post(p)
	as.Equal(200, res.Code)
	fakeWorker, ok := as.App.Worker.(*FakeWorker)
	as.True(ok)
	pnJSON, ok := fakeWorker.job.Args[workerPushNotificationKey].(string)
	as.True(ok)
	pn := &payloads.PushNotification{}
	b := []byte(pnJSON)
	json.Unmarshal(b, pn)

	as.Equal(p.OriginURL, pn.OriginURL)
	as.Equal(p.Events[0].Module, pn.Events[0].Module)
	as.Equal(p.Events[0].Version, pn.Events[0].Version)
}

// TODO: something like this to test Push_Notification_Job handler after mergeDB is completed

//func (as *ActionSuite) Test_Push_Notification_Job() {
//	storage, err := mem.NewStorage()
//	as.NoError(err)
//	eLog, err := mongo.NewLog("mongodb://127.0.0.1:27017")
//	pushHandler := GetProcessPushNotificationJob(storage, eLog)
//
//	p := &payloads.PushNotification{}
//	p.OriginURL = "https://mycdn.com/"
//	e := eventlog.Event{ID: "1", Module: "mymod", Version: "1.0.0", Time: time.Now(), Op: eventlog.OpAdd}
//	p.Events = []eventlog.Event{e}
//	pj, err := json.Marshal(p)
//	as.NoError(err)
//	args := worker.Args{
//		workerPushNotificationKey: string(pj),
//	}
//	pushHandler(args)
//
//	events, err := eLog.Read()
//	as.NoError(err)
//	as.Equal(1, len(events))
//}
