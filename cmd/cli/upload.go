package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"net/url"
	"path"
	"path/filepath"

	parser "github.com/gomods/athens/pkg/gomod/file"
	"github.com/gomods/athens/pkg/module"
	"github.com/gomods/athens/pkg/payloads"
	"github.com/spf13/afero"
	"github.com/spf13/cobra"
)

type uploadCmd struct {
	moduleName string
	version    string
	baseURL    string
}

func newUploadCmd() *cobra.Command {
	uploadCmd := &uploadCmd{}
	cmd := &cobra.Command{
		Use:   "upload [directory]",
		Short: "package up a directory and upload it to the athens server",
		Args:  cobra.ExactArgs(1),
		RunE:  upload(uploadCmd),
	}
	cmd.Flags().StringVarP(&uploadCmd.version, "version", "v", "v0.0.1", "The version of this module")
	cmd.Flags().StringVarP(&uploadCmd.baseURL, "base-url", "u", "http://localhost:3000/admin/upload", "The Athens base url.")
	return cmd
}

func upload(c *uploadCmd) func(*cobra.Command, []string) error {
	return func(cmd *cobra.Command, args []string) error {
		fs := afero.NewOsFs()
		dir := args[0]

		fullDirectory, err := filepath.Abs(dir)
		if err != nil {
			return fmt.Errorf("couldn't get full directory (%s)", err)
		}
		cmd.Printf("found directory %s", fullDirectory)
		modFilePath := filepath.Join(fullDirectory, "go.mod")
		modBytes, err := afero.ReadFile(fs, modFilePath)
		if err != nil {
			return fmt.Errorf("couldn't find go.mod file (%s)", err)
		}

		gomodParser := parser.NewFileParser(fs, modFilePath)
		c.moduleName, err = gomodParser.ModuleName()
		if err != nil {
			return fmt.Errorf("couldn't parse go.mod file (%s)", err)
		}

		zipBytes, err := module.MakeZip(fs, fullDirectory, c.moduleName, c.version)
		if err != nil {
			return fmt.Errorf("couldn't make zip (%s)", err)
		}

		infoFilePath := filepath.Join(fullDirectory, c.version+".info")
		infoBytes, err := afero.ReadFile(fs, infoFilePath)
		if err != nil {
			return fmt.Errorf("coudln't find .info file (%s)", err)
		}

		u, err := url.Parse(c.baseURL)
		if err != nil {
			return fmt.Errorf("not a valid base url (%s)", err)
		}

		u.Path = path.Join(u.Path, c.moduleName, c.version)
		postBody := &payloads.Upload{
			Module: modBytes,
			Zip:    zipBytes,
			Info:   infoBytes,
		}
		buf := new(bytes.Buffer)
		if err := json.NewEncoder(buf).Encode(postBody); err != nil {
			return fmt.Errorf("error encoding json (%s)", err)
		}
		cmd.Printf("POSTing to %s", u)
		resp, err := http.Post(u.String(), "application/json", buf)
		if err != nil {
			return fmt.Errorf("error uploading (%s)", err)
		} else if resp.StatusCode != 200 {
			return fmt.Errorf("upload failed because status code was %d", resp.StatusCode)
		}
		cmd.Println("Upload successful!")
		return nil
	}
}
