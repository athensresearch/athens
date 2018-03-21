package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"path/filepath"

	"github.com/spf13/cobra"

	parser "github.com/gomods/athens/pkg/gomod/file"
	"github.com/gomods/athens/pkg/module"
	"github.com/gomods/athens/pkg/payloads"
)

type uploadCmd struct {
	baseURL    string
	moduleName string
	version    string
}

func newUploadCmd() *cobra.Command {
	uploadCmd := &uploadCmd{}
	cmd := &cobra.Command{
		Use:   "upload [directory]",
		Short: "package up a directory and upload it to the athens server",
		Args:  cobra.ExactArgs(1),
		RunE:  upload(uploadCmd),
	}
	cmd.Flags().StringVarP(&uploadCmd.baseURL, "base-url", "b", "", "The base URL of the module (required)")
	cmd.MarkFlagRequired("base-url")
	cmd.Flags().StringVarP(&uploadCmd.version, "version", "v", "v0.0.1", "The version of this module")
	return cmd
}

func upload(c *uploadCmd) func(*cobra.Command, []string) error {
	return func(cmd *cobra.Command, args []string) error {
		dir := args[0]

		fullDirectory, err := filepath.Abs(dir)
		if err != nil {
			return fmt.Errorf("couldn't get full directory (%s)", err)
		}
		cmd.Printf("found directory %s", fullDirectory)
		modFilePath := filepath.Join(fullDirectory, "go.mod")
		modBytes, err := ioutil.ReadFile(modFilePath)
		if err != nil {
			return fmt.Errorf("couldn't find go.mod file (%s)", err)
		}

		gomodParser := parser.NewFileParser(modFilePath)
		c.moduleName, err = gomodParser.ModuleName()
		if err != nil {
			return fmt.Errorf("couldn't parse go.mod file (%s)", err)
		}

		zipBytes, err := module.MakeZip(fullDirectory, c.baseURL, c.moduleName, c.version)
		if err != nil {
			return fmt.Errorf("couldn't make zip (%s)", err)
		}

		url := fmt.Sprintf(
			"http://localhost:3000/admin/upload/%s/%s/%s",
			c.baseURL,
			c.moduleName,
			c.version,
		)
		postBody := &payloads.Upload{
			Module: modBytes,
			Zip:    zipBytes,
		}
		buf := new(bytes.Buffer)
		if err := json.NewEncoder(buf).Encode(postBody); err != nil {
			return fmt.Errorf("error encoding json (%s)", err)
		}
		cmd.Printf("POSTing to %s", url)
		resp, err := http.Post(url, "application/json", buf)
		if err != nil {
			return fmt.Errorf("error uploading (%s)", err)
		} else if resp.StatusCode != 200 {
			return fmt.Errorf("upload failed because status code was %d", resp.StatusCode)
		}
		cmd.Println("Upload successful!")
		return nil
	}
}
