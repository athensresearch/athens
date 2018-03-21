package file

import (
	"os"

	"github.com/gomods/athens/pkg/gomod"
)

// NewFileParser creates shorthand for parsing module name out of file specified by filepath
func NewFileParser(filepath string) parser.GomodParser {
	return fileParser{filepath: filepath}
}

type fileParser struct {
	filepath string
}

func (p fileParser) ModuleName() (string, error) {
	file, err := os.Open(p.filepath)
	if err != nil {
		return "", err
	}
	defer file.Close()

	return parser.Parse(file)
}
