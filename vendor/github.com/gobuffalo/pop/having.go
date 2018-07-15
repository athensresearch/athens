package pop

import (
	"strings"
)

// HavingClause defines a condition and its arguments for a HAVING clause
type HavingClause struct {
	Condition string
	Arguments []interface{}
}

type havingClauses []HavingClause

func (c HavingClause) String() string {
	return c.Condition
}

func (c havingClauses) String() string {
	if len(c) == 0 {
		return ""
	}

	cs := []string{}
	for _, cl := range c {
		cs = append(cs, cl.String())
	}
	return strings.Join(cs, " AND ")
}
