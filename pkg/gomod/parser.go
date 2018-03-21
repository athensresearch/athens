package parser

import (
	"bufio"
	"errors"
	"io"
	"regexp"
)

const (
	moduleRegexp = `module "([\w\.@:%_\+-.~#?&]+/.+)"`
)

var (
	re = regexp.MustCompile(moduleRegexp)
	// ErrNotFound describe an error when can't find module name
	ErrNotFound = errors.New("name not found")
)

// GomodParser retrieves module name from parsable source
type GomodParser interface {
	ModuleName() (string, error)
}

// Parse parses file passed as a reader and returns module name out of it.
func Parse(reader io.Reader) (string, error) {
	scanner := bufio.NewScanner(reader)

	for scanner.Scan() {
		line := scanner.Text()
		if name, found := checkVersion(line, re); found {
			return name, nil
		}
	}

	if err := scanner.Err(); err != nil {
		return "", err
	}

	return "", ErrNotFound
}

func checkVersion(line string, expression *regexp.Regexp) (string, bool) {
	matches := expression.FindAllStringSubmatch(line, 1)

	if len(matches) == 0 || len(matches[0]) < 2 {
		return "", false
	}

	return matches[0][1], true
}
