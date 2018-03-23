package mongo

import (
	"testing"

	"github.com/gomods/athens/pkg/fixtures"
	"github.com/stretchr/testify/suite"
)

type MongoTests struct {
	*fixtures.Mongo
}

func RunMongoTests(t *testing.T) {
	suite.Run(t, &MongoTests{Mongo: fixtures.NewMongo(fixtures.DefaultMongoURL)})
}

// TODO: add round-trip tests when a mongo saver is done
// (https://github.com/gomods/athens/issues/50)
