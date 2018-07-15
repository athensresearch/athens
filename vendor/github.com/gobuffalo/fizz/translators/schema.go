package translators

import (
	"fmt"
	"strings"

	"github.com/gobuffalo/fizz"
	"github.com/jmoiron/sqlx"
)

type SchemaQuery interface {
	ReplaceSchema(map[string]*fizz.Table)
	Build() error
	TableInfo(string) (*fizz.Table, error)
	ReplaceColumn(table string, oldColumn string, newColumn fizz.Column) error
	ColumnInfo(table string, column string) (*fizz.Column, error)
	IndexInfo(table string, idx string) (*fizz.Index, error)
	Delete(string)
	SetTable(*fizz.Table)
	DeleteColumn(string, string)
}

type Schema struct {
	schema  map[string]*fizz.Table
	Builder SchemaQuery
	Name    string
	URL     string
	db      *sqlx.DB
}

func CreateSchema(name string, url string, schema map[string]*fizz.Table) Schema {
	return Schema{
		Name:   name,
		URL:    url,
		schema: schema,
	}
}

func (s *Schema) ReplaceSchema(newSchema map[string]*fizz.Table) {
	s.schema = newSchema
}

func (s *Schema) Build() error {
	return fmt.Errorf("Build not implemented for this translator!")
}

func (s *Schema) TableInfo(table string) (*fizz.Table, error) {
	if ti, ok := s.schema[table]; ok {
		return ti, nil
	}

	if s.Builder != nil {
		err := s.Builder.Build()
		if err != nil {
			return nil, err
		}
	} else {
		err := s.Build()
		if err != nil {
			return nil, err
		}
	}
	if ti, ok := s.schema[table]; ok {
		return ti, nil
	}
	return nil, fmt.Errorf("Could not find table data for %s!", table)
}

func (s *Schema) ReplaceColumn(table string, oldColumn string, newColumn fizz.Column) error {
	tableInfo, err := s.TableInfo(table)
	if err != nil {
		return err
	}
	for i, col := range tableInfo.Columns {
		if strings.ToLower(strings.TrimSpace(col.Name)) == strings.ToLower(strings.TrimSpace(oldColumn)) {
			tableInfo.Columns[i] = newColumn
			return nil
		}
	}
	return fmt.Errorf("Could not find column(%s) in table(%s)!", oldColumn, table)
}

func (s *Schema) ColumnInfo(table string, column string) (*fizz.Column, error) {
	ti, err := s.TableInfo(table)
	if err != nil {
		return nil, err
	}

	if ci, ok := s.findColumnInfo(ti, column); ok {
		return ci, nil
	}
	return nil, fmt.Errorf("Could not find column data for %s in table %s!", column, table)
}

func (s *Schema) IndexInfo(table string, idx string) (*fizz.Index, error) {
	ti, err := s.TableInfo(table)
	if err != nil {
		return nil, err
	}

	if i, ok := s.findIndexInfo(ti, idx); ok {
		return i, nil
	}
	return nil, fmt.Errorf("Could not find index data for %s in table %s!", idx, table)
}

func (s *Schema) Delete(table string) {
	delete(s.schema, table)
}

func (s *Schema) SetTable(table *fizz.Table) {
	s.schema[table.Name] = table
}

func (s *Schema) DeleteColumn(table string, column string) {
	tableInfo, err := s.TableInfo(table)
	if err != nil {
		return
	}
	for i, col := range tableInfo.Columns {
		if strings.ToLower(strings.TrimSpace(col.Name)) == strings.ToLower(strings.TrimSpace(column)) {
			tableInfo.Columns = append(tableInfo.Columns[:i], tableInfo.Columns[i+1:]...)
			return
		}
	}
}

func (s *Schema) findColumnInfo(tableInfo *fizz.Table, column string) (*fizz.Column, bool) {
	for _, col := range tableInfo.Columns {
		if strings.ToLower(strings.TrimSpace(col.Name)) == strings.ToLower(strings.TrimSpace(column)) {
			return &col, true
		}
	}
	return nil, false
}

func (s *Schema) findIndexInfo(tableInfo *fizz.Table, index string) (*fizz.Index, bool) {
	for _, ind := range tableInfo.Indexes {
		if strings.ToLower(strings.TrimSpace(ind.Name)) == strings.ToLower(strings.TrimSpace(index)) {
			return &ind, true
		}
	}
	return nil, false
}
