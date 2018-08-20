package fs

// NOTE: for encoding and decoding data from the file
// encoding/json has to be used over encoding/gob due to a possible bug
// in afero. see issue #172 for reference
// https://github.com/spf13/afero/issues/172
import (
	"encoding/json"
	"io"
	"os"

	"github.com/gomods/athens/pkg/eventlog"
	"github.com/spf13/afero"
)

// Registry is a pointer registry for olympus server event logs
type Registry struct {
	rootDir string
	fs      afero.Fs
}

// registryData is a map[string]string used to encode/decode the registry from disk
type registryData map[string]string

var registryFilename = "pointerRegistry"

// NewRegistry returns a file based implementation of a pointer registry
func NewRegistry(rootDir string, filesystem afero.Fs) *Registry {
	return &Registry{rootDir: rootDir, fs: filesystem}
}

// LookupPointer returns the pointer to the given deployment's event log
func (r *Registry) LookupPointer(deploymentID string) (string, error) {
	f, err := r.fs.OpenFile(registryFilename, os.O_RDONLY|os.O_CREATE, 0440)
	if err != nil {
		return "", err
	}
	defer f.Close()

	var data = make(registryData)

	dec := json.NewDecoder(f)
	if err := dec.Decode(&data); err != nil {
		return "", err
	}

	result, ok := data[deploymentID]
	if !ok {
		return "", eventlog.ErrDeploymentNotFound
	}

	return result, nil
}

// SetPointer both sets and updates the deployment's event log pointer
func (r *Registry) SetPointer(deploymentID, pointer string) error {
	f, err := r.fs.OpenFile(registryFilename, os.O_RDWR|os.O_CREATE, 0660)
	if err != nil {
		return err
	}
	defer f.Close()

	var data = make(registryData)

	dec := json.NewDecoder(f)
	if err := dec.Decode(&data); err != nil && err != io.EOF {
		return err
	}

	data[deploymentID] = pointer

	if _, err := f.Seek(0, os.SEEK_SET); err != nil {
		return err
	}

	enc := json.NewEncoder(f)
	return enc.Encode(&data)
}
