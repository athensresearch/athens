package download

import (
	"context"
	"io"

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
}

// New takes an upstream Protocol and storage
// it always prefers storage, otherwise it goes to upstream
// and fills the storage with the results.
func New(dp Protocol, s storage.Backend) Protocol {
	return &protocol{dp: dp, s: s}
}

func (p *protocol) List(ctx context.Context, mod string) ([]string, error) {
	return p.dp.List(ctx, mod)
}

func (p *protocol) Info(ctx context.Context, mod, ver string) ([]byte, error) {
	const op errors.Op = "protocol.Info"
	v, err := p.s.Get(mod, ver)
	if errors.ErrNotFound(err) {
		v, err = p.fillCache(ctx, mod, ver)
	}
	if err != nil {
		return nil, errors.E(op, err)
	}

	return v.Info, nil
}

func (p *protocol) fillCache(ctx context.Context, mod, ver string) (*storage.Version, error) {
	const op errors.Op = "protocol.fillCache"
	v, err := p.dp.Version(ctx, mod, ver)
	if err != nil {
		return nil, errors.E(op, err)
	}
	err = p.s.Save(ctx, mod, ver, v.Mod, v.Zip, v.Info)
	if err != nil {
		return nil, errors.E(op, err)
	}

	return v, nil
}

func (p *protocol) Latest(ctx context.Context, mod string) (*storage.RevInfo, error) {
	return p.dp.Latest(ctx, mod)
}

func (p *protocol) GoMod(ctx context.Context, mod, ver string) ([]byte, error) {
	const op errors.Op = "protocol.GoMod"
	v, err := p.s.Get(mod, ver)
	if errors.ErrNotFound(err) {
		v, err = p.fillCache(ctx, mod, ver)
	}
	if err != nil {
		return nil, errors.E(op, err)
	}

	return v.Mod, nil
}

func (p *protocol) Zip(ctx context.Context, mod, ver string) (io.ReadCloser, error) {
	const op errors.Op = "protocol.Zip"
	v, err := p.s.Get(mod, ver)
	if errors.ErrNotFound(err) {
		v, err = p.fillCache(ctx, mod, ver)
	}
	if err != nil {
		return nil, errors.E(op, err)
	}

	return v.Zip, nil
}

func (p *protocol) Version(ctx context.Context, mod, ver string) (*storage.Version, error) {
	return p.dp.Version(ctx, mod, ver)
}
