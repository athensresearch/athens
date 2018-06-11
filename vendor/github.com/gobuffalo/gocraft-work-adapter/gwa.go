package gwa

import (
	"context"
	"time"

	"github.com/garyburd/redigo/redis"
	"github.com/gobuffalo/buffalo/worker"
	"github.com/gocraft/work"
	"github.com/markbates/going/defaults"
	"github.com/pkg/errors"
	"github.com/sirupsen/logrus"
)

type Options struct {
	*redis.Pool
	Logger         Logger
	Name           string
	MaxConcurrency int
}

var _ worker.Worker = &Adapter{}

func New(opts Options) *Adapter {
	ctx := context.Background()

	opts.Name = defaults.String(opts.Name, "buffalo")
	enqueuer := work.NewEnqueuer(opts.Name, opts.Pool)

	opts.MaxConcurrency = defaults.Int(opts.MaxConcurrency, 25)
	pool := work.NewWorkerPool(struct{}{}, uint(opts.MaxConcurrency), opts.Name, opts.Pool)

	if opts.Logger == nil {
		l := logrus.New()
		l.Level = logrus.InfoLevel
		l.Formatter = &logrus.TextFormatter{}
		opts.Logger = l
	}

	return &Adapter{
		Enqueur: enqueuer,
		Pool:    pool,
		Logger:  opts.Logger,
		ctx:     ctx,
	}
}

type Adapter struct {
	Enqueur *work.Enqueuer
	Pool    *work.WorkerPool
	Logger  Logger
	ctx     context.Context
}

func (q *Adapter) Start(ctx context.Context) error {
	q.Logger.Info("Starting gocraft/work Worker")
	q.ctx = ctx
	go func() {
		select {
		case <-ctx.Done():
			q.Stop()
		}
	}()
	q.Pool.Start()
	return nil
}

func (q *Adapter) Stop() error {
	q.Logger.Info("Stopping gocraft/work Worker")
	q.Pool.Stop()
	return nil
}

func (q *Adapter) Register(name string, h worker.Handler) error {
	q.Pool.Job(name, func(job *work.Job) error {
		return h(job.Args)
	})
	return nil
}

func (q Adapter) Perform(job worker.Job) error {
	q.Logger.Infof("Enqueuing job %s\n", job)
	_, err := q.Enqueur.Enqueue(job.Handler, job.Args)
	if err != nil {
		q.Logger.Errorf("error enqueuing job %s", job)
		return errors.WithStack(err)
	}
	return nil
}

func (q Adapter) PerformIn(job worker.Job, t time.Duration) error {
	q.Logger.Infof("Enqueuing job %s\n", job)
	d := int64(t / time.Second)
	_, err := q.Enqueur.EnqueueIn(job.Handler, d, job.Args)
	if err != nil {
		q.Logger.Errorf("error enqueuing job %s", job)
		return errors.WithStack(err)
	}
	return nil
}

func (q Adapter) PerformAt(job worker.Job, t time.Time) error {
	return q.PerformIn(job, t.Sub(time.Now()))
}
