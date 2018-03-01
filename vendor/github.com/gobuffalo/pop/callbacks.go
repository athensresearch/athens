package pop

import (
	"reflect"

	"github.com/pkg/errors"
	"golang.org/x/sync/errgroup"
)

type afterFindable interface {
	AfterFind(*Connection) error
}

func (m *Model) afterFind(c *Connection) error {
	if x, ok := m.Value.(afterFindable); ok {
		if err := x.AfterFind(c); err != nil {
			return errors.WithStack(err)
		}
	}

	// if the "model" is a slice/array we want
	// to loop through each of the elements in the collection
	// and call AfterFind on them if they exist.
	rv := reflect.Indirect(reflect.ValueOf(m.Value))
	kind := rv.Kind()
	if kind != reflect.Slice && kind != reflect.Array {
		return nil
	}

	wg := &errgroup.Group{}
	for i := 0; i < rv.Len(); i++ {
		func(i int) {
			wg.Go(func() error {
				y := rv.Index(i)
				y = y.Addr()
				if x, ok := y.Interface().(afterFindable); ok {
					return x.AfterFind(c)
				}
				return nil
			})
		}(i)
	}

	return wg.Wait()
}

type beforeSaveable interface {
	BeforeSave(*Connection) error
}

func (m *Model) beforeSave(c *Connection) error {
	if x, ok := m.Value.(beforeSaveable); ok {
		return x.BeforeSave(c)
	}
	return nil
}

type beforeCreateable interface {
	BeforeCreate(*Connection) error
}

func (m *Model) beforeCreate(c *Connection) error {
	if x, ok := m.Value.(beforeCreateable); ok {
		return x.BeforeCreate(c)
	}
	return nil
}

type beforeUpdateable interface {
	BeforeUpdate(*Connection) error
}

func (m *Model) beforeUpdate(c *Connection) error {
	if x, ok := m.Value.(beforeUpdateable); ok {
		return x.BeforeUpdate(c)
	}
	return nil
}

type beforeDestroyable interface {
	BeforeDestroy(*Connection) error
}

func (m *Model) beforeDestroy(c *Connection) error {
	if x, ok := m.Value.(beforeDestroyable); ok {
		return x.BeforeDestroy(c)
	}
	return nil
}

type afterDestroyable interface {
	AfterDestroy(*Connection) error
}

func (m *Model) afterDestroy(c *Connection) error {
	if x, ok := m.Value.(afterDestroyable); ok {
		return x.AfterDestroy(c)
	}
	return nil
}

type afterUpdateable interface {
	AfterUpdate(*Connection) error
}

func (m *Model) afterUpdate(c *Connection) error {
	if x, ok := m.Value.(afterUpdateable); ok {
		return x.AfterUpdate(c)
	}
	return nil
}

type afterCreateable interface {
	AfterCreate(*Connection) error
}

func (m *Model) afterCreate(c *Connection) error {
	if x, ok := m.Value.(afterCreateable); ok {
		return x.AfterCreate(c)
	}
	return nil
}

type afterSaveable interface {
	AfterSave(*Connection) error
}

func (m *Model) afterSave(c *Connection) error {
	if x, ok := m.Value.(afterSaveable); ok {
		return x.AfterSave(c)
	}
	return nil
}
