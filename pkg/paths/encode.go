package paths

import (
	"unicode/utf8"

	"github.com/gomods/athens/pkg/errors"
)

// EncodePath is ripped from cmd/go -- it replaces upper case
// letters with bang+lowercase.
func EncodePath(s string) (encoding string, err error) {
	const op errors.Op = "paths.EncodePath"
	haveUpper := false
	for _, r := range s {
		if r == '!' || r >= utf8.RuneSelf {
			// This should be disallowed by CheckPath, but diagnose anyway.
			// The correctness of the encoding loop below depends on it.
			return "", errors.E(op, "internal error: inconsistency in EncodePath")
		}
		if 'A' <= r && r <= 'Z' {
			haveUpper = true
		}
	}

	if !haveUpper {
		return s, nil
	}

	var buf []byte
	for _, r := range s {
		if 'A' <= r && r <= 'Z' {
			buf = append(buf, '!', byte(r+'a'-'A'))
		} else {
			buf = append(buf, byte(r))
		}
	}
	return string(buf), nil
}
