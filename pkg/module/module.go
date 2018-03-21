package module

import (
	"archive/zip"
	"bytes"
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"

	ignore "github.com/sabhiram/go-gitignore"
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
func MakeZip(dir, module, version string) ([]byte, error) {
	ignoreParser := getIgnoreParser(dir)
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

		fileContent, err := ioutil.ReadFile(path)
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

	err := filepath.Walk(dir, walkFunc)
	w.Close()

	return buf.Bytes(), err
}

func getIgnoreParser(dir string) ignore.IgnoreParser {
	gitFilePath := filepath.Join(dir, gitIgnoreFilename)
	gitParser, _ := ignore.CompileIgnoreFileAndLines(gitFilePath, gitIgnoreFilename)
	dsStoreParser := dsStoreIgnoreParser{}

	return newMultiIgnoreParser(gitParser, dsStoreParser)
}

// getFileName composes filename for zip to match standard specified as
// module@version/{filename}
func getFileName(path, dir, module, version string) string {
	filename := strings.TrimPrefix(path, dir)
	filename = strings.TrimLeftFunc(filename, func(r rune) bool { return r == os.PathSeparator })

	moduleID := fmt.Sprintf("%s@%s", module, version)

	return filepath.Join(moduleID, filename)
}
