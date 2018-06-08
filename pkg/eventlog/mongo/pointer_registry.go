package mongo

import (
	"github.com/globalsign/mgo"
	"github.com/gomods/athens/pkg/eventlog"
)

// Registry is a pointer registry for olypus server event logs
type Registry struct {
	s   *mgo.Session
	d   string // database
	c   string // collection
	url string
}

// NewRegistry creates a pointer registry from backing mongo database
func NewRegistry(url string) (*Registry, error) {
	return NewRegistryWithCollection(url, "pointer-registry")
}

// NewRegistryWithCollection creates a registry using the collection provided
func NewRegistryWithCollection(url, collection string) (*Registry, error) {
	r := Registry{
		url: url,
		c:   collection,
		d:   "athens",
	}
	return &r, r.Connect()
}

// Connect establishes a session with the mongo cluster
func (r *Registry) Connect() error {
	s, err := mgo.Dial(r.url)
	if err != nil {
		return err
	}
	r.s = s

	index := mgo.Index{
		Key:    []string{"deployment"},
		Unique: true,
	}

	c := r.s.DB(r.d).C(r.c)
	return c.EnsureIndex(index)
}

// LookupPointer returns the pointer to the given deploymentID eventlog
func (r *Registry) LookupPointer(deploymentID string) (string, error) {
	var result eventlog.RegisteredEventlog

	c := r.s.DB(r.d).C(r.c)
	if err := c.FindId(deploymentID).One(&result); err == mgo.ErrNotFound {
		return result.Pointer, eventlog.ErrDeploymentNotFound
	}

	return result.Pointer, nil
}

// SetPointer both sets and updates a pointer for a given deploymentID eventlog
func (r *Registry) SetPointer(deploymentID, pointer string) error {
	logPointer := eventlog.RegisteredEventlog{
		DeploymentID: deploymentID,
		Pointer:      pointer,
	}
	c := r.s.DB(r.d).C(r.c)
	_, err := c.UpsertId(deploymentID, logPointer)

	return err
}
