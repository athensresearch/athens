package pop

import "fmt"

// Join will append a JOIN clause to the query
func (q *Query) Join(table string, on string, args ...interface{}) *Query {
	if q.RawSQL.Fragment != "" {
		fmt.Println("Warning: Query is setup to use raw SQL")
		return q
	}
	q.joinClauses = append(q.joinClauses, joinClause{"JOIN", table, on, args})
	return q
}

// LeftJoin will append a LEFT JOIN clause to the query
func (q *Query) LeftJoin(table string, on string, args ...interface{}) *Query {
	if q.RawSQL.Fragment != "" {
		fmt.Println("Warning: Query is setup to use raw SQL")
		return q
	}
	q.joinClauses = append(q.joinClauses, joinClause{"LEFT JOIN", table, on, args})
	return q
}

// RightJoin will append a RIGHT JOIN clause to the query
func (q *Query) RightJoin(table string, on string, args ...interface{}) *Query {
	if q.RawSQL.Fragment != "" {
		fmt.Println("Warning: Query is setup to use raw SQL")
		return q
	}
	q.joinClauses = append(q.joinClauses, joinClause{"RIGHT JOIN", table, on, args})
	return q
}

// LeftOuterJoin will append a LEFT OUTER JOIN clause to the query
func (q *Query) LeftOuterJoin(table string, on string, args ...interface{}) *Query {
	if q.RawSQL.Fragment != "" {
		fmt.Println("Warning: Query is setup to use raw SQL")
		return q
	}
	q.joinClauses = append(q.joinClauses, joinClause{"LEFT OUTER JOIN", table, on, args})
	return q
}

// RightOuterJoin will append a RIGHT OUTER JOIN clause to the query
func (q *Query) RightOuterJoin(table string, on string, args ...interface{}) *Query {
	if q.RawSQL.Fragment != "" {
		fmt.Println("Warning: Query is setup to use raw SQL")
		return q
	}
	q.joinClauses = append(q.joinClauses, joinClause{"RIGHT OUTER JOIN", table, on, args})
	return q
}

// LeftInnerJoin will append a LEFT INNER JOIN clause to the query
func (q *Query) LeftInnerJoin(table string, on string, args ...interface{}) *Query {
	if q.RawSQL.Fragment != "" {
		fmt.Println("Warning: Query is setup to use raw SQL")
		return q
	}
	q.joinClauses = append(q.joinClauses, joinClause{"LEFT INNER JOIN", table, on, args})
	return q
}

// RightInnerJoin will append a RIGHT INNER JOIN clause to the query
func (q *Query) RightInnerJoin(table string, on string, args ...interface{}) *Query {
	if q.RawSQL.Fragment != "" {
		fmt.Println("Warning: Query is setup to use raw SQL")
		return q
	}
	q.joinClauses = append(q.joinClauses, joinClause{"RIGHT INNER JOIN", table, on, args})
	return q
}
