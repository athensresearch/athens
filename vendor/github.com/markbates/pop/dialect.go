package pop

import (
	"encoding/gob"
	"fmt"
	"io"

	"github.com/markbates/pop/columns"
	"github.com/markbates/pop/fizz"
	"github.com/pkg/errors"
	uuid "github.com/satori/go.uuid"
)

func init() {
	gob.Register(uuid.UUID{})
}

type dialect interface {
	URL() string
	MigrationURL() string
	Details() *ConnectionDetails
	TranslateSQL(string) string
	Create(store, *Model, columns.Columns) error
	Update(store, *Model, columns.Columns) error
	Destroy(store, *Model) error
	SelectOne(store, *Model, Query) error
	SelectMany(store, *Model, Query) error
	CreateDB() error
	DropDB() error
	DumpSchema(io.Writer) error
	LoadSchema(io.Reader) error
	FizzTranslator() fizz.Translator
	Lock(func() error) error
	TruncateAll(*Connection) error
}

func genericCreate(s store, model *Model, cols columns.Columns) error {
	keyType := model.PrimaryKeyType()
	switch keyType {
	case "int", "int64":
		var id int64
		w := cols.Writeable()
		query := fmt.Sprintf("INSERT INTO %s (%s) VALUES (%s)", model.TableName(), w.String(), w.SymbolizedString())
		Log(query)
		res, err := s.NamedExec(query, model.Value)
		if err != nil {
			return errors.WithStack(err)
		}
		id, err = res.LastInsertId()
		if err == nil {
			model.setID(id)
		}
		if err != nil {
			return errors.WithStack(err)
		}
		return nil
	case "UUID":
		if model.ID() == emptyUUID {
			u, err := uuid.NewV4()
			if err != nil {
				return errors.WithStack(err)
			}
			model.setID(u)
		}
		w := cols.Writeable()
		w.Add("id")
		query := fmt.Sprintf("INSERT INTO %s (%s) VALUES (%s)", model.TableName(), w.String(), w.SymbolizedString())
		Log(query)
		stmt, err := s.PrepareNamed(query)
		if err != nil {
			return errors.WithStack(err)
		}
		_, err = stmt.Exec(model.Value)
		if err != nil {
			return errors.WithStack(err)
		}
		return nil
	}
	return errors.Errorf("can not use %s as a primary key type!", keyType)
}

func genericUpdate(s store, model *Model, cols columns.Columns) error {
	stmt := fmt.Sprintf("UPDATE %s SET %s where %s", model.TableName(), cols.Writeable().UpdateString(), model.whereID())
	Log(stmt)
	_, err := s.NamedExec(stmt, model.Value)
	if err != nil {
		return errors.WithStack(err)
	}
	return nil
}

func genericDestroy(s store, model *Model) error {
	stmt := fmt.Sprintf("DELETE FROM %s WHERE %s", model.TableName(), model.whereID())
	err := genericExec(s, stmt)
	if err != nil {
		return errors.WithStack(err)
	}
	return nil
}

func genericExec(s store, stmt string) error {
	Log(stmt)
	_, err := s.Exec(stmt)
	if err != nil {
		return errors.WithStack(err)
	}
	return nil
}

func genericSelectOne(s store, model *Model, query Query) error {
	sql, args := query.ToSQL(model)
	Log(sql, args...)
	err := s.Get(model.Value, sql, args...)
	if err != nil {
		return errors.WithStack(err)
	}
	return nil
}

func genericSelectMany(s store, models *Model, query Query) error {
	sql, args := query.ToSQL(models)
	Log(sql, args...)
	err := s.Select(models.Value, sql, args...)
	if err != nil {
		return errors.WithStack(err)
	}
	return nil
}
