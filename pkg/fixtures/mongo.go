package fixtures

import (
	"github.com/globalsign/mgo"
	"github.com/stretchr/testify/suite"
	"github.com/technosophos/moniker"
)

var names = moniker.NewAlliterator()

// DefaultMongoURL is the default Mongo URL for testing
const DefaultMongoURL = "127.0.0.1:27017"

// Mongo is a text fixture for use with github.com/stretchr/testify/suite tests
//
// use it like this:
//
//	type MyTests struct {
//		*fixtures.Mongo
//	}
//
//	func RunMyTests(t *testing.T) {
//		suite.Run(t, &MyTests{Mongo: New(DefaultURL)})
//	}
type Mongo struct {
	suite.Suite
	url    string
	dbName string
	DB     *mgo.Database
}

// SetupTest creates a new mongo connection and DB, and attaches it to a
// session before each test executes
//
// This implements the SetupTestSuite interface
func (m *Mongo) SetupTest() {
	sess, err := mgo.Dial(m.url)
	m.Require().NoError(err)
	m.DB = sess.DB(m.dbName)
}

// TearDownTest drops the database that was created in SetupTest
//
// This implements the TearDownTestSuite interface
func (m *Mongo) TearDownTest() {
	m.Require().NoError(m.DB.DropDatabase())
}

// NewMongo creates a new Mongo test fixture
func NewMongo(url string) *Mongo {
	return &Mongo{
		url:    url,
		dbName: names.NameSep("-") + "-athens-testing",
	}
}
