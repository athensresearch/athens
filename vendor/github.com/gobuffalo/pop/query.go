package pop

import (
	"fmt"
	"strings"
)

// Query is the main value that is used to build up a query
// to be executed against the `Connection`.
type Query struct {
	RawSQL                  *clause
	limitResults            int
	addColumns              []string
	eager                   bool
	eagerFields             []string
	whereClauses            clauses
	orderClauses            clauses
	fromClauses             fromClauses
	belongsToThroughClauses belongsToThroughClauses
	joinClauses             joinClauses
	groupClauses            groupClauses
	havingClauses           havingClauses
	Paginator               *Paginator
	Connection              *Connection
}

// Clone will fill targetQ query with the connection used in q, if
// targetQ is not empty, Clone will override all the fields.
func (q *Query) Clone(targetQ *Query) {
	rawSQL := *q.RawSQL
	targetQ.RawSQL = &rawSQL

	targetQ.limitResults = q.limitResults
	targetQ.whereClauses = q.whereClauses
	targetQ.orderClauses = q.orderClauses
	targetQ.fromClauses = q.fromClauses
	targetQ.belongsToThroughClauses = q.belongsToThroughClauses
	targetQ.joinClauses = q.joinClauses
	targetQ.groupClauses = q.groupClauses
	targetQ.havingClauses = q.havingClauses

	if q.Paginator != nil {
		paginator := *q.Paginator
		targetQ.Paginator = &paginator
	}

	if q.Connection != nil {
		connection := *q.Connection
		targetQ.Connection = &connection
	}
}

// RawQuery will override the query building feature of Pop and will use
// whatever query you want to execute against the `Connection`. You can continue
// to use the `?` argument syntax.
//
//	c.RawQuery("select * from foo where id = ?", 1)
func (c *Connection) RawQuery(stmt string, args ...interface{}) *Query {
	return Q(c).RawQuery(stmt, args...)
}

// RawQuery will override the query building feature of Pop and will use
// whatever query you want to execute against the `Connection`. You can continue
// to use the `?` argument syntax.
//
//	q.RawQuery("select * from foo where id = ?", 1)
func (q *Query) RawQuery(stmt string, args ...interface{}) *Query {
	q.RawSQL = &clause{stmt, args}
	return q
}

// Eager will enable load associations of the model.
// by defaults loads all the associations on the model,
// but can take a variadic list of associations to load.
//
// 	c.Eager().Find(model, 1) // will load all associations for model.
// 	c.Eager("Books").Find(model, 1) // will load only Book association for model.
func (c *Connection) Eager(fields ...string) *Connection {
	con := c.copy()
	con.eager = true
	con.eagerFields = append(c.eagerFields, fields...)
	return con
}

// Eager will enable load associations of the model.
// by defaults loads all the associations on the model,
// but can take a variadic list of associations to load.
//
// 	q.Eager().Find(model, 1) // will load all associations for model.
// 	q.Eager("Books").Find(model, 1) // will load only Book association for model.
func (q *Query) Eager(fields ...string) *Query {
	q.eager = true
	q.eagerFields = append(q.eagerFields, fields...)
	return q
}

// disableEager disables eager mode for current query and Connection.
func (q *Query) disableEager() {
	q.Connection.eager, q.eager = false, false
	q.Connection.eagerFields, q.eagerFields = []string{}, []string{}
}

// Where will append a where clause to the query. You may use `?` in place of
// arguments.
//
// 	c.Where("id = ?", 1)
// 	q.Where("id in (?)", 1, 2, 3)
func (c *Connection) Where(stmt string, args ...interface{}) *Query {
	q := Q(c)
	return q.Where(stmt, args...)
}

// Where will append a where clause to the query. You may use `?` in place of
// arguments.
//
// 	q.Where("id = ?", 1)
// 	q.Where("id in (?)", 1, 2, 3)
func (q *Query) Where(stmt string, args ...interface{}) *Query {
	if q.RawSQL.Fragment != "" {
		fmt.Println("Warning: Query is setup to use raw SQL")
		return q
	}
	if inRegex.MatchString(stmt) {
		var inq []string
		for i := 0; i < len(args); i++ {
			inq = append(inq, "?")
		}
		qs := fmt.Sprintf("(%s)", strings.Join(inq, ","))
		stmt = strings.Replace(stmt, "(?)", qs, 1)
	}
	q.whereClauses = append(q.whereClauses, clause{stmt, args})
	return q
}

// Order will append an order clause to the query.
//
// 	c.Order("name desc")
func (c *Connection) Order(stmt string) *Query {
	return Q(c).Order(stmt)
}

// Order will append an order clause to the query.
//
// 	q.Order("name desc")
func (q *Query) Order(stmt string) *Query {
	if q.RawSQL.Fragment != "" {
		fmt.Println("Warning: Query is setup to use raw SQL")
		return q
	}
	q.orderClauses = append(q.orderClauses, clause{stmt, []interface{}{}})
	return q
}

// Limit will add a limit clause to the query.
func (c *Connection) Limit(limit int) *Query {
	return Q(c).Limit(limit)
}

// Limit will add a limit clause to the query.
func (q *Query) Limit(limit int) *Query {
	q.limitResults = limit
	return q
}

// Q will create a new "empty" query from the current connection.
func Q(c *Connection) *Query {
	return &Query{
		RawSQL:      &clause{},
		Connection:  c,
		eager:       c.eager,
		eagerFields: c.eagerFields,
	}
}

// ToSQL will generate SQL and the appropriate arguments for that SQL
// from the `Model` passed in.
func (q Query) ToSQL(model *Model, addColumns ...string) (string, []interface{}) {
	sb := q.toSQLBuilder(model, addColumns...)
	return sb.String(), sb.Args()
}

// ToSQLBuilder returns a new `SQLBuilder` that can be used to generate SQL,
// get arguments, and more.
func (q Query) toSQLBuilder(model *Model, addColumns ...string) *sqlBuilder {
	if len(q.addColumns) != 0 {
		addColumns = q.addColumns
	}
	return newSQLBuilder(q, model, addColumns...)
}
