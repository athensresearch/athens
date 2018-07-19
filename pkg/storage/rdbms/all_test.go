package rdbms

import (
	"testing"

	"github.com/gobuffalo/suite"
	"github.com/gomods/athens/pkg/storage"
)

type RDBMSTestSuite struct {
	*suite.Model
	storage storage.BackendConnector
}

func (rd *RDBMSTestSuite) SetupTest() {
	rd.storage = &ModuleStore{conn: rd.DB}
	rd.Model.SetupTest()
}

func Test_ActionSuite(t *testing.T) {
	suite.Run(t, &RDBMSTestSuite{Model: suite.NewModel()})
}
