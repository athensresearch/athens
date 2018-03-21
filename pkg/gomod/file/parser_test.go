package file

import (
	"os"
	"testing"

	"github.com/stretchr/testify/assert"

	"github.com/gomods/athens/pkg/gomod"
)

func TestFileParser_ModuleName(t *testing.T) {
	a := assert.New(t)

	var testCases = []struct {
		file        string
		expected    string
		expectedErr error
	}{
		{"testdata/go.0.mod", "my/thing", nil},
		{"testdata/go.1.mod", "my/thing2", nil},
		{"testdata/go.2.mod", "", parser.ErrNotFound},
		{"testdata/go.3.mod", "", parser.ErrNotFound},
		{"testdata/go.4.mod", "", parser.ErrNotFound},
	}

	for _, tc := range testCases {
		t.Run(tc.file, func(t *testing.T) {
			fp := NewFileParser(tc.file)
			actual, actualErr := fp.ModuleName()

			a.Equal(tc.expected, actual)
			a.Equal(tc.expectedErr, actualErr)
		})
	}
}

func TestFileParser_ModuleName_FileNotFound(t *testing.T) {
	fp := NewFileParser("/not/exist")
	_, err := fp.ModuleName()
	assert.True(t, os.IsNotExist(err))
}
