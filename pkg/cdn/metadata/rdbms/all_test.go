package rdbms

import (
	"testing"

	"github.com/gobuffalo/suite"
	"github.com/gomods/athens/pkg/cdn/metadata"
)

const (
	module      = "testmodule"
	redirectURL = "https://mycdn.com/gomods.io/my/testmodule"
)

type RDBMSTestSuite struct {
	*suite.Model
	storage metadata.StorageConnector
}

func (rd *RDBMSTestSuite) SetupTest() {
	rd.storage = &MetadataStore{conn: rd.DB}
	rd.Model.SetupTest()
}

func Test_ActionSuite(t *testing.T) {
	suite.Run(t, &RDBMSTestSuite{Model: suite.NewModel()})
}
