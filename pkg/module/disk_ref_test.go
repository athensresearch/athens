package module

import (
	"io/ioutil"
	"path/filepath"

	"github.com/spf13/afero"
)

func (m *ModuleSuite) TestDiskRefReadAndClear() {
	const (
		root    = "testroot"
		version = "v1.0.0"
		info    = "testinfo"
		mod     = "testmod"
		zip     = "testzip"
	)
	r := m.Require()

	packagePath := getPackagePath(root, mod)
	// create a new disk ref using the filesystem
	diskRef := newDiskRef(m.fs, root, mod, version)

	// ensure that reading fails, because there are no files
	ver, err := diskRef.Read()
	r.Nil(ver)
	r.NotNil(err)

	// create all the files the disk ref expects
	r.NoError(createAndWriteFile(m.fs, filepath.Join(packagePath, version+".info"), info))
	r.NoError(createAndWriteFile(m.fs, filepath.Join(packagePath, version+".mod"), mod))
	r.NoError(createAndWriteFile(m.fs, filepath.Join(packagePath, version+".zip"), zip))

	// read from the disk ref - this time it should succeed
	ver, err = diskRef.Read()
	r.NoError(err)
	r.Equal(info, string(ver.Info))
	r.Equal(mod, string(ver.Mod))
	zipBytes, err := ioutil.ReadAll(ver.Zip)
	r.NoError(err)
	r.Equal(zip, string(zipBytes))

	// Validate that the root dir still exists
	fInfo, err := m.fs.Stat(root)
	r.NotNil(fInfo)
	r.Nil(err)

	// clear the disk ref and expect it to fail again
	r.NoError(diskRef.Clear())
	ver, err = diskRef.Read()
	r.Nil(ver)
	r.NotNil(err)

	// The root dir should not exist after a clear
	fInfo, err = m.fs.Stat(root)
	r.Nil(fInfo)
	r.NotNil(err)

}

// creates filename with fs, writes data to the file, and closes the file,
//
// returns a non-nil error if anything went wrong. the file will be closed
// regardless of what this function returns
func createAndWriteFile(fs afero.Fs, filename, data string) error {
	fileHandle, err := fs.Create(filename)
	if err != nil {
		return err
	}
	defer fileHandle.Close()
	_, err = fileHandle.Write([]byte(data))
	return err
}
