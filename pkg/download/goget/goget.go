package goget

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"os/exec"
	"strings"
	"time"

	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/download"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/module"
	"github.com/gomods/athens/pkg/storage"
	"github.com/spf13/afero"
)

// New returns a download protocol by using
// go get. You must have a modules supported
// go binary for this to work.
func New() (download.Protocol, error) {
	const op errors.Op = "goget.New"
	goBin := env.GoBinPath()
	fs := afero.NewOsFs()
	mf, err := module.NewGoGetFetcher(goBin, fs)
	if err != nil {
		return nil, errors.E(op, err)
	}
	return &goget{
		goBinPath: goBin,
		fetcher:   mf,
		fs:        fs,
	}, nil
}

type goget struct {
	goBinPath string
	fetcher   module.Fetcher
	fs        afero.Fs
}

func (gg *goget) List(ctx context.Context, mod string) ([]string, error) {
	const op errors.Op = "goget.List"
	lr, err := gg.list(op, mod)
	if err != nil {
		return nil, err
	}

	return lr.Versions, nil
}

type listResp struct {
	Path     string
	Version  string
	Versions []string `json:",omitempty"`
	Time     time.Time
}

func (gg *goget) Info(ctx context.Context, mod string, ver string) ([]byte, error) {
	const op errors.Op = "goget.Info"
	v, err := gg.Version(ctx, mod, ver)
	if err != nil {
		return nil, errors.E(op, err)
	}
	v.Zip.Close()

	return v.Info, nil
}

func (gg *goget) Latest(ctx context.Context, mod string) (*storage.RevInfo, error) {
	const op errors.Op = "goget.Latest"
	lr, err := gg.list(op, mod)
	if err != nil {
		return nil, err
	}

	pseudoInfo := strings.Split(lr.Version, "-")
	if len(pseudoInfo) < 3 {
		return nil, errors.E(op, fmt.Errorf("malformed pseudoInfo %v", lr.Version))
	}
	return &storage.RevInfo{
		Name:    pseudoInfo[2],
		Short:   pseudoInfo[2],
		Time:    lr.Time,
		Version: lr.Version,
	}, nil
}

func (gg *goget) list(op errors.Op, mod string) (*listResp, error) {
	hackyPath, err := afero.TempDir(gg.fs, "", "hackymod")
	if err != nil {
		return nil, errors.E(op, err)
	}
	defer gg.fs.RemoveAll(hackyPath)
	err = module.Dummy(gg.fs, hackyPath)
	cmd := exec.Command(
		gg.goBinPath,
		"list", "-m", "-versions", "-json",
		config.FmtModVer(mod, "latest"),
	)
	cmd.Dir = hackyPath

	bts, err := cmd.CombinedOutput()
	if err != nil {
		errFmt := fmt.Errorf("%v: %s", err, bts)
		return nil, errors.E(op, errFmt)
	}

	// ugly hack until go cli implements -quiet flag.
	// https://github.com/golang/go/issues/26628
	if bytes.HasPrefix(bts, []byte("go: finding")) {
		bts = bts[bytes.Index(bts, []byte{'\n'}):]
	}

	var lr listResp
	err = json.Unmarshal(bts, &lr)
	if err != nil {
		return nil, errors.E(op, err)
	}

	return &lr, nil
}

func (gg *goget) GoMod(ctx context.Context, mod string, ver string) ([]byte, error) {
	const op errors.Op = "goget.Info"
	v, err := gg.Version(ctx, mod, ver)
	if err != nil {
		return nil, errors.E(op, err)
	}
	v.Zip.Close()

	return v.Mod, nil
}

func (gg *goget) Zip(ctx context.Context, mod, ver string) (io.ReadCloser, error) {
	const op errors.Op = "goget.Info"
	v, err := gg.Version(ctx, mod, ver)
	if err != nil {
		return nil, errors.E(op, err)
	}

	return v.Zip, nil
}

func (gg *goget) Version(ctx context.Context, mod, ver string) (*storage.Version, error) {
	const op errors.Op = "goget.Version"
	ref, err := gg.fetcher.Fetch(mod, ver)
	if err != nil {
		return nil, errors.E(op, err)
	}
	v, err := ref.Read()
	if err != nil {
		return nil, errors.E(op, err)
	}

	return v, nil
}
