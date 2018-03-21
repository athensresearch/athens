package parser

import (
	"bytes"
	"errors"
	"io"
	"testing"

	"github.com/stretchr/testify/assert"
)

var errReaderError = errors.New("errReader error")

type errReader struct{}

// Read implements io.Reader but returns error (not EOF)
func (e *errReader) Read(p []byte) (n int, err error) { return 0, errReaderError }

func TestParse(t *testing.T) {
	a := assert.New(t)

	var testCases = []struct {
		reader      io.Reader
		expected    string
		expectedErr error
	}{
		{newBuf(`module "my/thing"`), "my/thing", nil},
		{newBuf("module \"my/thing2\"\nmodule \"my/thing1\""), "my/thing2", nil},
		{newBuf(`module "github.com/gomods/athens"`), "github.com/gomods/athens", nil},
		{newBuf(`module "github.com.athens/gomods"`), "github.com.athens/gomods", nil},
		{newBuf(``), "", ErrNotFound},
		{newBuf(`module "my/thing2`), "", ErrNotFound},
		{newBuf(`module my/thing3`), "", ErrNotFound},
		{newBuf(`module github.com/gomods/athens`), "", ErrNotFound},
		{newBuf(`module "github.com?gomods"`), "", ErrNotFound},
		{newBuf(`module "github.com.athens"`), "", ErrNotFound},
		{newBuf(`module "github.com.athens"`), "", ErrNotFound},
		{newBuf(`module "github.com&athens"`), "", ErrNotFound},
		{newBuf(`module "?github%com&athens"`), "", ErrNotFound},
		{newBuf(`foobar`), "", ErrNotFound},
		{new(errReader), "", errReaderError},
	}

	for _, tc := range testCases {
		t.Run("", func(t *testing.T) {
			actual, actualErr := Parse(tc.reader)

			a.Equal(tc.expected, actual)
			a.Equal(tc.expectedErr, actualErr)
		})
	}
}

func newBuf(content string) io.Reader {
	return bytes.NewBuffer([]byte(content))
}
