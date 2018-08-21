package mongo

import (
	"testing"

	"github.com/stretchr/testify/suite"
)

type MongoTests struct {
	suite.Suite
}

func (d *MongoTests) SetupTest() {
	ms, err := newTestStore()

	d.Require().NoError(err)

	ms.s.DB(ms.d).C(ms.c).RemoveAll(nil)
}

func TestDiskStorage(t *testing.T) {
	suite.Run(t, new(MongoTests))
}
