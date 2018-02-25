package pop

import (
	"fmt"
	"net/url"
	"regexp"
	"strconv"
	"strings"
	"time"

	_mysql "github.com/go-sql-driver/mysql"
	"github.com/markbates/going/defaults"
	"github.com/pkg/errors"
)

// ConnectionDetails stores the data needed to connect to a datasource
type ConnectionDetails struct {
	// Example: "postgres" or "sqlite3" or "mysql"
	Dialect string
	// The name of your database. Example: "foo_development"
	Database string
	// The host of your database. Example: "127.0.0.1"
	Host string
	// The port of your database. Example: 1234
	// Will default to the "default" port for each dialect.
	Port string
	// The username of the database user. Example: "root"
	User string
	// The password of the database user. Example: "password"
	Password string
	// Instead of specifying each individual piece of the
	// connection you can instead just specify the URL of the
	// database. Example: "postgres://postgres:postgres@localhost:5432/pop_test?sslmode=disable"
	URL string
	// Defaults to 0 "unlimited". See https://golang.org/pkg/database/sql/#DB.SetMaxOpenConns
	Pool    int
	Options map[string]string
}

var dialectX = regexp.MustCompile(`\s+:\/\/`)

// Finalize cleans up the connection details by normalizing names,
// filling in default values, etc...
func (cd *ConnectionDetails) Finalize() error {
	if cd.URL != "" {
		ul := cd.URL
		if cd.Dialect != "" {
			if !dialectX.MatchString(ul) {
				ul = cd.Dialect + "://" + ul
			}
		}
		cd.Database = cd.URL
		if !strings.HasPrefix(cd.Dialect, "sqlite") {
			u, err := url.Parse(ul)
			if err != nil {
				return errors.Wrapf(err, "couldn't parse %s", ul)
			}
			cd.Dialect = u.Scheme
			cd.Database = u.Path

			hp := strings.Split(u.Host, ":")
			cd.Host = hp[0]
			if len(hp) > 1 {
				cd.Port = hp[1]
			}

			if u.User != nil {
				cd.User = u.User.Username()
				cd.Password, _ = u.User.Password()
			}
		}

	}
	switch strings.ToLower(cd.Dialect) {
	case "postgres", "postgresql", "pg":
		cd.Dialect = "postgres"
		cd.Port = defaults.String(cd.Port, "5432")
		cd.Database = strings.TrimPrefix(cd.Database, "/")
	case "cockroach", "cockroachdb", "crdb":
		cd.Dialect = "cockroach"
		cd.Port = defaults.String(cd.Port, "26257")
		cd.Database = strings.TrimPrefix(cd.Database, "/")
	case "mysql":
		// parse and verify whether URL is supported by underlying driver or not.
		if cd.URL != "" {
			cfg, err := _mysql.ParseDSN(strings.TrimPrefix(cd.URL, "mysql://"))
			if err != nil {
				return errors.Errorf("The URL is not supported by MySQL driver.")
			}
			cd.User = cfg.User
			cd.Password = cfg.Passwd
			cd.Database = cfg.DBName
			addr := strings.TrimSuffix(strings.TrimPrefix(cfg.Addr, "("), ")")
			if cfg.Net == "unix" {
				cd.Port = "socket"
				cd.Host = addr
			} else {
				tmp := strings.Split(addr, ":")
				switch len(tmp) {
				case 1:
					cd.Host = tmp[0]
					cd.Port = "3306"
				case 2:
					cd.Host = tmp[0]
					cd.Port = tmp[1]
				}
			}
		} else {
			cd.Port = defaults.String(cd.Port, "3306")
			cd.Database = strings.TrimPrefix(cd.Database, "/")
		}
	case "sqlite", "sqlite3":
		cd.Dialect = "sqlite3"
	default:
		return errors.Errorf("Unknown dialect %s!", cd.Dialect)
	}
	return nil
}

// Parse is deprecated! Please use `ConnectionDetails.Finalize()` instead!
func (cd *ConnectionDetails) Parse(port string) error {
	fmt.Println("[POP] ConnectionDetails#Parse(port string) has been deprecated!")
	return cd.Finalize()
}

// RetrySleep returns the amount of time to wait between two connection retries
func (cd *ConnectionDetails) RetrySleep() time.Duration {
	d, err := time.ParseDuration(defaults.String(cd.Options["retry_sleep"], "1ms"))
	if err != nil {
		return 1 * time.Millisecond
	}
	return d
}

// RetryLimit returns the maximum number of accepted connection retries
func (cd *ConnectionDetails) RetryLimit() int {
	i, err := strconv.Atoi(defaults.String(cd.Options["retry_limit"], "1000"))
	if err != nil {
		return 100
	}
	return i
}
