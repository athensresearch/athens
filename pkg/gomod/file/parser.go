package file

import (
	"github.com/gomods/athens/pkg/gomod"
	"github.com/spf13/afero"
)

// NewFileParser creates shorthand for parsing module name out of file specified by filepath
func NewFileParser(fs afero.Fs, filepath string) parser.GomodParser {
	return fileParser{filepath: filepath, fs: fs}
}

type fileParser struct {
	filepath string
	fs       afero.Fs
}

func (p fileParser) ModuleName() (string, error) {
	file, err := p.fs.Open(p.filepath)
	if err != nil {
		return "", err
	}
	defer file.Close()

	return parser.Parse(file)
}
