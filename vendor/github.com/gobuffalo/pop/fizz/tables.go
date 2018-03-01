package fizz

import (
	"fmt"
	"strings"
)

type Table struct {
	Name        string `db:"name"`
	Columns     []Column
	Indexes     []Index
	ForeignKeys []ForeignKey
	Options     map[string]interface{}
}

func (t *Table) DisableTimestamps() {
	t.Options["timestamps"] = false
}

func (t *Table) Column(name string, colType string, options map[string]interface{}) {
	var primary bool
	if _, ok := options["primary"]; ok {
		primary = true
	}
	c := Column{
		Name:    name,
		ColType: colType,
		Options: options,
		Primary: primary,
	}
	t.Columns = append(t.Columns, c)
}

func (t *Table) ForeignKey(column string, refs interface{}, options Options) {
	fk := ForeignKey{
		Column:     column,
		References: parseForeignKeyRef(refs),
		Options:    options,
	}

	if options["name"] != nil {
		fk.Name = options["name"].(string)
	} else {
		fk.Name = fmt.Sprintf("%s_%s_%s_fk", t.Name, fk.References.Table, strings.Join(fk.References.Columns, "_"))
	}

	t.ForeignKeys = append(t.ForeignKeys, fk)
}

func (t *Table) Timestamp(name string) {
	c := Column{
		Name:    name,
		ColType: "timestamp",
		Options: Options{},
	}

	t.Columns = append(t.Columns, c)
}

func (t *Table) Timestamps() {
	t.Columns = append(t.Columns, []Column{CREATED_COL, UPDATED_COL}...)
}

func (t *Table) ColumnNames() []string {
	cols := make([]string, len(t.Columns))
	for i, c := range t.Columns {
		cols[i] = c.Name
	}
	return cols
}

func (t *Table) HasColumns(args ...string) bool {
	keys := map[string]struct{}{}
	for _, k := range t.ColumnNames() {
		keys[k] = struct{}{}
	}
	for _, a := range args {
		if _, ok := keys[a]; !ok {
			return false
		}
	}
	return true
}

func (f fizzer) CreateTable() interface{} {
	return func(name string, fn func(t *Table)) {
		t := Table{
			Name:    name,
			Columns: []Column{},
		}

		fn(&t)
		var foundPrimary bool
		for _, c := range t.Columns {
			if c.Primary {
				foundPrimary = true
				break
			}
		}

		if !foundPrimary {
			t.Columns = append([]Column{INT_ID_COL}, t.Columns...)
		}

		if enabled, exists := t.Options["timestamps"]; !exists || enabled == true {
			t.Timestamps()
		}

		f.add(f.Bubbler.CreateTable(t))
	}
}

func (f fizzer) DropTable() interface{} {
	return func(name string) {
		f.add(f.Bubbler.DropTable(Table{Name: name}))
	}
}

func (f fizzer) RenameTable() interface{} {
	return func(old, new string) {
		f.add(f.Bubbler.RenameTable([]Table{
			{Name: old},
			{Name: new},
		}))
	}
}
