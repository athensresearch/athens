package storage

import (
	"fmt"
)

type NotFoundErr struct {
	BasePath string
	Module   string
}

func (n NotFoundErr) Error() string {
	return fmt.Sprintf("%s/%s not found", n.BasePath, n.Module)
}

func IsNotFoundError(err error) bool {
	_, ok := err.(NotFoundErr)
	return ok
}
