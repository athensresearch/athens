package pop

import (
	"bytes"
	"fmt"
	"io"
	"os"
	"os/exec"
	"strings"

	// Load MySQL Go driver
	_ "github.com/go-sql-driver/mysql"
	"github.com/gobuffalo/fizz"
	"github.com/gobuffalo/fizz/translators"
	"github.com/gobuffalo/pop/columns"
	"github.com/jmoiron/sqlx"
	"github.com/markbates/going/defaults"
	"github.com/pkg/errors"
)

var _ dialect = &mysql{}

type mysql struct {
	ConnectionDetails *ConnectionDetails
}

func (m *mysql) Name() string {
	return "mysql"
}

func (m *mysql) Details() *ConnectionDetails {
	return m.ConnectionDetails
}

func (m *mysql) URL() string {
	deets := m.ConnectionDetails
	if deets.URL != "" {
		return strings.TrimPrefix(deets.URL, "mysql://")
	}
	encoding := defaults.String(deets.Encoding, "utf8_general_ci")
	if deets.Encoding == "" {
		Log(`Warning: The default encoding will change to "utf8mb4_general_ci" in the next version. Set the "encoding" param in your connection setup to "utf8_general_ci" if you still want to use this encoding with MySQL.`)
	}
	s := "%s:%s@(%s:%s)/%s?parseTime=true&multiStatements=true&readTimeout=1s&collation=%s"
	return fmt.Sprintf(s, deets.User, deets.Password, deets.Host, deets.Port, deets.Database, encoding)
}

func (m *mysql) urlWithoutDb() string {
	deets := m.ConnectionDetails
	if deets.URL != "" {
		// respect user's own URL definition (with options).
		url := strings.TrimPrefix(deets.URL, "mysql://")
		return strings.Replace(url, "/"+deets.Database+"?", "/?", 1)
	}
	encoding := defaults.String(deets.Encoding, "utf8_general_ci")
	if deets.Encoding == "" {
		Log(`Warning: The default encoding will change to "utf8mb4_general_ci" in the next version. Set the "encoding" param in your connection setup to "utf8_general_ci" if you still want to use this encoding with MySQL.`)
	}
	s := "%s:%s@(%s:%s)/?parseTime=true&multiStatements=true&readTimeout=1s&collation=%s"
	return fmt.Sprintf(s, deets.User, deets.Password, deets.Host, deets.Port, encoding)
}

func (m *mysql) MigrationURL() string {
	return m.URL()
}

func (m *mysql) Create(s store, model *Model, cols columns.Columns) error {
	return errors.Wrap(genericCreate(s, model, cols), "mysql create")
}

func (m *mysql) Update(s store, model *Model, cols columns.Columns) error {
	return errors.Wrap(genericUpdate(s, model, cols), "mysql update")
}

func (m *mysql) Destroy(s store, model *Model) error {
	return errors.Wrap(genericDestroy(s, model), "mysql destroy")
}

func (m *mysql) SelectOne(s store, model *Model, query Query) error {
	return errors.Wrap(genericSelectOne(s, model, query), "mysql select one")
}

func (m *mysql) SelectMany(s store, models *Model, query Query) error {
	return errors.Wrap(genericSelectMany(s, models, query), "mysql select many")
}

// CreateDB creates a new database, from the given connection credentials
func (m *mysql) CreateDB() error {
	deets := m.ConnectionDetails
	db, err := sqlx.Open(deets.Dialect, m.urlWithoutDb())
	if err != nil {
		return errors.Wrapf(err, "error creating MySQL database %s", deets.Database)
	}
	defer db.Close()
	encoding := defaults.String(deets.Encoding, "utf8_general_ci")
	if deets.Encoding == "" {
		Log(`Warning: The default encoding will change to "utf8mb4_general_ci" in the next version. Set the "encoding" param in your connection setup to "utf8_general_ci" if you still want to use this encoding with MySQL.`)
	}
	query := fmt.Sprintf("CREATE DATABASE `%s` DEFAULT COLLATE `%s`", deets.Database, encoding)
	Log(query)

	_, err = db.Exec(query)
	if err != nil {
		return errors.Wrapf(err, "error creating MySQL database %s", deets.Database)
	}

	fmt.Printf("created database %s\n", deets.Database)
	return nil
}

// DropDB drops an existing database, from the given connection credentials
func (m *mysql) DropDB() error {
	deets := m.ConnectionDetails
	db, err := sqlx.Open(deets.Dialect, m.urlWithoutDb())
	if err != nil {
		return errors.Wrapf(err, "error dropping MySQL database %s", deets.Database)
	}
	defer db.Close()
	query := fmt.Sprintf("DROP DATABASE `%s`", deets.Database)
	Log(query)

	_, err = db.Exec(query)
	if err != nil {
		return errors.Wrapf(err, "error dropping MySQL database %s", deets.Database)
	}

	fmt.Printf("dropped database %s\n", deets.Database)
	return nil
}

func (m *mysql) TranslateSQL(sql string) string {
	return sql
}

func (m *mysql) FizzTranslator() fizz.Translator {
	t := translators.NewMySQL(m.URL(), m.Details().Database)
	return t
}

func (m *mysql) Lock(fn func() error) error {
	return fn()
}

func (m *mysql) DumpSchema(w io.Writer) error {
	deets := m.Details()
	cmd := exec.Command("mysqldump", "-d", "-h", deets.Host, "-P", deets.Port, "-u", deets.User, fmt.Sprintf("--password=%s", deets.Password), deets.Database)
	if deets.Port == "socket" {
		cmd = exec.Command("mysqldump", "-d", "-S", deets.Host, "-u", deets.User, fmt.Sprintf("--password=%s", deets.Password), deets.Database)
	}
	Log(strings.Join(cmd.Args, " "))
	cmd.Stdout = w
	cmd.Stderr = os.Stderr

	err := cmd.Run()
	if err != nil {
		return err
	}

	fmt.Printf("dumped schema for %s\n", m.Details().Database)
	return nil
}

// LoadSchema executes a schema sql file against the configured database.
func (m *mysql) LoadSchema(r io.Reader) error {
	return genericLoadSchema(m.ConnectionDetails, m.MigrationURL(), r)
}

// TruncateAll truncates all tables for the given connection.
func (m *mysql) TruncateAll(tx *Connection) error {
	stmts := []string{}
	err := tx.RawQuery(mysqlTruncate, m.Details().Database).All(&stmts)
	if err != nil {
		return err
	}
	if len(stmts) == 0 {
		return nil
	}

	var qb bytes.Buffer
	// #49: Disable foreign keys before truncation
	qb.WriteString("SET SESSION FOREIGN_KEY_CHECKS = 0; ")
	qb.WriteString(strings.Join(stmts, " "))
	// #49: Re-enable foreign keys after truncation
	qb.WriteString(" SET SESSION FOREIGN_KEY_CHECKS = 1;")

	return tx.RawQuery(qb.String()).Exec()
}

func newMySQL(deets *ConnectionDetails) dialect {
	cd := &mysql{
		ConnectionDetails: deets,
	}

	return cd
}

const mysqlTruncate = "SELECT concat('TRUNCATE TABLE `', TABLE_NAME, '`;') as stmt FROM INFORMATION_SCHEMA.TABLES WHERE table_schema = ? AND table_type <> 'VIEW'"
