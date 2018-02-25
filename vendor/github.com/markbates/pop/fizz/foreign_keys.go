package fizz

import (
	"fmt"
	"strings"
)

type ForeignKeyRef struct {
	Table   string
	Columns []string
}

type ForeignKey struct {
	Name       string
	Column     string
	References ForeignKeyRef
	Options    Options
}

func (f fizzer) AddForeignKey() interface{} {
	return func(table string, column string, refs interface{}, options Options) {
		fk := ForeignKey{
			Column:     column,
			References: parseForeignKeyRef(refs),
			Options:    options,
		}

		if options["name"] != nil {
			fk.Name = options["name"].(string)
		} else {
			fk.Name = fmt.Sprintf("%s_%s_%s_fk", table, fk.References.Table, strings.Join(fk.References.Columns, "_"))
		}

		f.add(f.Bubbler.AddForeignKey(Table{
			Name:        table,
			ForeignKeys: []ForeignKey{fk},
		}))
	}
}

func (f fizzer) DropForeignKey() interface{} {
	return func(table string, fk string, options Options) {
		f.add(f.Bubbler.DropForeignKey(Table{
			Name: table,
			ForeignKeys: []ForeignKey{
				{
					Name:    fk,
					Options: options,
				},
			},
		}))
	}
}

func parseForeignKeyRef(refs interface{}) (fkr ForeignKeyRef) {
	refMap, ok := refs.(map[string]interface{})
	if !ok {
		fmt.Printf(`invalid references format %s\nmust be "{"table": ["colum1", "column2"]}"`, refs)
		return
	}
	if len(refMap) != 1 {
		fmt.Printf("only one table is supported as Foreign key reference")
		return
	}
	for table, columns := range refMap {
		fkr.Table = table
		for _, c := range columns.([]interface{}) {
			fkr.Columns = append(fkr.Columns, fmt.Sprintf("%s", c))
		}
	}

	return
}
