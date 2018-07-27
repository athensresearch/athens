package olympus

import (
	"bytes"
	"context"
	"fmt"
	"io/ioutil"
	"net/http"

	"github.com/opentracing/opentracing-go"

	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/storage"
)

// Get a specific version of a module
func (s *ModuleStore) Get(module, vsn string) (*storage.Version, error) {
	sp, _ := opentracing.StartSpanFromContext(context.TODO(), "storage.olympus.Get")
	defer sp.Finish()

	// TODO: fetch from endpoint
	modURI := fmt.Sprintf("%s/%s", s.url, config.PackageVersionedName(module, vsn, "mod"))
	zipURI := fmt.Sprintf("%s/%s", s.url, config.PackageVersionedName(module, vsn, "zip"))
	infoURI := fmt.Sprintf("%s/%s", s.url, config.PackageVersionedName(module, vsn, "info"))

	// fetch mod file
	var mod []byte

	modResp, err := s.client.Get(modURI)
	if err != nil {
		return nil, err
	}
	defer modResp.Body.Close()
	if modResp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("GET %v returned unexpected status: %v", modURI, modResp.StatusCode)
	}

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
	if zipResp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("GET %v returned unexpected status: %v", zipURI, zipResp.StatusCode)
	}

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
	if infoResp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("GET %v returned unexpected status: %v", infoURI, infoResp.StatusCode)
	}

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
