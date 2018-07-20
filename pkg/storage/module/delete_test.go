package module

import (
	"context"
	"errors"
	"testing"
	"time"

	"github.com/gobuffalo/envy"
	multierror "github.com/hashicorp/go-multierror"
	"github.com/stretchr/testify/suite"
)

type DeleteTests struct {
	suite.Suite
}

func TestDelete(t *testing.T) {
	suite.Run(t, new(DeleteTests))
}

func (d *DeleteTests) SetupTest() {
	envy.Set("ATHENS_TIMEOUT", "1")
}

func (d *DeleteTests) TearDownTest() {
	envy.Set("ATHENS_TIMEOUT", "300")
}

func (d *DeleteTests) TestDeleteTimeout() {
	r := d.Require()

	err := Delete(context.Background(), "mx", "1.1.1", delWithTimeout)

	me := err.(*multierror.Error)
	r.Equal(3, len(me.WrappedErrors()))
	r.Contains(me.Error(), "deleting mx.1.1.1.info failed: context deadline exceeded")
	r.Contains(me.Error(), "deleting mx.1.1.1.zip failed: context deadline exceeded")
	r.Contains(me.Error(), "deleting mx.1.1.1.mod failed: context deadline exceeded")
}

func (d *DeleteTests) TestDeleteError() {
	r := d.Require()

	err := Delete(context.Background(), "mx", "1.1.1", delWithErr)

	me := err.(*multierror.Error)
	r.Equal(3, len(me.WrappedErrors()))
	r.Contains(me.Error(), "some err")
	r.Contains(me.Error(), "some err")
	r.Contains(me.Error(), "some err")
}

func delWithTimeout(ctx context.Context, path string) error {
	time.Sleep(2 * time.Second)
	return nil
}

func delWithErr(ctx context.Context, path string) error {
	return errors.New("some err")
}
