// Package zip provides a helper for the archive/zip package (archive/unarchive to/from a file/reader/writer)
package zip

import (
	zip_impl "archive/zip"
	"io"
	"os"
	"path"
	"path/filepath"
	"strings"
)

// Archive compresses a file/directory to a writer
//
// If the path ends with a separator, then the contents of the folder at that path
// are at the root level of the archive, otherwise, the root of the archive contains
// the folder as its only item (with contents inside).
//
// If progress is not nil, it is called for each file added to the archive.
func Archive(inFilePath string, writer io.Writer, progress ProgressFunc) error {
	zipWriter := zip_impl.NewWriter(writer)

	basePath := filepath.Dir(inFilePath)

	err := filepath.Walk(inFilePath, func(filePath string, fileInfo os.FileInfo, err error) error {
		if err != nil || fileInfo.IsDir() {
			return err
		}

		relativeFilePath, err := filepath.Rel(basePath, filePath)
		if err != nil {
			return err
		}

		archivePath := path.Join(filepath.SplitList(relativeFilePath)...)

		if progress != nil {
			progress(archivePath)
		}

		file, err := os.Open(filePath)
		if err != nil {
			return err
		}
		defer func() {
			_ = file.Close()
		}()

		zipFileWriter, err := zipWriter.Create(archivePath)
		if err != nil {
			return err
		}

		_, err = io.Copy(zipFileWriter, file)
		return err
	})
	if err != nil {
		return err
	}

	return zipWriter.Close()
}

// ArchiveFile compresses a file/directory to a file
//
// See Archive() doc
func ArchiveFile(inFilePath string, outFilePath string, progress ProgressFunc) error {
	outFile, err := os.Create(outFilePath)
	if err != nil {
		return err
	}
	defer func() {
		_ = outFile.Close()
	}()

	return Archive(inFilePath, outFile, progress)
}

// Unarchive decompresses a reader to a directory
//
// The data's size is required because the zip reader needs it.
//
// The archive's content will be extracted directly to outFilePath.
//
// If progress is not nil, it is called for each file extracted from the archive.
func Unarchive(reader io.ReaderAt, readerSize int64, outFilePath string, progress ProgressFunc) error {
	zipReader, err := zip_impl.NewReader(reader, readerSize)
	if err != nil {
		return err
	}

	for _, zipFile := range zipReader.File {
		err := unarchiveFile(zipFile, outFilePath, progress)
		if err != nil {
			return err
		}
	}

	return nil
}

// UnarchiveFile decompresses a file to a directory
//
// See Unarchive() doc
func UnarchiveFile(inFilePath string, outFilePath string, progress ProgressFunc) error {
	inFile, err := os.Open(inFilePath)
	if err != nil {
		return err
	}
	defer func() {
		_ = inFile.Close()
	}()

	inFileInfo, err := inFile.Stat()
	if err != nil {
		return err
	}
	inFileSize := inFileInfo.Size()

	return Unarchive(inFile, inFileSize, outFilePath, progress)
}

func unarchiveFile(zipFile *zip_impl.File, outFilePath string, progress ProgressFunc) error {
	if zipFile.FileInfo().IsDir() {
		return nil
	}

	if progress != nil {
		progress(zipFile.Name)
	}

	zipFileReader, err := zipFile.Open()
	if err != nil {
		return err
	}
	defer func() {
		_ = zipFileReader.Close()
	}()

	filePath := filepath.Join(outFilePath, filepath.Join(strings.Split(zipFile.Name, "/")...))

	err = os.MkdirAll(filepath.Dir(filePath), os.FileMode(0755))
	if err != nil {
		return err
	}

	file, err := os.Create(filePath)
	if err != nil {
		return err
	}
	defer func() {
		_ = file.Close()
	}()

	_, err = io.Copy(file, zipFileReader)
	return err
}

// ProgressFunc is the type of the function called for each archive file.
type ProgressFunc func(archivePath string)
