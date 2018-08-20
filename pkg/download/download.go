package download

import (
	"context"
	"io"
	"time"

	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/storage"
)

// Protocol is the download protocol which mirrors
// the http requests that cmd/go makes to the proxy.
type Protocol interface {
	// List implements GET /{module}/@v/list
	List(ctx context.Context, mod string) ([]string, error)

	// Info implements GET /{module}/@v/{version}.info
	Info(ctx context.Context, mod, ver string) ([]byte, error)

	// Latest implements GET /{module}/@latest
	Latest(ctx context.Context, mod string) (*storage.RevInfo, error)

	// GoMod implements GET /{module}/@v/{version}.mod
	GoMod(ctx context.Context, mod, ver string) ([]byte, error)

	// Zip implements GET /{module}/@v/{version}.zip
	Zip(ctx context.Context, mod, ver string) (io.ReadCloser, error)

	// Version is a helper method to get Info, GoMod, and Zip together.
	Version(ctx context.Context, mod, ver string) (*storage.Version, error)
}

type protocol struct {
	s  storage.Backend
	dp Protocol
	ch chan *job
}

type job struct {
	mod, ver string
	done     chan error
}

// New takes an upstream Protocol and storage
// it always prefers storage, otherwise it goes to upstream
// and fills the storage with the results.
func New(dp Protocol, s storage.Backend, workers int) Protocol {
	ch := make(chan *job)
	p := &protocol{dp: dp, s: s, ch: ch}
	p.start(workers)
	return p
}

func (p *protocol) start(numWorkers int) {
	for i := 0; i < numWorkers; i++ {
		go p.listen()
	}
}

func (p *protocol) listen() {
	for j := range p.ch {
		j.done <- p.fillCache(j.mod, j.ver)
	}
}

func (p *protocol) request(mod, ver string) error {
	j := &job{
		mod:  mod,
		ver:  ver,
		done: make(chan error),
	}
	p.ch <- j
	return <-j.done
}

func (p *protocol) List(ctx context.Context, mod string) ([]string, error) {
	return p.dp.List(ctx, mod)
}

func (p *protocol) Info(ctx context.Context, mod, ver string) ([]byte, error) {
	const op errors.Op = "protocol.Info"
	info, err := p.s.Info(ctx, mod, ver)
	if errors.IsNotFoundErr(err) {
		err = p.request(mod, ver)
		if err != nil {
			return nil, errors.E(op, err)
		}
		info, err = p.s.Info(ctx, mod, ver)
	}
	if err != nil {
		return nil, errors.E(op, err)
	}

	return info, nil
}

func (p *protocol) fillCache(mod, ver string) error {
	const op errors.Op = "protocol.fillCache"
	ctx, cancel := context.WithTimeout(context.Background(), time.Minute*10)
	defer cancel()
	v, err := p.dp.Version(ctx, mod, ver)
	if err != nil {
		return errors.E(op, err)
	}
	defer v.Zip.Close()
	err = p.s.Save(ctx, mod, ver, v.Mod, v.Zip, v.Info)
	if err != nil {
		return errors.E(op, err)
	}

	return nil
}

func (p *protocol) Latest(ctx context.Context, mod string) (*storage.RevInfo, error) {
	const op errors.Op = "protocol.Latest"
	info, err := p.dp.Latest(ctx, mod)
	if err != nil {
		return nil, errors.E(op, err)
	}

	return info, nil
}

func (p *protocol) GoMod(ctx context.Context, mod, ver string) ([]byte, error) {
	const op errors.Op = "protocol.GoMod"
	goMod, err := p.s.GoMod(ctx, mod, ver)
	if errors.IsNotFoundErr(err) {
		err = p.request(mod, ver)
		if err != nil {
			return nil, errors.E(op, err)
		}
		goMod, err = p.s.GoMod(ctx, mod, ver)
	}
	if err != nil {
		return nil, errors.E(op, err)
	}

	return goMod, nil
}

func (p *protocol) Zip(ctx context.Context, mod, ver string) (io.ReadCloser, error) {
	const op errors.Op = "protocol.Zip"
	zip, err := p.s.Zip(ctx, mod, ver)
	if errors.IsNotFoundErr(err) {
		err = p.request(mod, ver)
		if err != nil {
			return nil, errors.E(op, err)
		}
		zip, err = p.s.Zip(ctx, mod, ver)
	}
	if err != nil {
		return nil, errors.E(op, err)
	}

	return zip, nil
}

func (p *protocol) Version(ctx context.Context, mod, ver string) (*storage.Version, error) {
	const op errors.Op = "protocol.Version"
	info, err := p.Info(ctx, mod, ver)
	if err != nil {
		return nil, errors.E(op, err)
	}

	goMod, err := p.GoMod(ctx, mod, ver)
	if err != nil {
		return nil, errors.E(op, err)
	}

	zip, err := p.s.Zip(ctx, mod, ver)
	if err != nil {
		return nil, errors.E(op, err)
	}

	return &storage.Version{
		Info: info,
		Mod:  goMod,
		Zip:  zip,
	}, nil
}
