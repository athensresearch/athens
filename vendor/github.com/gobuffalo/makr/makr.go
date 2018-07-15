package makr

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"github.com/pkg/errors"
)

// Data to be passed into generators
type Data map[string]interface{}

// ShouldFunc decides whether a generator should be run or not
type ShouldFunc func(Data) bool

// Runnable interface must be implemented to be considered a runnable generator
type Runnable interface {
	Run(string, Data) error
}

// Generator is the top level construct that holds all of the Runnables
type Generator struct {
	Runners []Runnable
	Should  ShouldFunc
	Data    Data
}

// New Generator
func New() *Generator {
	return &Generator{
		Runners: []Runnable{},
		Data:    Data{},
	}
}

// Add a Runnable generator to the list
func (g *Generator) Add(r Runnable) {
	g.Runners = append(g.Runners, r)
}

// Run all of the generators
func (g *Generator) Run(rootPath string, data Data) error {
	// defer g.fmt(rootPath)
	dd := Data{}
	for k, v := range data {
		dd[k] = v
	}
	for k, v := range g.Data {
		dd[k] = v
	}
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()
	return chdir(rootPath, func() error {
		if g.Should != nil {
			b := g.Should(dd)
			if !b {
				return nil
			}
		}
		err := os.MkdirAll(rootPath, 0755)
		if err != nil {
			return errors.WithStack(err)
		}
		err = os.Chdir(rootPath)
		if err != nil {
			return errors.WithStack(err)
		}
		for _, r := range g.Runners {
			select {
			case <-ctx.Done():
				break
			default:
				err := r.Run(rootPath, dd)
				if err != nil {
					return errors.WithStack(err)
				}
			}
		}
		return nil
	})
}

func (g *Generator) Fmt(rootPath string) {
	pwd, _ := os.Getwd()
	files := []string{}
	filepath.Walk(rootPath, func(path string, info os.FileInfo, err error) error {
		path = strings.TrimPrefix(path, pwd+"/")
		if strings.Contains(path, ".git") || strings.Contains(path, "node_modules") || strings.Contains(path, "vendor"+string(os.PathSeparator)) {
			if info.IsDir() {
				return filepath.SkipDir
			}
			return nil
		}
		if filepath.Ext(path) == ".go" {
			files = append(files, path)
		}
		return nil
	})
	c := GoFmt(files...)
	fmt.Printf("--> %s\n", strings.Join(c.Args, " "))
	c.Stdout = os.Stdout
	c.Stderr = os.Stderr
	err := c.Run()
	if err != nil {
		fmt.Printf("Error running %s: %s", strings.Join(c.Args, " "), err.Error())
	}
}

func chdir(path string, fn func() error) error {
	pwd, _ := os.Getwd()
	defer os.Chdir(pwd)
	os.Chdir(path)
	return fn()
}

var nullShould = func(data Data) bool {
	return true
}
