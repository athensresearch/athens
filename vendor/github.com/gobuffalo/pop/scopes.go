package pop

// ScopeFunc applies a custom operation on a given `Query`
type ScopeFunc func(q *Query) *Query

// Scope the query by using a `ScopeFunc`
//
//	func ByName(name string) ScopeFunc {
//		return func(q *Query) *Query {
//			return q.Where("name = ?", name)
//		}
//	}
//
//	func WithDeleted(q *pop.Query) *pop.Query {
//		return q.Where("deleted_at is null")
//	}
//
//	c.Scope(ByName("mark)).Scope(WithDeleted).First(&User{})
func (q *Query) Scope(sf ScopeFunc) *Query {
	return sf(q)
}

// Scope the query by using a `ScopeFunc`
//
//	func ByName(name string) ScopeFunc {
//		return func(q *Query) *Query {
//			return q.Where("name = ?", name)
//		}
//	}
//
//	func WithDeleted(q *pop.Query) *pop.Query {
//		return q.Where("deleted_at is null")
//	}
//
//	c.Scope(ByName("mark)).Scope(WithDeleted).First(&User{})
func (c *Connection) Scope(sf ScopeFunc) *Query {
	return Q(c).Scope(sf)
}
