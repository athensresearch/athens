package main

import (
	"log"

	"github.com/spf13/cobra"
)

func main() {
	cmd := &cobra.Command{
		Use:          "athens",
		Short:        "The athens dependency manager",
		Example:      usage,
		SilenceUsage: true,
	}

	cmd.AddCommand(newUploadCmd())

	if err := cmd.Execute(); err != nil {
		log.Fatal(err)
	}

}
