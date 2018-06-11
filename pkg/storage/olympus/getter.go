package olympus

import (
	"bytes"
	"fmt"
	"io/ioutil"

	"github.com/gomods/athens/pkg/storage"
)

// Get a specific version of a module
func (s *ModuleStore) Get(module, vsn string) (*storage.Version, error) {
	// TODO: fetch from endpoint

	modURI := fmt.Sprintf("%s/%s/@v/%s.mod", s.url, module, vsn)
	zipURI := fmt.Sprintf("%s/%s/@v/%s.zip", s.url, module, vsn)
	infoURI := fmt.Sprintf("%s/%s/@v/%s.info", s.url, module, vsn)

	// fetch mod file
	var mod []byte

	modResp, err := s.client.Get(modURI)
	if err != nil {
		return nil, err
	}
	defer modResp.Body.Close()

	mod, err = ioutil.ReadAll(modResp.Body)
	if err != nil {
		return nil, err
	}

	// fetch source file
	var zip []byte
	zipResp, err := s.client.Get(zipURI)
	if err != nil {
		return nil, err
	}
	defer zipResp.Body.Close()

	zip, err = ioutil.ReadAll(zipResp.Body)
	if err != nil {
		return nil, err
	}

	// fetch info file
	var info []byte
	infoResp, err := s.client.Get(infoURI)
	if err != nil {
		return nil, err
	}
	defer infoResp.Body.Close()

	info, err = ioutil.ReadAll(infoResp.Body)
	if err != nil {
		return nil, err
	}

	return &storage.Version{
		Info: info,
		Mod:  mod,
		Zip:  ioutil.NopCloser(bytes.NewReader(zip)),
	}, nil
}
