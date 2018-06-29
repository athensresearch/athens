package s3

import (
	"context"
	"fmt"
)

var (
	mod  = []byte("123")
	zip  = []byte("456")
	info = []byte("789")
)

func (d *S3Tests) TestSave() {
	r := d.Require()

	versions := []string{"v1.0.0", "v1.1.0", "v1.2.0"}
	expectedValues := make(map[string][]byte)
	for i, version := range versions {
		module := fmt.Sprintf("module-%d", i)
		vmod := append(mod, []byte(version)...)
		vinfo := append(info, []byte(version)...)
		vzip := append(zip, []byte(version)...)

		r.NoError(d.storage.Save(context.Background(), module, version, vmod, vzip, vinfo))
		expectedValues[key(module, version, "info")] = vinfo
		expectedValues[key(module, version, "mod")] = vmod
		expectedValues[key(module, version, "zip")] = vzip
	}

	r.NoError(Verify(d.client, expectedValues))
}
