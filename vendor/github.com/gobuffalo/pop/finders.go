package pop

import (
	"database/sql"
	"fmt"
	"reflect"
	"regexp"
	"strconv"
	"strings"

	"github.com/gobuffalo/pop/associations"
	"github.com/gobuffalo/uuid"
	"github.com/pkg/errors"
)

var rLimitOffset = regexp.MustCompile("(?i)(limit [0-9]+ offset [0-9]+)$")
var rLimit = regexp.MustCompile("(?i)(limit [0-9]+)$")

// Find the first record of the model in the database with a particular id.
//
//	c.Find(&User{}, 1)
func (c *Connection) Find(model interface{}, id interface{}) error {
	q := Q(c)
	return q.Find(model, id)
}

// Find the first record of the model in the database with a particular id.
//
//	q.Find(&User{}, 1)
func (q *Query) Find(model interface{}, id interface{}) error {
	m := &Model{Value: model}
	idq := fmt.Sprintf("%s.id = ?", m.TableName())
	switch t := id.(type) {
	case uuid.UUID:
		return q.Where(idq, t.String()).First(model)
	case string:
		var err error
		id, err = strconv.Atoi(t)
		if err != nil {
			return q.Where(idq, t).First(model)
		}
	}

	return q.Where(idq, id).First(model)
}

// First record of the model in the database that matches the query.
//
//	c.First(&User{})
func (c *Connection) First(model interface{}) error {
	q := Q(c)
	return q.First(model)
}

// First record of the model in the database that matches the query.
//
//	q.Where("name = ?", "mark").First(&User{})
func (q *Query) First(model interface{}) error {
	err := q.Connection.timeFunc("First", func() error {
		q.Limit(1)
		m := &Model{Value: model}
		if err := q.Connection.Dialect.SelectOne(q.Connection.Store, m, *q); err != nil {
			return err
		}
		return m.afterFind(q.Connection)
	})

	if err != nil {
		return err
	}

	if q.eager {
		err = q.eagerAssociations(model)
		q.disableEager()
		return err
	}
	return nil
}

// Last record of the model in the database that matches the query.
//
//	c.Last(&User{})
func (c *Connection) Last(model interface{}) error {
	q := Q(c)
	return q.Last(model)
}

// Last record of the model in the database that matches the query.
//
//	q.Where("name = ?", "mark").Last(&User{})
func (q *Query) Last(model interface{}) error {
	err := q.Connection.timeFunc("Last", func() error {
		q.Limit(1)
		q.Order("created_at DESC, id DESC")
		m := &Model{Value: model}
		if err := q.Connection.Dialect.SelectOne(q.Connection.Store, m, *q); err != nil {
			return err
		}
		return m.afterFind(q.Connection)
	})

	if err != nil {
		return err
	}

	if q.eager {
		err = q.eagerAssociations(model)
		q.disableEager()
		return err
	}

	return nil
}

// All retrieves all of the records in the database that match the query.
//
//	c.All(&[]User{})
func (c *Connection) All(models interface{}) error {
	q := Q(c)
	return q.All(models)
}

// All retrieves all of the records in the database that match the query.
//
//	q.Where("name = ?", "mark").All(&[]User{})
func (q *Query) All(models interface{}) error {
	err := q.Connection.timeFunc("All", func() error {
		m := &Model{Value: models}
		err := q.Connection.Dialect.SelectMany(q.Connection.Store, m, *q)
		if err != nil {
			return err
		}
		err = q.paginateModel(models)
		if err != nil {
			return err
		}
		return m.afterFind(q.Connection)
	})

	if err != nil {
		return err
	}

	if q.eager {
		err = q.eagerAssociations(models)
		q.disableEager()
		return err
	}

	return nil
}

func (q *Query) paginateModel(models interface{}) error {
	if q.Paginator == nil {
		return nil
	}

	ct, err := q.Count(models)
	if err != nil {
		return err
	}

	q.Paginator.TotalEntriesSize = ct
	st := reflect.ValueOf(models).Elem()
	q.Paginator.CurrentEntriesSize = st.Len()
	q.Paginator.TotalPages = (q.Paginator.TotalEntriesSize / q.Paginator.PerPage)
	if q.Paginator.TotalEntriesSize%q.Paginator.PerPage > 0 {
		q.Paginator.TotalPages = q.Paginator.TotalPages + 1
	}
	return nil
}

// Load loads all association or the fields specified in params for
// an already loaded model.
//
// tx.First(&u)
// tx.Load(&u)
func (c *Connection) Load(model interface{}, fields ...string) error {
	q := Q(c)
	q.eagerFields = fields
	err := q.eagerAssociations(model)
	q.disableEager()
	return err
}

func (q *Query) eagerAssociations(model interface{}) error {
	var err error

	// eagerAssociations for a slice or array model passed as a param.
	v := reflect.ValueOf(model)
	if reflect.Indirect(v).Kind() == reflect.Slice ||
		reflect.Indirect(v).Kind() == reflect.Array {
		v = v.Elem()
		for i := 0; i < v.Len(); i++ {
			err = q.eagerAssociations(v.Index(i).Addr().Interface())
			if err != nil {
				return err
			}
		}
		return err
	}

	assos, err := associations.ForStruct(model, q.eagerFields...)
	if err != nil {
		return err
	}

	//disable eager mode for current connection.
	q.eager = false
	q.Connection.eager = false

	for _, association := range assos {
		if association.Skipped() {
			continue
		}

		query := Q(q.Connection)

		whereCondition, args := association.Constraint()
		query = query.Where(whereCondition, args...)

		// validates if association is Sortable
		sortable := (*associations.AssociationSortable)(nil)
		t := reflect.TypeOf(association)
		if t.Implements(reflect.TypeOf(sortable).Elem()) {
			m := reflect.ValueOf(association).MethodByName("OrderBy")
			out := m.Call([]reflect.Value{})
			orderClause := out[0].String()
			if orderClause != "" {
				query = query.Order(orderClause)
			}
		}

		sqlSentence, args := query.ToSQL(&Model{Value: association.Interface()})
		query = query.RawQuery(sqlSentence, args...)

		if association.Kind() == reflect.Slice || association.Kind() == reflect.Array {
			err = query.All(association.Interface())
		}

		if association.Kind() == reflect.Struct {
			err = query.First(association.Interface())
		}

		if err != nil && errors.Cause(err) != sql.ErrNoRows {
			return err
		}

		// load all inner associations.
		innerAssociations := association.InnerAssociations()
		for _, inner := range innerAssociations {
			v = reflect.Indirect(reflect.ValueOf(model)).FieldByName(inner.Name)
			innerQuery := Q(query.Connection)
			innerQuery.eagerFields = []string{inner.Fields}
			err = innerQuery.eagerAssociations(v.Addr().Interface())
			if err != nil {
				return err
			}
		}
	}
	return nil
}

// Exists returns true/false if a record exists in the database that matches
// the query.
//
// 	q.Where("name = ?", "mark").Exists(&User{})
func (q *Query) Exists(model interface{}) (bool, error) {
	tmpQuery := Q(q.Connection)
	q.Clone(tmpQuery) //avoid meddling with original query

	var res bool

	err := tmpQuery.Connection.timeFunc("Exists", func() error {
		tmpQuery.Paginator = nil
		tmpQuery.orderClauses = clauses{}
		tmpQuery.limitResults = 0
		query, args := tmpQuery.ToSQL(&Model{Value: model})

		// when query contains custom selected fields / executed using RawQuery,
		// sql may already contains limit and offset
		if rLimitOffset.MatchString(query) {
			foundLimit := rLimitOffset.FindString(query)
			query = query[0 : len(query)-len(foundLimit)]
		} else if rLimit.MatchString(query) {
			foundLimit := rLimit.FindString(query)
			query = query[0 : len(query)-len(foundLimit)]
		}

		existsQuery := fmt.Sprintf("SELECT EXISTS (%s)", query)
		Log(existsQuery, args...)
		return q.Connection.Store.Get(&res, existsQuery, args...)
	})
	return res, err
}

// Count the number of records in the database.
//
//	c.Count(&User{})
func (c *Connection) Count(model interface{}) (int, error) {
	return Q(c).Count(model)
}

// Count the number of records in the database.
//
//	q.Where("name = ?", "mark").Count(&User{})
func (q Query) Count(model interface{}) (int, error) {
	return q.CountByField(model, "*")
}

// CountByField counts the number of records in the database, for a given field.
//
//	q.Where("sex = ?", "f").Count(&User{}, "name")
func (q Query) CountByField(model interface{}, field string) (int, error) {
	tmpQuery := Q(q.Connection)
	q.Clone(tmpQuery) //avoid meddling with original query

	res := &rowCount{}

	err := tmpQuery.Connection.timeFunc("CountByField", func() error {
		tmpQuery.Paginator = nil
		tmpQuery.orderClauses = clauses{}
		tmpQuery.limitResults = 0
		query, args := tmpQuery.ToSQL(&Model{Value: model})
		//when query contains custom selected fields / executed using RawQuery,
		//	sql may already contains limit and offset

		if rLimitOffset.MatchString(query) {
			foundLimit := rLimitOffset.FindString(query)
			query = query[0 : len(query)-len(foundLimit)]
		} else if rLimit.MatchString(query) {
			foundLimit := rLimit.FindString(query)
			query = query[0 : len(query)-len(foundLimit)]
		}

		countQuery := fmt.Sprintf("SELECT COUNT(%s) AS row_count FROM (%s) a", field, query)
		Log(countQuery, args...)
		return q.Connection.Store.Get(res, countQuery, args...)
	})
	return res.Count, err
}

type rowCount struct {
	Count int `db:"row_count"`
}

// Select allows to query only fields passed as parameter.
// c.Select("field1", "field2").All(&model)
// => SELECT field1, field2 FROM models
func (c *Connection) Select(fields ...string) *Query {
	return c.Q().Select(fields...)
}

// Select allows to query only fields passed as parameter.
// c.Select("field1", "field2").All(&model)
// => SELECT field1, field2 FROM models
func (q *Query) Select(fields ...string) *Query {
	for _, f := range fields {
		if strings.TrimSpace(f) != "" {
			q.addColumns = append(q.addColumns, f)
		}
	}
	return q
}
