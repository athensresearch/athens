package env

import (
	"os"
)

// BasicAuth returns BASIC_AUTH_USER
// and BASIC_AUTH_PASSWORD and ok if either
// of them are empty
func BasicAuth() (user, pass string, ok bool) {
	user = os.Getenv("BASIC_AUTH_USER")
	pass = os.Getenv("BASIC_AUTH_PASS")
	ok = user != "" && pass != ""
	return user, pass, ok
}
