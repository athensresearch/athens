package columns

import (
	"sort"
	"strings"
)

// ReadableColumns represents a list of columns Pop is allowed to read.
type ReadableColumns struct {
	Columns
}

// SelectString returns the SQL column list part of the SELECT
// query.
func (c ReadableColumns) SelectString() string {
	xs := []string{}
	for _, t := range c.Cols {
		xs = append(xs, t.SelectSQL)
	}
	sort.Strings(xs)
	return strings.Join(xs, ", ")
}
