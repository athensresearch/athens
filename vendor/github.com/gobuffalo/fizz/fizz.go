package fizz

import (
	"io"
	"io/ioutil"
	"os"
	"os/exec"
	"strings"

	"github.com/pkg/errors"
)

type Options map[string]interface{}

type fizzer struct {
	Bubbler *Bubbler
}

func (f fizzer) add(s string, err error) error {
	if err != nil {
		return errors.WithStack(err)
	}
	f.Bubbler.data = append(f.Bubbler.data, s)
	return nil
}

func (f fizzer) Exec(out io.Writer) func(string) error {
	return func(s string) error {
		args := strings.Split(s, " ")
		cmd := exec.Command(args[0], args[1:]...)
		cmd.Stdin = os.Stdin
		cmd.Stdout = out
		cmd.Stderr = os.Stderr
		err := cmd.Run()
		if err != nil {
			return errors.Wrapf(err, "error executing command: %s", s)
		}
		return nil
	}
}

func AFile(f *os.File, t Translator) (string, error) {
	b, err := ioutil.ReadAll(f)
	if err != nil {
		return "", errors.WithStack(err)
	}
	return AString(string(b), t)
}

func AString(s string, t Translator) (string, error) {
	b := NewBubbler(t)
	return b.Bubble(s)
}
