package translators

import (
	"fmt"
	"regexp"
	"strings"

	"github.com/pkg/errors"

	"github.com/gobuffalo/pop/fizz"
)

type MySQL struct {
	Schema SchemaQuery
}

func NewMySQL(url, name string) *MySQL {
	schema := &mysqlSchema{Schema{URL: url, Name: name, schema: map[string]*fizz.Table{}}}
	schema.Builder = schema
	return &MySQL{
		Schema: schema,
	}
}

func (p *MySQL) CreateTable(t fizz.Table) (string, error) {
	sql := []string{}
	cols := []string{}
	for _, c := range t.Columns {
		cols = append(cols, p.buildColumn(c))
		if c.Primary {
			cols = append(cols, fmt.Sprintf("PRIMARY KEY(%s)", c.Name))
		}
	}

	for _, fk := range t.ForeignKeys {
		cols = append(cols, p.buildForeignKey(t, fk, true))
	}

	s := fmt.Sprintf("CREATE TABLE %s (\n%s\n) ENGINE=InnoDB;", t.Name, strings.Join(cols, ",\n"))

	sql = append(sql, s)

	for _, i := range t.Indexes {
		s, err := p.AddIndex(fizz.Table{
			Name:    t.Name,
			Indexes: []fizz.Index{i},
		})
		if err != nil {
			return "", err
		}
		sql = append(sql, s)
	}

	return strings.Join(sql, "\n"), nil
}

func (p *MySQL) DropTable(t fizz.Table) (string, error) {
	return fmt.Sprintf("DROP TABLE %s;", t.Name), nil
}

func (p *MySQL) RenameTable(t []fizz.Table) (string, error) {
	if len(t) < 2 {
		return "", errors.New("Not enough table names supplied!")
	}
	return fmt.Sprintf("ALTER TABLE %s RENAME TO %s;", t[0].Name, t[1].Name), nil
}

func (p *MySQL) ChangeColumn(t fizz.Table) (string, error) {
	if len(t.Columns) == 0 {
		return "", errors.New("Not enough columns supplied!")
	}
	c := t.Columns[0]
	s := fmt.Sprintf("ALTER TABLE %s MODIFY %s;", t.Name, p.buildColumn(c))
	return s, nil
}

func (p *MySQL) AddColumn(t fizz.Table) (string, error) {
	if len(t.Columns) == 0 {
		return "", errors.New("Not enough columns supplied!")
	}

	if _, ok := t.Columns[0].Options["first"]; ok {
		c := t.Columns[0]
		s := fmt.Sprintf("ALTER TABLE %s ADD COLUMN %s FIRST;", t.Name, p.buildColumn(c))
		return s, nil
	}

	if val, ok := t.Columns[0].Options["after"]; ok {
		c := t.Columns[0]
		s := fmt.Sprintf("ALTER TABLE %s ADD COLUMN %s AFTER %s;", t.Name, p.buildColumn(c), val)
		return s, nil
	}

	c := t.Columns[0]
	s := fmt.Sprintf("ALTER TABLE %s ADD COLUMN %s;", t.Name, p.buildColumn(c))
	return s, nil
}

func (p *MySQL) DropColumn(t fizz.Table) (string, error) {
	if len(t.Columns) == 0 {
		return "", errors.New("Not enough columns supplied!")
	}
	c := t.Columns[0]
	return fmt.Sprintf("ALTER TABLE %s DROP COLUMN %s;", t.Name, c.Name), nil
}

func (p *MySQL) RenameColumn(t fizz.Table) (string, error) {
	if len(t.Columns) < 2 {
		return "", errors.New("Not enough columns supplied!")
	}
	oc := t.Columns[0]
	nc := t.Columns[1]

	ti, err := p.Schema.TableInfo(t.Name)
	if err != nil {
		return "", err
	}
	var c fizz.Column
	for _, c = range ti.Columns {
		if c.Name == oc.Name {
			break
		}
	}
	col := p.buildColumn(c)
	col = strings.Replace(col, oc.Name, fmt.Sprintf("%s %s", oc.Name, nc.Name), -1)
	s := fmt.Sprintf("ALTER TABLE %s CHANGE %s;", t.Name, col)
	return s, nil
}

func (p *MySQL) AddIndex(t fizz.Table) (string, error) {
	if len(t.Indexes) == 0 {
		return "", errors.New("Not enough indexes supplied!")
	}
	i := t.Indexes[0]
	s := fmt.Sprintf("CREATE INDEX %s ON %s (%s);", i.Name, t.Name, strings.Join(i.Columns, ", "))
	if i.Unique {
		s = strings.Replace(s, "CREATE", "CREATE UNIQUE", 1)
	}
	return s, nil
}

func (p *MySQL) DropIndex(t fizz.Table) (string, error) {
	if len(t.Indexes) == 0 {
		return "", errors.New("Not enough indexes supplied!")
	}
	i := t.Indexes[0]
	return fmt.Sprintf("DROP INDEX %s ON %s;", i.Name, t.Name), nil
}

func (p *MySQL) RenameIndex(t fizz.Table) (string, error) {
	schema := p.Schema.(*mysqlSchema)
	version, err := schema.Version()
	if err != nil {
		return "", errors.WithStack(err)
	}
	if !strings.HasPrefix(version, "5.7") {
		return "", errors.New("renaming indexes on MySQL versions less than 5.7 is not supported by fizz; use raw SQL instead")
	}
	ix := t.Indexes
	if len(ix) < 2 {
		return "", errors.New("Not enough indexes supplied!")
	}
	oi := ix[0]
	ni := ix[1]
	return fmt.Sprintf("ALTER TABLE %s RENAME INDEX %s TO %s;", t.Name, oi.Name, ni.Name), nil
}

func (p *MySQL) AddForeignKey(t fizz.Table) (string, error) {
	if len(t.ForeignKeys) == 0 {
		return "", errors.New("Not enough foreign keys supplied!")
	}

	return p.buildForeignKey(t, t.ForeignKeys[0], false), nil
}

func (p *MySQL) DropForeignKey(t fizz.Table) (string, error) {
	if len(t.ForeignKeys) == 0 {
		return "", errors.New("Not enough foreign keys supplied!")
	}

	fk := t.ForeignKeys[0]

	var ifExists string
	if v, ok := fk.Options["if_exists"]; ok && v.(bool) {
		ifExists = "IF EXISTS"
	}

	s := fmt.Sprintf("ALTER TABLE %s DROP FOREIGN KEY %s %s;", t.Name, ifExists, fk.Name)
	return s, nil
}

func (p *MySQL) buildColumn(c fizz.Column) string {
	s := fmt.Sprintf("%s %s", c.Name, p.colType(c))
	if c.Options["null"] == nil || c.Primary {
		s = fmt.Sprintf("%s NOT NULL", s)
	}
	if c.Options["default"] != nil {
		d := fmt.Sprintf("%#v", c.Options["default"])
		re := regexp.MustCompile("^(\")(.+)(\")$")
		d = re.ReplaceAllString(d, "'$2'")
		s = fmt.Sprintf("%s DEFAULT %s", s, d)
	}
	if c.Options["default_raw"] != nil {
		d := fmt.Sprintf("%s", c.Options["default_raw"])
		s = fmt.Sprintf("%s DEFAULT %s", s, d)
	}

	if c.Primary && (c.ColType == "integer" || strings.ToLower(c.ColType) == "int") {
		s = fmt.Sprintf("%s AUTO_INCREMENT", s)
	}
	return s
}

func (p *MySQL) colType(c fizz.Column) string {
	switch strings.ToLower(c.ColType) {
	case "string":
		s := "255"
		if c.Options["size"] != nil {
			s = fmt.Sprintf("%d", c.Options["size"])
		}
		return fmt.Sprintf("VARCHAR (%s)", s)
	case "uuid":
		return "char(36)"
	case "timestamp", "time", "datetime":
		return "DATETIME"
	case "blob":
		return "BLOB"
	default:
		return c.ColType
	}
}

func (p *MySQL) buildForeignKey(t fizz.Table, fk fizz.ForeignKey, onCreate bool) string {
	refs := fmt.Sprintf("%s (%s)", fk.References.Table, strings.Join(fk.References.Columns, ", "))
	s := fmt.Sprintf("FOREIGN KEY (%s) REFERENCES %s", fk.Column, refs)

	if onUpdate, ok := fk.Options["on_update"]; ok {
		s += fmt.Sprintf(" ON UPDATE %s", onUpdate)
	}

	if onDelete, ok := fk.Options["on_delete"]; ok {
		s += fmt.Sprintf(" ON DELETE %s", onDelete)
	}

	if !onCreate {
		s = fmt.Sprintf("ALTER TABLE %s ADD CONSTRAINT %s %s;", t.Name, fk.Name, s)
	}

	return s
}
