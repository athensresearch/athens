package suite

import (
	"testing"

	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/middleware/csrf"
	"github.com/markbates/willie"
	"github.com/stretchr/testify/suite"
)

type Action struct {
	*Model
	Session *buffalo.Session
	Willie  *willie.Willie
	App     *buffalo.App
	csrf    buffalo.MiddlewareFunc
}

func NewAction(app *buffalo.App) *Action {
	as := &Action{
		App:   app,
		Model: NewModel(),
	}
	return as
}

func Run(t *testing.T, s suite.TestingSuite) {
	suite.Run(t, s)
}

func (as *Action) HTML(u string, args ...interface{}) *willie.Request {
	return as.Willie.HTML(u, args...)
}

func (as *Action) JSON(u string, args ...interface{}) *willie.JSON {
	return as.Willie.JSON(u, args...)
}

func (as *Action) SetupTest() {
	as.App.SessionStore = newSessionStore()
	s, _ := as.App.SessionStore.New(nil, as.App.SessionName)
	as.Session = &buffalo.Session{
		Session: s,
	}

	as.Model.SetupTest()
	as.csrf = csrf.New
	csrf.New = func(next buffalo.Handler) buffalo.Handler {
		return func(c buffalo.Context) error {
			return next(c)
		}
	}
	as.Willie = willie.New(as.App)
}

func (as *Action) TearDownTest() {
	csrf.New = as.csrf
	as.Model.TearDownTest()
}
