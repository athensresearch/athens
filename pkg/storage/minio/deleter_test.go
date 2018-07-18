package minio

import (
	"bytes"
	"context"
	"time"

	"github.com/gomods/athens/pkg/storage"
)

const module = "testmodule"

var (
	mod  = []byte{1, 2, 3}
	zip  = []byte{4, 5, 6}
	info = []byte{7, 8, 9}
)

func (d *MinioTests) TestDelete() {
	r := d.Require()

	version := "delete" + time.Now().String()
	err := d.storage.Save(context.Background(), module, version, mod, bytes.NewReader(zip), info)
	r.NoError(err)

	tests := []struct {
		module  string
		version string
		want    error
	}{
		{
			module:  "does/not/exist",
			version: "v1.0.0",
			want: storage.ErrVersionNotFound{
				Module:  "does/not/exist",
				Version: "v1.0.0",
			},
		},
		{
			module:  module,
			version: version,
		},
	}
	for _, test := range tests {
		err := d.storage.Delete(test.module, test.version)
		r.Equal(test.want, err)
		exists := d.storage.Exists(test.module, test.version)
		r.Equal(false, exists)
	}
}
