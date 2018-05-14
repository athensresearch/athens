package actions

import (
	"fmt"

	"github.com/gobuffalo/envy"
	"github.com/gomods/athens/pkg/eventlog"
	"github.com/gomods/athens/pkg/eventlog/mongo"
)

func newEventlog() (eventlog.Reader, error) {
	mongoURI, err := envy.MustGet("ATHENS_MONGO_STORAGE_URL")
	if err != nil {
		return nil, fmt.Errorf("missing mongo URL (%s)", err)
	}
	l, err := mongo.NewLog(mongoURI)
	return l, err
}
