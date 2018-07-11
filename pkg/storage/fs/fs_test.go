package fs

import (
	"path/filepath"
)

func (d *FsTests) TestLocationFuncs() {
	r := d.Require()
	storage := d.storage.(*storageImpl)
	moduleLoc := storage.moduleLocation(module)
	r.Equal(filepath.Join(d.rootDir, module), moduleLoc)
	versionedLoc := storage.versionLocation(module, version)
	r.Equal(filepath.Join(d.rootDir, module, version), versionedLoc)
}
