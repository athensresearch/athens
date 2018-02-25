package willie

import (
	"bytes"
	"io"
	"mime/multipart"
	"net/http"

	"github.com/pkg/errors"
)

type File struct {
	io.Reader
	ParamName string
	FileName  string
}

type fileHandler struct {
	io.Reader
	*multipart.Writer
}

func (r *Request) MultiPartPost(body interface{}, files ...File) (*Response, error) {
	req, err := newMultipart(r.URL, "POST", body, files...)
	if err != nil {
		return nil, errors.WithStack(err)
	}
	return r.perform(req), nil
}

func (r *Request) MultiPartPut(body interface{}, files ...File) (*Response, error) {
	req, err := newMultipart(r.URL, "PUT", body, files...)
	if err != nil {
		return nil, errors.WithStack(err)
	}
	return r.perform(req), nil
}

// this helper method was inspired by this blog post by Matt Aimonetti:
// https://matt.aimonetti.net/posts/2013/07/01/golang-multipart-file-upload-example/
func newMultipart(url string, method string, body interface{}, files ...File) (*http.Request, error) {

	bb := &bytes.Buffer{}
	writer := multipart.NewWriter(bb)
	defer writer.Close()
	for _, f := range files {
		part, err := writer.CreateFormFile(f.ParamName, f.FileName)
		if err != nil {
			return nil, errors.WithStack(err)
		}
		_, err = io.Copy(part, f)
	}

	for k, v := range toURLValues(body) {
		for _, vv := range v {
			err := writer.WriteField(k, vv)
			if err != nil {
				return nil, errors.WithStack(err)
			}
		}
	}

	req, err := http.NewRequest(method, url, bb)
	if err != nil {
		return nil, errors.WithStack(err)
	}
	req.Header.Set("Content-Type", writer.FormDataContentType())
	return req, nil
}
