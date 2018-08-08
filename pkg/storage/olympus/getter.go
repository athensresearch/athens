package olympus

import (
	"context"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"

	"github.com/opentracing/opentracing-go"

	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/errors"
)

// Info storage.Getter implementation
func (s *ModuleStore) Info(ctx context.Context, module, vsn string) ([]byte, error) {
	const op errors.Op = "olympus.Info"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.olympus.Info")
	defer sp.Finish()

	// TODO: fetch from endpoint
	infoURI := fmt.Sprintf("%s/%s", s.url, config.PackageVersionedName(module, vsn, "info"))
	resp, err := s.client.Get(infoURI)
	if err != nil {
		return nil, errors.E(op, err)
	}
	defer resp.Body.Close()
	info, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, errors.E(op, err)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, errors.E(op, fmt.Sprintf("unexpected status %v - body: %s", resp.StatusCode, info), resp.StatusCode)
	}

	return info, nil
}

// GoMod storage.Getter implementation
func (s *ModuleStore) GoMod(ctx context.Context, module, vsn string) ([]byte, error) {
	const op errors.Op = "olympus.GoMod"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.olympus.GoMod")
	defer sp.Finish()

	// TODO: fetch from endpoint
	modURI := fmt.Sprintf("%s/%s", s.url, config.PackageVersionedName(module, vsn, "mod"))
	resp, err := s.client.Get(modURI)
	if err != nil {
		return nil, errors.E(op, err)
	}
	defer resp.Body.Close()
	mod, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, errors.E(op, err)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, errors.E(op, fmt.Sprintf("unexpected status %v - body: %s", resp.StatusCode, mod), resp.StatusCode)
	}

	return mod, err
}

// Zip storage.Getter implementation
func (s *ModuleStore) Zip(ctx context.Context, module, vsn string) (io.ReadCloser, error) {
	const op errors.Op = "olympus.Zip"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.olympus.Zip")
	defer sp.Finish()

	// TODO: fetch from endpoint
	zipURI := fmt.Sprintf("%s/%s", s.url, config.PackageVersionedName(module, vsn, "zip"))
	resp, err := s.client.Get(zipURI)
	if err != nil {
		return nil, err
	}
	if resp.StatusCode != http.StatusOK {
		msg := fmt.Errorf("GET %v returned unexpected status: %v", zipURI, resp.StatusCode)
		return nil, errors.E(op, msg)
	}

	return resp.Body, nil
}
