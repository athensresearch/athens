package gwa

import (
	"context"
	"time"

	"github.com/gobuffalo/buffalo/worker"
	"github.com/gocraft/work"
	"github.com/gomodule/redigo/redis"
	"github.com/markbates/going/defaults"
	"github.com/pkg/errors"
	"github.com/sirupsen/logrus"
)

// Options describes the adapter configuration.
type Options struct {
	*redis.Pool
	Logger         Logger
	Name           string
	MaxConcurrency int
}

var _ worker.Worker = &Adapter{}

// New constructs a new adapter.
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

// Adapter adapts gocraft/work to use with buffalo.
type Adapter struct {
	Enqueur *work.Enqueuer
	Pool    *work.WorkerPool
	Logger  Logger
	ctx     context.Context
}

// Start starts the adapter event loop.
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

// Stop stops the adapter event loop.
func (q *Adapter) Stop() error {
	q.Logger.Info("Stopping gocraft/work Worker")
	q.Pool.Stop()
	return nil
}

// Register binds a new job, with a name and a handler.
func (q *Adapter) Register(name string, h worker.Handler) error {
	q.Pool.Job(name, func(job *work.Job) error {
		return h(job.Args)
	})
	return nil
}

// RegisterWithOptions binds a new job, with a name, options and a handler.
func (q *Adapter) RegisterWithOptions(name string, opts work.JobOptions, h worker.Handler) error {
	q.Pool.JobWithOptions(name, opts, func(job *work.Job) error {
		return h(job.Args)
	})
	return nil
}

// Perform sends a new job to the queue, now.
func (q Adapter) Perform(job worker.Job) error {
	q.Logger.Infof("Enqueuing job %s\n", job)
	_, err := q.Enqueur.Enqueue(job.Handler, job.Args)
	if err != nil {
		q.Logger.Errorf("error enqueuing job %s", job)
		return errors.WithStack(err)
	}
	return nil
}

// PerformIn sends a new job to the queue, with a given delay.
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

// PerformAt sends a new job to the queue, with a given start time.
func (q Adapter) PerformAt(job worker.Job, t time.Time) error {
	return q.PerformIn(job, t.Sub(time.Now()))
}
