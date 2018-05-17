package actions

import (
	"testing"

	"github.com/gobuffalo/suite"
	"github.com/gomods/athens/pkg/payloads"
)

type ActionSuite struct {
	*suite.Action
}

func Test_ActionSuite(t *testing.T) {
	as := &ActionSuite{suite.NewAction(App())}
	suite.Run(t, as)
}

func (as *ActionSuite) Test_Cache_Miss_Route() {
	mod := &payloads.Module{}
	mod.Name = "moduleName"
	mod.Version = "1.0.0"
	res := as.JSON("/cachemiss").Post(mod)
	as.Equal(200, res.Code)
	as.Contains(res.Body.String(), "\"module\":\"moduleName\",\"version\":\"1.0.0\"")
}
