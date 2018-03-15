package user

import (
	"errors"
	"time"

	"github.com/markbates/goth"
)

var ErrNotFound = errors.New("user not found")

// UserStore provides an interface for storing Users
type Store interface {
	Get(id, provider string) (*User, error)
	Save(*User) error
	Update(*User) error
}

// User contains the information common amongst most OAuth and OAuth2 providers.
// All of the "raw" datafrom the provider can be found in the `RawData` field.
type User struct {
	RawData           map[string]interface{}
	Provider          string
	Email             string
	Name              string
	FirstName         string `bson:"first_name"`
	LastName          string `bson:"last_name"`
	NickName          string `bson:"nick_name"`
	Description       string
	UserID            string `bson:"userid"`
	AvatarURL         string `bson:"avatar_url"`
	Location          string
	AccessToken       string    `bson:"access_token"`
	AccessTokenSecret string    `bson:"access_token_secret"`
	RefreshToken      string    `bson:"refresh_token"`
	ExpiresAt         time.Time `bson:"expires_at"`
}

// FromGothic converts a Gothic user to an Athens user
func FromGothic(g *goth.User) *User {
	u := &User{
		RawData:           g.RawData,
		Provider:          g.Provider,
		Email:             g.Email,
		Name:              g.Name,
		FirstName:         g.FirstName,
		LastName:          g.LastName,
		NickName:          g.NickName,
		Description:       g.Description,
		UserID:            g.UserID,
		AvatarURL:         g.AvatarURL,
		Location:          g.Location,
		AccessToken:       g.AccessToken,
		AccessTokenSecret: g.AccessTokenSecret,
		RefreshToken:      g.RefreshToken,
		ExpiresAt:         g.ExpiresAt,
	}
	return u
}
