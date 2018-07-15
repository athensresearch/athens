package pop

import (
	"fmt"

	"github.com/gobuffalo/pop/columns"
	"github.com/gobuffalo/uuid"
	"github.com/gobuffalo/validate"
)

// Reload fetch fresh data for a given model, using its ID.
func (c *Connection) Reload(model interface{}) error {
	sm := Model{Value: model}
	return sm.iterate(func(m *Model) error {
		return c.Find(m.Value, m.ID())
	})
}

// Exec runs the given query.
func (q *Query) Exec() error {
	return q.Connection.timeFunc("Exec", func() error {
		sql, args := q.ToSQL(nil)
		Log(sql, args...)
		_, err := q.Connection.Store.Exec(sql, args...)
		return err
	})
}

// ExecWithCount runs the given query, and returns the amount of
// affected rows.
func (q *Query) ExecWithCount() (int, error) {
	count := int64(0)
	return int(count), q.Connection.timeFunc("Exec", func() error {
		sql, args := q.ToSQL(nil)
		Log(sql, args...)
		result, err := q.Connection.Store.Exec(sql, args...)
		if err != nil {
			return err
		}

		count, err = result.RowsAffected()
		return err
	})
}

// ValidateAndSave applies validation rules on the given entry, then save it
// if the validation succeed, excluding the given columns.
func (c *Connection) ValidateAndSave(model interface{}, excludeColumns ...string) (*validate.Errors, error) {
	sm := &Model{Value: model}
	verrs, err := sm.validateSave(c)
	if err != nil {
		return verrs, err
	}
	if verrs.HasAny() {
		return verrs, nil
	}
	return verrs, c.Save(model, excludeColumns...)
}

var emptyUUID = uuid.Nil.String()

// Save wraps the Create and Update methods. It executes a Create if no ID is provided with the entry;
// or issues an Update otherwise.
func (c *Connection) Save(model interface{}, excludeColumns ...string) error {
	sm := &Model{Value: model}
	return sm.iterate(func(m *Model) error {
		id := m.ID()
		if fmt.Sprint(id) == "0" || fmt.Sprint(id) == emptyUUID {
			return c.Create(m.Value, excludeColumns...)
		}
		return c.Update(m.Value, excludeColumns...)
	})
}

// ValidateAndCreate applies validation rules on the given entry, then creates it
// if the validation succeed, excluding the given columns.
func (c *Connection) ValidateAndCreate(model interface{}, excludeColumns ...string) (*validate.Errors, error) {
	if c.eager {
		return c.eagerValidateAndCreate(model, excludeColumns...)
	}

	sm := &Model{Value: model}
	verrs, err := sm.validateCreate(c)
	if err != nil {
		return verrs, err
	}
	if verrs.HasAny() {
		return verrs, nil
	}
	return verrs, c.Create(model, excludeColumns...)
}

// Create add a new given entry to the database, excluding the given columns.
// It updates `created_at` and `updated_at` columns automatically.
func (c *Connection) Create(model interface{}, excludeColumns ...string) error {
	if c.eager {
		return c.eagerCreate(model, excludeColumns...)
	}

	sm := &Model{Value: model}
	return sm.iterate(func(m *Model) error {
		return c.timeFunc("Create", func() error {
			var err error
			if err = m.beforeSave(c); err != nil {
				return err
			}

			if err = m.beforeCreate(c); err != nil {
				return err
			}

			cols := columns.ForStructWithAlias(m.Value, m.TableName(), m.As)

			if sm.TableName() == m.TableName() {
				cols.Remove(excludeColumns...)
			}

			m.touchCreatedAt()
			m.touchUpdatedAt()

			if err = c.Dialect.Create(c.Store, m, cols); err != nil {
				return err
			}

			if err = m.afterCreate(c); err != nil {
				return err
			}

			return m.afterSave(c)
		})
	})
}

// ValidateAndUpdate applies validation rules on the given entry, then update it
// if the validation succeed, excluding the given columns.
func (c *Connection) ValidateAndUpdate(model interface{}, excludeColumns ...string) (*validate.Errors, error) {
	sm := &Model{Value: model}
	verrs, err := sm.validateUpdate(c)
	if err != nil {
		return verrs, err
	}
	if verrs.HasAny() {
		return verrs, nil
	}
	return verrs, c.Update(model, excludeColumns...)
}

// Update writes changes from an entry to the database, excluding the given columns.
// It updates the `updated_at` column automatically.
func (c *Connection) Update(model interface{}, excludeColumns ...string) error {
	sm := &Model{Value: model}
	return sm.iterate(func(m *Model) error {
		return c.timeFunc("Update", func() error {
			var err error

			if err = m.beforeSave(c); err != nil {
				return err
			}
			if err = m.beforeUpdate(c); err != nil {
				return err
			}

			cols := columns.ForStructWithAlias(model, m.TableName(), m.As)
			cols.Remove("id", "created_at")

			if m.TableName() == sm.TableName() {
				cols.Remove(excludeColumns...)
			}

			m.touchUpdatedAt()

			if err = c.Dialect.Update(c.Store, m, cols); err != nil {
				return err
			}
			if err = m.afterUpdate(c); err != nil {
				return err
			}

			return m.afterSave(c)
		})
	})
}

// Destroy deletes a given entry from the database
func (c *Connection) Destroy(model interface{}) error {
	sm := &Model{Value: model}
	return sm.iterate(func(m *Model) error {
		return c.timeFunc("Destroy", func() error {
			var err error

			if err = m.beforeDestroy(c); err != nil {
				return err
			}
			if err = c.Dialect.Destroy(c.Store, m); err != nil {
				return err
			}

			return m.afterDestroy(c)
		})
	})
}
