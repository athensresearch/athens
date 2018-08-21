package mongo

import (
	"crypto/tls"
	"crypto/x509"
	"fmt"
	"io/ioutil"
	"net"
	"strings"

	"github.com/globalsign/mgo"
	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/errors"
)

// ModuleStore represents a mongo backed storage backend.
type ModuleStore struct {
	s        *mgo.Session
	d        string // database
	c        string // collection
	url      string
	certPath string
}

// NewStorage returns a connected Mongo backed storage
// that satisfies the Backend interface.
func NewStorage(connectionString string) (*ModuleStore, error) {
	return NewStorageWithCert(connectionString, "")
}

// NewStorageWithCert returns a connected Mongo backed storage
// that satisfies the Backend interface.
func NewStorageWithCert(connectionString, certPath string) (*ModuleStore, error) {
	const op errors.Op = "fs.NewStorage"
	ms := &ModuleStore{url: connectionString, certPath: certPath}

	err := ms.connect()
	if err != nil {
		return nil, errors.E(op, err)
	}
	return ms, nil

}

func (m *ModuleStore) connect() error {
	const op errors.Op = "mongo.connect"

	var err error
	m.s, err = m.newSession()
	if err != nil {
		return errors.E(op, err)
	}

	// TODO: database and collection as env vars, or params to New()? together with user/mongo
	m.d = "athens"
	m.c = "modules"

	index := mgo.Index{
		Key:        []string{"base_url", "module", "version"},
		Unique:     true,
		DropDups:   true,
		Background: true,
		Sparse:     true,
	}
	c := m.s.DB(m.d).C(m.c)
	return c.EnsureIndex(index)
}

func (m *ModuleStore) newSession() (*mgo.Session, error) {
	tlsConfig := &tls.Config{}

	dialInfo, err := mgo.ParseURL(m.url)
	if err != nil {
		return nil, err
	}

	dialInfo.Timeout = env.MongoConnectionTimeoutSecWithDefault(1)

	if m.certPath != "" {
		roots := x509.NewCertPool()
		cert, err := ioutil.ReadFile(m.certPath)
		if err != nil {
			return nil, err
		}

		if ok := roots.AppendCertsFromPEM(cert); !ok {
			return nil, fmt.Errorf("failed to parse certificate from: %s", m.certPath)
		}

		tlsConfig.InsecureSkipVerify = true
		tlsConfig.ClientCAs = roots

		dialInfo.DialServer = func(addr *mgo.ServerAddr) (net.Conn, error) {
			return tls.Dial("tcp", addr.String(), tlsConfig)
		}
	}

	return mgo.DialWithInfo(dialInfo)
}

func (m *ModuleStore) gridFileName(mod, ver string) string {
	return strings.Replace(mod, "/", "_", -1) + "_" + ver + ".zip"
}
