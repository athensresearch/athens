package pop

import (
	"bytes"
	"io"
	"io/ioutil"
	"os"
	"path/filepath"
	"text/template"

	"github.com/gobuffalo/envy"
	"github.com/pkg/errors"

	"gopkg.in/yaml.v2"
)

var lookupPaths = []string{"", "./config", "/config", "../", "../config", "../..", "../../config"}

// ConfigName is the name of the YAML databases config file
var ConfigName = "database.yml"

func init() {
	ap := os.Getenv("APP_PATH")
	if ap != "" {
		AddLookupPaths(ap)
	}
	ap = os.Getenv("POP_PATH")
	if ap != "" {
		AddLookupPaths(ap)
	}
	LoadConfigFile()
}

// LoadConfigFile loads a POP config file from the configured lookup paths
func LoadConfigFile() error {
	path, err := findConfigPath()
	if err != nil {
		return errors.WithStack(err)
	}
	Connections = map[string]*Connection{}
	Log("Loading config file from %s\n", path)
	f, err := os.Open(path)
	if err != nil {
		return errors.WithStack(err)
	}
	return LoadFrom(f)
}

// LookupPaths returns the current configuration lookup paths
func LookupPaths() []string {
	return lookupPaths
}

// AddLookupPaths add paths to the current lookup paths list
func AddLookupPaths(paths ...string) error {
	lookupPaths = append(paths, lookupPaths...)
	return LoadConfigFile()
}

func findConfigPath() (string, error) {
	for _, p := range LookupPaths() {
		path, _ := filepath.Abs(filepath.Join(p, ConfigName))
		if _, err := os.Stat(path); err == nil {
			return path, err
		}
	}
	return "", errors.New("tried to load pop configuration file, but couldn't find it")
}

// LoadFrom reads a configuration from the reader and sets up the connections
func LoadFrom(r io.Reader) error {
	envy.Load()
	tmpl := template.New("test")
	tmpl.Funcs(map[string]interface{}{
		"envOr": func(s1, s2 string) string {
			return envy.Get(s1, s2)
		},
		"env": func(s1 string) string {
			return envy.Get(s1, "")
		},
	})
	b, err := ioutil.ReadAll(r)
	if err != nil {
		return errors.WithStack(err)
	}
	t, err := tmpl.Parse(string(b))
	if err != nil {
		return errors.Wrap(err, "couldn't parse config template")
	}

	var bb bytes.Buffer
	err = t.Execute(&bb, nil)
	if err != nil {
		return errors.Wrap(err, "couldn't execute config template")
	}

	deets := map[string]*ConnectionDetails{}
	err = yaml.Unmarshal(bb.Bytes(), &deets)
	if err != nil {
		return errors.Wrap(err, "couldn't unmarshal config to yaml")
	}
	for n, d := range deets {
		con, err := NewConnection(d)
		if err != nil {
			return err
		}
		Connections[n] = con
	}
	return nil
}
