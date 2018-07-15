package columns

import "fmt"

// Column represents a SQL table column.
type Column struct {
	Name      string
	Writeable bool
	Readable  bool
	SelectSQL string
}

// UpdateString returns the SQL statement to UPDATE the column.
func (c Column) UpdateString() string {
	return fmt.Sprintf("%s = :%s", c.Name, c.Name)
}

// SetSelectSQL sets a custom SELECT statement for the column.
func (c *Column) SetSelectSQL(s string) {
	c.SelectSQL = s
	c.Writeable = false
	c.Readable = true
}
