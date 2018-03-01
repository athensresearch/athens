// +build appengine

package pop

import "github.com/gobuffalo/pop/fizz"

var schemaMigrations = fizz.Table{
	Name: "schema_migration",
	Columns: []fizz.Column{
		{Name: "version", ColType: "string"},
	},
	Indexes: []fizz.Index{},
}
