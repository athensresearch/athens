package storage

import (
	"fmt"
)

// ErrNotFound is an error implementation that indicates a module
// doesn't exist
type ErrNotFound struct {
	Module string
}

func (n ErrNotFound) Error() string {
	return fmt.Sprintf("module %s not found", n.Module)
}

// ErrVersionNotFound is an error implementation that indicates a module
// at a specific version doesn't exist
type ErrVersionNotFound struct {
	Module  string
	Version string
}

func (e ErrVersionNotFound) Error() string {
	return fmt.Sprintf("module %s@%s not found", e.Module, e.Version)
}

// IsNotFoundError returns true if err is an ErrNotFound
func IsNotFoundError(err error) bool {
	if _, ok := err.(ErrNotFound); ok {
		return ok
	}

	_, ok := err.(ErrVersionNotFound)
	return ok
}

// ErrVersionAlreadyExists is an error implementation that indicates that a
// module@version already exists
type ErrVersionAlreadyExists struct {
	Module  string
	Version string
}

func (e ErrVersionAlreadyExists) Error() string {
	return fmt.Sprintf("%s@%s already exists", e.Module, e.Version)
}

// IsVersionAlreadyExistsErr returns true if err is an ErrVersionAlreadyExists
func IsVersionAlreadyExistsErr(err error) bool {
	_, ok := err.(ErrVersionAlreadyExists)
	return ok
}
