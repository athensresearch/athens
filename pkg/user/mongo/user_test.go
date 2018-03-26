package mongo

import (
	"testing"
	"time"

	"github.com/gomods/athens/pkg/user"
)

var m *UserStore

func TestConnect(t *testing.T) {
	setup(t)
}

func TestCreate(t *testing.T) {
	setup(t)
	defer m.s.Close()
	g := &user.User{}
	g.AccessToken = "accesstoken"
	g.AccessTokenSecret = "accesstokensecret"
	g.AvatarURL = "https://avatar.url"
	g.Description = "description"
	g.Email = "goth@goth.user"
	g.ExpiresAt = time.Now()
	g.FirstName = "Goth"
	g.LastName = "User"
	g.Location = "Buffalo, NY"
	g.Name = "Goth User"
	g.NickName = "guser"
	g.Provider = "github"
	g.RefreshToken = "refreshtoken"
	g.UserID = "012345678"
	err := m.Save(g)
	if err != nil {
		t.Error(err)
	}
}

func setup(t *testing.T) {
	var err error
	if m == nil {
		m = NewUserStore("mongodb://127.0.0.1:27017")
		err = m.Connect()
		if err != nil {
			t.Error(err)
			t.FailNow()
		}
		_, err := m.s.DB(m.d).C(m.c).RemoveAll(nil)
		if err != nil {
			t.Error(err)
			t.FailNow()
		}

	}
}
