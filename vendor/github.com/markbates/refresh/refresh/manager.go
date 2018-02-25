package refresh

import (
	"context"
	"fmt"
	"log"
	"os"
	"os/exec"
	"strings"
	"sync"
	"time"

	"github.com/fsnotify/fsnotify"
	"github.com/gobuffalo/envy"
)

type Manager struct {
	*Configuration
	Logger     *Logger
	Restart    chan bool
	gil        *sync.Once
	ID         string
	context    context.Context
	cancelFunc context.CancelFunc
}

func New(c *Configuration) *Manager {
	return NewWithContext(c, context.Background())
}

func NewWithContext(c *Configuration, ctx context.Context) *Manager {
	ctx, cancelFunc := context.WithCancel(ctx)
	m := &Manager{
		Configuration: c,
		Logger:        NewLogger(c),
		Restart:       make(chan bool),
		gil:           &sync.Once{},
		ID:            ID(),
		context:       ctx,
		cancelFunc:    cancelFunc,
	}
	return m
}

func (r *Manager) Start() error {
	w := NewWatcher(r)
	w.Start()
	go r.build(fsnotify.Event{Name: ":start:"})
	if !r.Debug {
		go func() {
			for {
				select {
				case event := <-w.Events:
					if event.Op != fsnotify.Chmod {
						go r.build(event)
					}
					w.Remove(event.Name)
					w.Add(event.Name)
				case <-r.context.Done():
					break
				}
			}
		}()
	}
	go func() {
		for {
			select {
			case err := <-w.Errors:
				r.Logger.Error(err)
			case <-r.context.Done():
				break
			}
		}
	}()
	r.runner()
	return nil
}

func (r *Manager) build(event fsnotify.Event) {
	r.gil.Do(func() {
		defer func() {
			r.gil = &sync.Once{}
		}()
		r.buildTransaction(func() error {
			// time.Sleep(r.BuildDelay * time.Millisecond)

			now := time.Now()
			r.Logger.Print("Rebuild on: %s", event.Name)
			cmd := exec.Command(envy.Get("GO_BIN", "go"), "build", "-v", "-i", "-o", r.FullBuildPath(), r.BuildTargetPath)
			err := r.runAndListen(cmd)
			if err != nil {
				if strings.Contains(err.Error(), "no buildable Go source files") {
					r.cancelFunc()
					log.Fatal(err)
				}
				return err
			}

			tt := time.Since(now)
			r.Logger.Success("Building Completed (PID: %d) (Time: %s)", cmd.Process.Pid, tt)
			r.Restart <- true
			return nil
		})
	})
}

func (r *Manager) buildTransaction(fn func() error) {
	lpath := ErrorLogPath()
	err := fn()
	if err != nil {
		f, _ := os.Create(lpath)
		fmt.Fprint(f, err)
		r.Logger.Error("Error!")
		r.Logger.Error(err)
	} else {
		os.Remove(lpath)
	}
}
