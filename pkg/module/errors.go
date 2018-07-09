package module

import "fmt"

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
