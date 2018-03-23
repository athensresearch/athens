package mongo

import (
	"errors"
	"strings"

	"github.com/gomods/athens/pkg/user"

	"github.com/globalsign/mgo"
	"github.com/globalsign/mgo/bson"
)

type MongoUserStore struct {
	s   *mgo.Session
	d   string // database
	c   string // collection
	url string
}

// NewMongoUserStore returns an unconnected MongoUserStore
// that satisfies the UserStore interface.  You must call
// Connect() on the returned store before using it.
func NewMongoUserStore(url string) *MongoUserStore {
	return &MongoUserStore{url: url}
}

func (m *MongoUserStore) Connect() error {
	s, err := mgo.Dial(m.url)
	if err != nil {
		panic(err)
	}
	m.s = s

	// TODO(BJK) database and collection as env vars, or params to New()?
	m.d = "athens"
	m.c = "users"

	index := mgo.Index{
		Key:        []string{"provider", "userid"},
		Unique:     true,
		DropDups:   true,
		Background: true,
		Sparse:     true,
	}
	c := m.s.DB(m.d).C(m.c)
	err = c.EnsureIndex(index)
	if err != nil {
		panic(err)
	}
	return err
}

// Get returns a user from the Mongo Store
func (m *MongoUserStore) Get(id, provider string) (*user.User, error) {
	c := m.s.DB(m.d).C(m.c)
	result := &user.User{}
	err := c.Find(bson.M{"provider": provider, "userid": id}).One(result)
	if err != nil {
		if strings.Contains(err.Error(), "not found") {
			err = user.ErrNotFound
		}
	}
	return result, err
}

// Save adds a user to the Mongo Store
func (m *MongoUserStore) Save(u *user.User) error {
	c := m.s.DB(m.d).C(m.c)
	return c.Insert(u)
}

// Update updates a user in the Mongo Store
func (m *MongoUserStore) Update(*user.User) error {
	return errors.New("not implemented")
}
