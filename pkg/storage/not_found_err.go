package storage

import (
	"fmt"
)

type NotFoundErr struct {
	BasePath string
	Module   string
}

type ErrVersionNotFound struct {
	NotFoundErr
	Version string
}

func (n NotFoundErr) Error() string {
	return fmt.Sprintf("%s/%s not found", n.BasePath, n.Module)
}

func IsNotFoundError(err error) bool {
	_, ok := err.(NotFoundErr)
	return ok
}

type ErrVersionAlreadyExists struct {
	ErrVersionNotFound
}

func (e ErrVersionAlreadyExists) Error() string {
	return fmt.Sprintf("%s/%s@%s", e.BasePath, e.Module, e.Version)
}

func IsVersionAlreadyExistsErr(err error) bool {
	_, ok := err.(ErrVersionAlreadyExists)
	return ok
}
