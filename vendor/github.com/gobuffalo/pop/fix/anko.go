package fix

import (
	"bytes"
	"strings"
)

// Anko converts old anko-form migrations to new plush ones.
func Anko(content string) (string, error) {
	bb := &bytes.Buffer{}

	lines := strings.Split(content, "\n")

	// fix create_table
	for i, line := range lines {
		tl := strings.TrimSpace(line)
		if strings.HasPrefix(tl, "create_table") {
			line = strings.Replace(line, ", func(t) {", ") {", -1)
		}
		if strings.HasPrefix(tl, "})") {
			line = "}"
		}
		lines[i] = line
	}

	// fix (` && `)
	for i, line := range lines {
		lines[i] = strings.Replace(line, "(`", `("`, -1)
		lines[i] = strings.Replace(lines[i], "`)", `")`, -1)
	}

	// fix raw
	for i, line := range lines {
		tl := strings.TrimSpace(line)
		if strings.HasPrefix(tl, "raw(") {
			line = strings.Replace(line, "raw(", "sql(", -1)
		}
		lines[i] = line
	}

	bb.WriteString(strings.Join(lines, "\n"))

	return bb.String(), nil
}
