package module

import (
	"fmt"
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

	// close the version's zip file (which also cleans up the underlying diskref's GOPATH) and expect it to fail again
	r.NoError(ver.Zip.Close())
	ver, err = diskRef.Read()
	r.Nil(ver)
	r.NotNil(err)

	// The root dir should not exist after a clear
	fInfo, err = m.fs.Stat(root)
	r.Nil(fInfo)
	r.NotNil(err)

}

func (m *ModuleSuite) TestDiskRefClearFail() {
	root := "testroot"
	r := m.Require()
	// This should fail because we haven't created any files
	err := ClearFiles(m.fs, root)
	r.EqualError(err, "open testroot: file does not exist")
}

func (m *ModuleSuite) TestDiskRefClearSuccess() {
	const (
		root = "testroot"
		mod  = "testmod"
		file = "testfile"
		info = "testinfo"
	)
	r := m.Require()

	// Create a single file
	packagePath := getPackagePath(root, mod)
	filePath := filepath.Join(packagePath, file)
	r.NoError(createAndWriteFile(m.fs, filePath, info))

	// Validate the file exists
	_, err := m.fs.Stat(filePath)
	r.NoError(err)

	// Now clear the files
	err = ClearFiles(m.fs, root)
	r.NoError(err)

	// Validate the file has been deleted
	_, err = m.fs.Stat(filePath)
	expErr := fmt.Sprintf("open %s: file does not exist", filePath)
	r.EqualError(err, expErr)
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
