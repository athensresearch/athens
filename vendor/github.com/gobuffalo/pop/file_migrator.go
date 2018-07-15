package pop

import (
	"bytes"
	"fmt"
	"io"
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"
	"text/template"

	"github.com/gobuffalo/fizz"
	"github.com/gobuffalo/pop/fix"
	"github.com/pkg/errors"
)

// FileMigrator is a migrator for SQL and Fizz
// files on disk at a specified path.
type FileMigrator struct {
	Migrator
	Path string
}

// NewFileMigrator for a path and a Connection
func NewFileMigrator(path string, c *Connection) (FileMigrator, error) {
	fm := FileMigrator{
		Migrator: NewMigrator(c),
		Path:     path,
	}
	fm.SchemaPath = path

	err := fm.findMigrations()
	if err != nil {
		return fm, errors.WithStack(err)
	}

	return fm, nil
}

func (fm *FileMigrator) findMigrations() error {
	dir := fm.Path
	if fi, err := os.Stat(dir); err != nil || !fi.IsDir() {
		// directory doesn't exist
		return nil
	}
	filepath.Walk(dir, func(p string, info os.FileInfo, err error) error {
		if !info.IsDir() {
			matches := mrx.FindAllStringSubmatch(info.Name(), -1)
			if len(matches) == 0 {
				return nil
			}
			m := matches[0]
			var dbType string
			if m[3] == "" {
				dbType = "all"
			} else {
				dbType = m[3][1:]
				if !DialectSupported(dbType) {
					return fmt.Errorf("unsupported dialect %s", dbType)
				}
			}
			mf := Migration{
				Path:      p,
				Version:   m[1],
				Name:      m[2],
				DBType:    dbType,
				Direction: m[4],
				Type:      m[5],
				Runner: func(mf Migration, tx *Connection) error {
					f, err := os.Open(p)
					if err != nil {
						return errors.WithStack(err)
					}
					content, err := migrationContent(mf, tx, f)
					if err != nil {
						return errors.Wrapf(err, "error processing %s", mf.Path)
					}

					if content == "" {
						return nil
					}

					err = tx.RawQuery(content).Exec()
					if err != nil {
						return errors.Wrapf(err, "error executing %s, sql: %s", mf.Path, content)
					}
					return nil
				},
			}
			fm.Migrations[mf.Direction] = append(fm.Migrations[mf.Direction], mf)
		}
		return nil
	})
	return nil
}

func migrationContent(mf Migration, c *Connection, r io.Reader) (string, error) {
	b, err := ioutil.ReadAll(r)
	if err != nil {
		return "", nil
	}

	content := string(b)

	if mf.Type == "fizz" {
		// test for && fix anko migrations
		fixed, err := fix.Anko(content)
		if err != nil {
			return "", errors.Wrapf(err, "could not fizz the migration %s", mf.Path)
		}
		if strings.TrimSpace(fixed) != strings.TrimSpace(content) {
			fmt.Printf("[WARN] %s uses an old fizz syntax. please use\n%s\n", mf.Path, fixed)
		}
		content = fixed
	}

	t := template.Must(template.New("sql").Parse(content))
	var bb bytes.Buffer
	err = t.Execute(&bb, c.Dialect.Details())
	if err != nil {
		return "", errors.Wrapf(err, "could not execute migration template %s", mf.Path)
	}
	content = bb.String()

	if mf.Type == "fizz" {
		content, err = fizz.AString(content, c.Dialect.FizzTranslator())
		if err != nil {
			return "", errors.Wrapf(err, "could not fizz the migration %s", mf.Path)
		}
	}
	return content, nil
}
