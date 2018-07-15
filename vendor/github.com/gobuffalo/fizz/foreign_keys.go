package fizz

import (
	"fmt"
	"strings"

	"github.com/pkg/errors"
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

func (f fizzer) AddForeignKey(table string, column string, refs interface{}, options Options) error {
	fkr, err := parseForeignKeyRef(refs)
	if err != nil {
		return errors.WithStack(err)
	}
	fk := ForeignKey{
		Column:     column,
		References: fkr,
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
	return nil
}

func (f fizzer) DropForeignKey(table string, fk string, options Options) {
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

func parseForeignKeyRef(refs interface{}) (ForeignKeyRef, error) {
	fkr := ForeignKeyRef{}
	refMap, ok := refs.(map[string]interface{})
	if !ok {
		return fkr, errors.Errorf(`invalid references format %s\nmust be "{"table": ["colum1", "column2"]}"`, refs)
	}
	if len(refMap) != 1 {
		return fkr, errors.Errorf("only one table is supported as Foreign key reference")
	}
	for table, columns := range refMap {
		fkr.Table = table
		for _, c := range columns.([]interface{}) {
			fkr.Columns = append(fkr.Columns, fmt.Sprintf("%s", c))
		}
	}

	return fkr, nil
}
