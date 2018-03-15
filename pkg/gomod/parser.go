package parser

import (
	"bufio"
	"fmt"
	"io"
	"regexp"
)

const (
	moduleRegexp = `module "([\w\.@:%_\+-.~#?&]+/.+)"`
)

// GomodParser retrieves module name from parsable source
type GomodParser interface {
	ModuleName() (string, error)
}

// Parse parses file passed as a reader and returns module name out of it.
func Parse(reader io.Reader) (string, error) {
	scanner := bufio.NewScanner(reader)

	re := regexp.MustCompile(moduleRegexp)

	for scanner.Scan() {
		line := scanner.Text()
		if name, found := checkVersion(line, re); found {
			return name, nil
		}
	}

	if err := scanner.Err(); err != nil {
		return "", err
	}

	return "", fmt.Errorf("name not found")
}

func checkVersion(line string, expression *regexp.Regexp) (string, bool) {
	matches := expression.FindAllStringSubmatch(line, 1)

	if len(matches) == 0 || len(matches[0]) < 2 {
		return "", false
	}

	return matches[0][1], true
}
