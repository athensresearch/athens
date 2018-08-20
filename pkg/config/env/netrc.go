package env

import (
	"os"
)

// NETRCPath tells you where the .netrc path initially resides.
// This is so that you can mount the .netrc file to a secret location
// in the fs system and then move it ~/.netrc. In certain deployments
// like Kubernetes, we can't mount directly to ~ because it would then
// clean out whatever is already there as part of the image (such as
// .cache directory in the Go image).
func NETRCPath() string {
	return os.Getenv("ATHENS_NETRC_PATH")
}
