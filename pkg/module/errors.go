package module

import (
	"fmt"

	"github.com/gomods/athens/pkg/errors"
)

// ErrModuleExcluded is error returned when processing of error is skipped
// due to filtering rules
type ErrModuleExcluded struct {
	module string
}

func (e *ErrModuleExcluded) Error() string {
	return fmt.Sprintf("Module %s is excluded", e.module)
}

// NewErrModuleExcluded creates new ErrModuleExcluded
func NewErrModuleExcluded(module string) error {
	return &ErrModuleExcluded{module: module}
}

// NewErrModuleAlreadyFetched returns an error indicating that a module has already been
// fetched
func NewErrModuleAlreadyFetched(op errors.Op, mod, ver string) error {
	return errors.E(op, errors.M(mod), errors.V(ver), errors.KindAlreadyExists)
}
