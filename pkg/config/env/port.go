package env

import "github.com/gobuffalo/envy"

// Port returns the PORT env var that a server (Olympus/Zeus) should
// run on. Buffalo uses the PORT environment so this keeps it consistent
// but can also be used directly as a Buffalo service option
func Port(value string) string {
	return envy.Get("PORT", value)
}
