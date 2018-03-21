package fix

import (
	"io/ioutil"
	"time"

	"github.com/gobuffalo/packr"
	"github.com/gobuffalo/plush"
	"github.com/gobuffalo/uuid"
	"github.com/pkg/errors"
)

func render(file packr.File) (string, error) {
	b, err := ioutil.ReadAll(file)
	if err != nil {
		return "", errors.WithStack(err)
	}

	return plush.Render(string(b), plush.NewContextWith(map[string]interface{}{
		"uuid": func() uuid.UUID {
			u, _ := uuid.NewV4()
			return u
		},
		"uuidNamed": uuidNamed,
		"now":       time.Now,
	}))

}

func uuidNamed(name string, help plush.HelperContext) uuid.UUID {
	u, _ := uuid.NewV4()
	if ux, ok := help.Value(name).(uuid.UUID); ok {
		return ux
	}
	help.Set(name, u)
	return u
}
