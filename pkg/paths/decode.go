package paths

import (
	"fmt"
	"unicode/utf8"

	"github.com/gomods/athens/pkg/errors"
)

// DecodePath returns the module path of the given safe encoding.
// It fails if the encoding is invalid or encodes an invalid path.
func DecodePath(encoding string) (path string, err error) {
	const op errors.Op = "paths.DecodePath"
	path, ok := decodeString(encoding)
	if !ok {
		return "", errors.E(op, fmt.Sprintf("invalid module path encoding %q", encoding))
	}

	return path, nil
}

// Ripped from cmd/go
func decodeString(encoding string) (string, bool) {
	var buf []byte

	bang := false
	for _, r := range encoding {
		if r >= utf8.RuneSelf {
			return "", false
		}
		if bang {
			bang = false
			if r < 'a' || 'z' < r {
				return "", false
			}
			buf = append(buf, byte(r+'A'-'a'))
			continue
		}
		if r == '!' {
			bang = true
			continue
		}
		if 'A' <= r && r <= 'Z' {
			return "", false
		}
		buf = append(buf, byte(r))
	}
	if bang {
		return "", false
	}
	return string(buf), true
}
