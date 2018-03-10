package bytes

import (
	"bytes"

	"github.com/gomods/athens/pkg/gomod"
)

// NewContentParser creates shorthand for parsing module name out of file content
func NewContentParser(content []byte) parser.GomodParser {
	return contentParser{content: content}
}

type contentParser struct {
	content []byte
}

func (p contentParser) ModuleName() (string, error) {
	readCloser := bytes.NewReader(p.content)
	return parser.Parse(readCloser)
}
