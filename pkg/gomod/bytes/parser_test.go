package bytes

import (
	"testing"

	"github.com/stretchr/testify/assert"

	parser "github.com/gomods/athens/pkg/gomod"
)

func TestContentParser_ModuleName(t *testing.T) {
	a := assert.New(t)

	var testCases = []struct {
		content     []byte
		expected    string
		expectedErr error
	}{
		{[]byte(`module "my/thing"`), "my/thing", nil},
		{[]byte(`module "my/thing2`), "", parser.ErrNotFound},
		{[]byte(`foobar`), "", parser.ErrNotFound},
	}

	for _, tc := range testCases {
		t.Run("", func(t *testing.T) {
			cp := NewContentParser(tc.content)
			actual, actualErr := cp.ModuleName()

			a.Equal(tc.expected, actual)
			a.Equal(tc.expectedErr, actualErr)
		})
	}
}
