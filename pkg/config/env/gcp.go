package env

import (
	"fmt"

	"github.com/gobuffalo/envy"
)

// GcpBucketName returns Google Cloud Storage bucket name defined by ATHENS_STORAGE_GCP_BUCKET
func GcpBucketName() (string, error) {
	env, err := envy.MustGet("ATHENS_STORAGE_GCP_BUCKET")
	if err != nil {
		return "", fmt.Errorf("missing Google Cloud storage bucket name: %s", err)
	}

	return env, nil
}
