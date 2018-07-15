package fizz

import (
	"fmt"
	"strings"

	"github.com/pkg/errors"
)

type Index struct {
	Name    string
	Columns []string
	Unique  bool
	Options Options
}

func (f fizzer) AddIndex(table string, columns interface{}, options Options) error {
	i := Index{}
	switch t := columns.(type) {
	default:
		return errors.Errorf("unexpected type for columns %T", t) // %T prints whatever type t has
	case string:
		i.Columns = []string{t}
	case []interface{}:
		cl := make([]string, len(t))
		for i, c := range t {
			cl[i] = c.(string)
		}
		i.Columns = cl
	}

	if options["name"] != nil {
		i.Name = options["name"].(string)
	} else {
		i.Name = fmt.Sprintf("%s_%s_idx", table, strings.Join(i.Columns, "_"))
	}
	i.Unique = options["unique"] != nil
	f.add(f.Bubbler.AddIndex(Table{
		Name:    table,
		Indexes: []Index{i},
	}))
	return nil
}

func (f fizzer) DropIndex(table, name string) {
	f.add(f.Bubbler.DropIndex(Table{
		Name: table,
		Indexes: []Index{
			{Name: name},
		},
	}))
}

func (f fizzer) RenameIndex(table, old, new string) {
	f.add(f.Bubbler.RenameIndex(Table{
		Name: table,
		Indexes: []Index{
			{Name: old},
			{Name: new},
		},
	}))
}
