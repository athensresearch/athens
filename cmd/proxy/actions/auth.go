package actions

import (
	"fmt"
	"os"

	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/user"
	"github.com/markbates/goth"
	"github.com/markbates/goth/gothic"
	"github.com/markbates/goth/providers/github"
)

func init() {
	gothic.Store = App().SessionStore

	goth.UseProviders(
		github.New(os.Getenv("GITHUB_KEY"), os.Getenv("GITHUB_SECRET"), fmt.Sprintf("%s%s", App().Host, "/auth/github/callback")),
	)
}

func authCallback(store user.Store) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		usr, err := gothic.CompleteUserAuth(c.Response(), c.Request())
		if err != nil {
			return c.Error(401, err)
		}
		// Do something with the user, maybe register them/sign them in
		var u *user.User
		u, err = store.Get(usr.UserID, usr.Provider)
		if err != nil {
			if err == user.ErrNotFound {
				// insert the user
				u = user.FromGothic(&usr)
				e := store.Save(u)
				if e != nil {
					return c.Error(500, e)
				}
			}
			return c.Error(500, err)
		}
		return c.Render(200, registry.JSON(u))
	}
}
