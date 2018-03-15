package disk

import (
	"io/ioutil"
	"os"
	"path/filepath"
)

func (s *storageImpl) Save(baseURL, module, vsn string, mod, zip []byte) error {
	dir := s.versionDiskLocation(baseURL, module, vsn)
	// TODO: 777 is not the best filemode, use something better

	// make the versioned directory to hold the go.mod and the zipfile
	if err := os.MkdirAll(dir, os.ModePerm); err != nil {
		return err
	}

	// write the go.mod file
	if err := ioutil.WriteFile(filepath.Join(dir, "go.mod"), mod, os.ModePerm); err != nil {
		return err
	}

	// write the zipfile
	return ioutil.WriteFile(filepath.Join(dir, "source.zip"), zip, os.ModePerm)
}
