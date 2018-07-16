package actions

import (
	"fmt"
	"log"
	"os"

	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/pkg/user"
	"github.com/markbates/goth"
	"github.com/markbates/goth/gothic"
	"github.com/markbates/goth/providers/github"
)

func init() {
	app, err := App()
	if err != nil {
		log.Fatal(err)
	}
	gothic.Store = app.SessionStore

	goth.UseProviders(
		github.New(os.Getenv("GITHUB_KEY"), os.Getenv("GITHUB_SECRET"), fmt.Sprintf("%s%s", app.Host, "/auth/github/callback")),
	)
}

func authCallback(store user.Store) func(c buffalo.Context) error {
	return func(c buffalo.Context) error {
		usr, err := gothic.CompleteUserAuth(c.Response(), c.Request())
		if err != nil {
			return c.Render(401, proxy.JSON(err.Error()))
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
					return c.Render(500, proxy.JSON(e.Error()))
				}
			}
			return c.Render(500, proxy.JSON(err.Error()))
		}
		return c.Render(200, proxy.JSON(u))
	}
}
