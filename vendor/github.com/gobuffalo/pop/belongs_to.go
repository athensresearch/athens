package pop

import "fmt"

// BelongsTo adds a "where" clause based on the "ID" of the
// "model" passed into it.
func (c *Connection) BelongsTo(model interface{}) *Query {
	return Q(c).BelongsTo(model)
}

// BelongsToAs adds a "where" clause based on the "ID" of the
// "model" passed into it using an alias.
func (c *Connection) BelongsToAs(model interface{}, as string) *Query {
	return Q(c).BelongsToAs(model, as)
}

// BelongsTo adds a "where" clause based on the "ID" of the
// "model" passed into it.
func (q *Query) BelongsTo(model interface{}) *Query {
	m := &Model{Value: model}
	q.Where(fmt.Sprintf("%s = ?", m.associationName()), m.ID())
	return q
}

// BelongsToAs adds a "where" clause based on the "ID" of the
// "model" passed into it, using an alias.
func (q *Query) BelongsToAs(model interface{}, as string) *Query {
	m := &Model{Value: model}
	q.Where(fmt.Sprintf("%s = ?", as), m.ID())
	return q
}

// BelongsToThrough adds a "where" clause that connects the "bt" model
// through the associated "thru" model.
func (c *Connection) BelongsToThrough(bt, thru interface{}) *Query {
	return Q(c).BelongsToThrough(bt, thru)
}

// BelongsToThrough adds a "where" clause that connects the "bt" model
// through the associated "thru" model.
func (q *Query) BelongsToThrough(bt, thru interface{}) *Query {
	q.belongsToThroughClauses = append(q.belongsToThroughClauses, belongsToThroughClause{
		BelongsTo: &Model{Value: bt},
		Through:   &Model{Value: thru},
	})
	return q
}
