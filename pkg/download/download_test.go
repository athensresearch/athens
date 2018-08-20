package download

import (
	"bytes"
	"context"
	"io/ioutil"
	"testing"

	"github.com/gomods/athens/pkg/storage/mem"

	"github.com/gomods/athens/pkg/storage"
	"golang.org/x/sync/errgroup"
)

type testMod struct {
	mod, ver string
}

var mods = []testMod{
	{"github.com/athens-artifacts/no-tags", "v0.0.2"},
	{"github.com/athens-artifacts/happy-path", "v0.0.0-20180803035119-e4e0177efdb5"},
	{"github.com/athens-artifacts/samplelib", "v1.0.0"},
}

func TestDownloadProtocol(t *testing.T) {
	s, err := mem.NewStorage()
	if err != nil {
		t.Fatal(err)
	}
	dp := New(&mockProtocol{}, s, 2)
	ctx := context.Background()

	var eg errgroup.Group
	for i := 0; i < len(mods); i++ {
		m := mods[i]
		eg.Go(func() error {
			_, err := dp.GoMod(ctx, m.mod, m.ver)
			return err
		})
	}

	err = eg.Wait()
	if err != nil {
		t.Fatal(err)
	}

	for _, m := range mods {
		bts, err := dp.GoMod(ctx, m.mod, m.ver)
		if err != nil {
			t.Fatal(err)
		}
		if !bytes.Equal(bts, []byte(m.mod+"@"+m.ver)) {
			t.Fatalf("unexpected gomod content: %s", bts)
		}
	}
}

type mockProtocol struct {
	Protocol
}

// Info implements GET /{module}/@v/{version}.info
func (m *mockProtocol) Info(ctx context.Context, mod, ver string) ([]byte, error) {
	return []byte(mod + "@" + ver), nil
}

func (m *mockProtocol) Version(ctx context.Context, mod, ver string) (*storage.Version, error) {
	bts := []byte(mod + "@" + ver)
	return &storage.Version{
		Mod:  bts,
		Info: bts,
		Zip:  ioutil.NopCloser(bytes.NewReader(bts)),
	}, nil
}
