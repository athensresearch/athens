// +build appengine

package pop

import "github.com/gobuffalo/fizz"

func newSchemaMigrations(name string) fizz.Table {
	return fizz.Table{
		Name: name,
		Columns: []fizz.Column{
			{Name: "version", ColType: "string"},
		},
		Indexes: []fizz.Index{},
	}
}
