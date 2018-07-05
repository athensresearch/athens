package models

import (
	"log"

	"github.com/gobuffalo/pop"
	"github.com/gomods/athens/pkg/config/env"
)

// DB is a connection to your database to be used
// throughout your application.
var DB *pop.Connection

func init() {
	var err error
	env := env.GoEnvironmentWithDefault("development")
	DB, err = pop.Connect(env)
	if err != nil {
		log.Fatal(err)
	}
	pop.Debug = env == "development"
}
