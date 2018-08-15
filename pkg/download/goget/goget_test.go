package goget

import (
	"bytes"
	"context"
	"encoding/json"
	"io/ioutil"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"github.com/gomods/athens/pkg/storage"
	"github.com/stretchr/testify/require"
)

type listTest struct {
	name string
	path string
	tags []string
}

var listTests = []listTest{
	{
		name: "happy tags",
		path: "github.com/athens-artifacts/happy-path",
		tags: []string{"v0.0.1", "v0.0.2", "v0.0.3"},
	},
	{
		name: "no tags",
		path: "github.com/athens-artifacts/no-tags",
	},
}

func TestList(t *testing.T) {
	dp, err := New()
	require.NoError(t, err, "failed to create protocol")
	ctx := context.Background()

	for _, tc := range listTests {
		t.Run(tc.name, func(t *testing.T) {
			versions, err := dp.List(ctx, tc.path)
			require.NoError(t, err)
			require.EqualValues(t, tc.tags, versions)
		})
	}
}

type latestTest struct {
	name string
	path string
	info *storage.RevInfo
	err  bool
}

var latestTests = []latestTest{
	{
		name: "happy path",
		path: "github.com/athens-artifacts/no-tags",
		info: &storage.RevInfo{
			Version: "v0.0.0-20180803171426-1a540c5d67ab",
			Time:    time.Date(2018, 8, 3, 17, 14, 26, 0, time.UTC),
		},
	},
	{
		name: "tagged latest",
		path: "github.com/athens-artifacts/happy-path",
		info: &storage.RevInfo{
			Version: "v0.0.3",
			Time:    time.Date(2018, 8, 3, 17, 16, 00, 0, time.UTC),
		},
	},
}

func TestLatest(t *testing.T) {
	dp, err := New()
	require.NoError(t, err)
	ctx := context.Background()

	for _, tc := range latestTests {
		t.Run(tc.name, func(t *testing.T) {
			info, err := dp.Latest(ctx, tc.path)
			if !tc.err && err != nil {
				t.Fatal(err)
			} else if tc.err && err == nil {
				t.Fatalf("expected %v error but got nil", tc.err)
			}

			require.EqualValues(t, tc.info, info)
		})
	}
}

type infoTest struct {
	name    string
	path    string
	version string
	info    *storage.RevInfo
}

var infoTests = []infoTest{
	{
		name:    "happy path",
		path:    "github.com/athens-artifacts/happy-path",
		version: "v0.0.2",
		info: &storage.RevInfo{
			Version: "v0.0.2",
			Time:    time.Date(2018, 8, 3, 3, 45, 19, 0, time.UTC),
		},
	},
	{
		name:    "pseudo version",
		path:    "github.com/athens-artifacts/no-tags",
		version: "v0.0.0-20180803035119-e4e0177efdb5",
		info: &storage.RevInfo{
			Version: "v0.0.0-20180803035119-e4e0177efdb5",
			Time:    time.Date(2018, 8, 3, 3, 51, 19, 0, time.UTC),
		},
	},
}

func TestInfo(t *testing.T) {
	dp, err := New()
	require.NoError(t, err)
	ctx := context.Background()

	for _, tc := range infoTests {
		t.Run(tc.name, func(t *testing.T) {
			bts, err := dp.Info(ctx, tc.path, tc.version)
			require.NoError(t, err)

			var info storage.RevInfo
			dec := json.NewDecoder(bytes.NewReader(bts))
			dec.DisallowUnknownFields()
			err = dec.Decode(&info)
			require.NoError(t, err)

			require.EqualValues(t, tc.info, &info)
		})
	}
}

type modTest struct {
	name    string
	path    string
	version string
	err     bool
}

var modTests = []modTest{
	{
		name:    "no mod file",
		path:    "github.com/athens-artifacts/no-tags",
		version: "v0.0.0-20180803035119-e4e0177efdb5",
	},
	{
		name:    "upstream mod file",
		path:    "github.com/athens-artifacts/happy-path",
		version: "v0.0.3",
	},
	{
		name:    "incorrect github repo",
		path:    "github.com/athens-artifacts/not-exists",
		version: "v1.0.0",
		err:     true,
	},
}

func TestGoMod(t *testing.T) {
	dp, err := New()
	require.NoError(t, err)
	ctx := context.Background()

	for _, tc := range modTests {
		t.Run(tc.name, func(t *testing.T) {
			mod, err := dp.GoMod(ctx, tc.path, tc.version)
			require.Equal(t, tc.err, err != nil, err)

			if tc.err {
				t.Skip()
			}
			expected := getGoldenFile(t, tc.name)
			require.Equal(t, string(expected), string(mod))
		})
	}
}

func getGoldenFile(t *testing.T, name string) []byte {
	t.Helper()
	file := filepath.Join("test_data", strings.Replace(name, " ", "_", -1)+".golden")
	bts, err := ioutil.ReadFile(file)
	if err != nil {
		t.Fatal(err)
	}

	return bts
}
