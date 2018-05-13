package module

import (
	"archive/zip"
	"bytes"
	"fmt"
	"os"
	"path/filepath"
	"strings"

	ignore "github.com/sabhiram/go-gitignore"
	"github.com/spf13/afero"
)

const (
	gitIgnoreFilename = ".gitignore"
)

type file struct {
	Name string
	Body string
}

// MakeZip takes dir and module info and generates vgo valid zip
// the dir must end with a "/"
func MakeZip(fs afero.Fs, dir, module, version string) ([]byte, error) {
	ignoreParser := getIgnoreParser(fs, dir)
	buf := new(bytes.Buffer)
	w := zip.NewWriter(buf)

	walkFunc := func(path string, info os.FileInfo, err error) error {
		if err != nil || info == nil || info.IsDir() {
			return err
		}

		fileName := getFileName(path, dir, module, version)

		if ignoreParser.MatchesPath(fileName) {
			return nil
		}

		fileContent, err := afero.ReadFile(fs, path)
		if err != nil {
			return err
		}

		f, err := w.Create(fileName)
		if err != nil {
			return err
		}

		_, err = f.Write(fileContent)
		return err
	}

	err := afero.Walk(fs, dir, walkFunc)
	w.Close()

	return buf.Bytes(), err
}

func getIgnoreParser(fs afero.Fs, dir string) ignore.IgnoreParser {
	gitFilePath := filepath.Join(dir, gitIgnoreFilename)
	gitParser, _ := compileIgnoreFileAndLines(fs, gitFilePath, gitIgnoreFilename)
	dsStoreParser := dsStoreIgnoreParser{}

	return newMultiIgnoreParser(gitParser, dsStoreParser)
}

func compileIgnoreFileAndLines(fs afero.Fs, fpath string, lines ...string) (*ignore.GitIgnore, error) {
	buffer, err := afero.ReadFile(fs, fpath)
	if err != nil {
		return nil, err
	}
	s := strings.Split(string(buffer), "\n")
	return ignore.CompileIgnoreLines(append(s, lines...)...)
}

// getFileName composes filename for zip to match standard specified as
// module@version/{filename}
func getFileName(path, dir, module, version string) string {
	filename := strings.TrimPrefix(path, dir)
	filename = strings.TrimLeftFunc(filename, func(r rune) bool { return r == os.PathSeparator })

	moduleID := fmt.Sprintf("%s@%s", module, version)

	return filepath.Join(moduleID, filename)
}
