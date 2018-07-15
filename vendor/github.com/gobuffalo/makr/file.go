package makr

import (
	"bytes"
	"fmt"
	"os"
	"path/filepath"
	"text/template"

	"github.com/pkg/errors"
)

// File to generate
type File struct {
	Path          string
	Template      string
	TemplateFuncs template.FuncMap
	Permission    os.FileMode
	Should        ShouldFunc
}

// Run the generator
func (f File) Run(rootPath string, data Data) error {
	return chdir(rootPath, func() error {
		if f.Should != nil && !f.Should(data) {
			return nil
		}

		path, err := f.render(f.Path, data)
		if err != nil {
			return err
		}

		body, err := f.render(f.Template, data)
		if err != nil {
			return err
		}

		return f.save(rootPath, path, body)
	})
}

func (f File) save(rootPath, path, body string) error {
	dir := filepath.Dir(path)
	err := os.MkdirAll(filepath.Join(rootPath, dir), 0755)
	if err != nil {
		return err
	}

	odir := filepath.Join(rootPath, path)
	fmt.Printf("--> %s\n", odir)

	ff, err := os.Create(odir)
	if err != nil {
		return err
	}

	_, err = ff.WriteString(body)
	if err != nil {
		return errors.WithStack(err)
	}
	return nil
}

func (f *File) render(s string, data Data) (string, error) {
	t := template.New(s)
	if f.TemplateFuncs != nil {
		t = t.Funcs(f.TemplateFuncs)
	}
	t, err := t.Parse(s)
	if err != nil {
		return "", err
	}
	bb := &bytes.Buffer{}
	err = t.Execute(bb, data)
	return bb.String(), err
}

// NewFile set up with sensible defaults
func NewFile(path string, t string) File {
	return File{
		Path:          path,
		Template:      t,
		TemplateFuncs: Helpers,
		Permission:    0664,
		Should: func(data Data) bool {
			return true
		},
	}
}
