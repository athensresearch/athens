package env

import (
	"fmt"

	"github.com/gobuffalo/envy"
)

// MinioEndpoint returns Minio endpoing URI defined by ATHENS_MINIO_ENDPOINT.
func MinioEndpoint() (string, error) {
	env, err := envy.MustGet("ATHENS_MINIO_ENDPOINT")
	if err != nil {
		return "", fmt.Errorf("missing minio endpoint: %s", err)
	}

	return env, nil
}

// MinioAccessKeyID returns Minio access key ID defined by ATHENS_MINIO_ACCESS_KEY_ID.
func MinioAccessKeyID() (string, error) {
	env, err := envy.MustGet("ATHENS_MINIO_ACCESS_KEY_ID")
	if err != nil {
		return "", fmt.Errorf("missing minio access key ID: %s", err)
	}

	return env, nil
}

// MinioSecretAccessKey returns Minio secret access key defined by ATHENS_MINIO_SECRET_ACCESS_KEY.
func MinioSecretAccessKey() (string, error) {
	env, err := envy.MustGet("ATHENS_MINIO_SECRET_ACCESS_KEY")
	if err != nil {
		return "", fmt.Errorf("missing minio secret access key ID: %s", err)
	}

	return env, nil
}

// MinioBucketNameWithDefault returns bucket name used with Minio. Defined by ATHENS_MINIO_BUCKET_NAME.
func MinioBucketNameWithDefault(value string) string {
	return envy.Get("ATHENS_MINIO_BUCKET_NAME", value)
}

// MinioSSLWithDefault returns flag whether or not SSL should be used with Minio. Defined by ATHENS_MINIO_USE_SSL.
func MinioSSLWithDefault(value string) string {
	return envy.Get("ATHENS_MINIO_USE_SSL", value)
}
