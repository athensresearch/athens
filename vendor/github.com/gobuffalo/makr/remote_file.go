package makr

import (
	"bytes"
	"fmt"
	"io"
	"net/http"

	"github.com/pkg/errors"
)

// RemoteFile represents a remote file (URL) that you want to pull down and write into the project
type RemoteFile struct {
	RemotePath string
	File
}

// Run the generator
func (f RemoteFile) Run(rootPath string, data Data) error {
	return chdir(rootPath, func() error {
		if f.Should != nil && !f.Should(data) {
			return nil
		}

		res, err := http.Get(f.RemotePath)
		if err != nil {
			return errors.WithStack(err)
		}
		code := res.StatusCode
		if code < 200 || code >= 300 {
			return errors.WithStack(fmt.Errorf("Error fetching %s (%d)", f.RemotePath, code))
		}

		bb := bytes.Buffer{}
		_, err = io.Copy(&bb, res.Body)
		if err != nil {
			return errors.WithStack(err)
		}
		return f.save(rootPath, f.Path, bb.String())
	})
}
