package env

import (
	"fmt"

	"github.com/gobuffalo/envy"
)

// GCPBucketName returns Google Cloud Storage bucket name defined by ATHENS_STORAGE_GCP_BUCKET
func GCPBucketName() (string, error) {
	env, err := envy.MustGet("ATHENS_STORAGE_GCP_BUCKET")
	if err != nil {
		return "", fmt.Errorf("missing Google Cloud storage bucket name: %s", err)
	}

	return env, nil
}

// GCPProjectID returns the project id on which the project
// is running or the cloud storage is using.
func GCPProjectID() string {
	return envy.Get("GOOGLE_CLOUD_PROJECT", "")
}
