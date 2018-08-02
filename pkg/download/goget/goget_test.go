package goget

import (
	"context"
	"testing"

	"github.com/stretchr/testify/require"
)

type testCase struct {
	name    string
	mod     string
	version string
}

// TODO(marwan): we should create Test Repos under github.com/gomods
// so we can get reproducible results from live VCS repos.
// For now, I cannot test that github.com/pkg/errors returns v0.8.0
// from goget.Latest, because they could very well introduce a new tag
// in the near future.
var tt = []testCase{
	{"basic list", "github.com/pkg/errors", "latest"},
	{"list non tagged", "github.com/marwan-at-work/gowatch", "latest"},
	{"list vanity", "golang.org/x/tools", "latest"},
}

func TestList(t *testing.T) {
	dp, err := New()
	require.NoError(t, err, "failed to create protocol")
	ctx := context.Background()

	for _, tc := range tt {
		t.Run(tc.name, func(t *testing.T) {
			_, err := dp.List(ctx, tc.mod) // TODO ensure list is correct per TODO above.
			if err != nil {
				t.Fatal(err)
			}
		})
	}
}
