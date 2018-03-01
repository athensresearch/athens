package validators

import (
	"fmt"

	"github.com/gobuffalo/validate"
	"github.com/markbates/going/defaults"
)

type FuncValidator struct {
	Fn      func() bool
	Field   string
	Name    string
	Message string
}

func (f *FuncValidator) IsValid(verrs *validate.Errors) {
	// for backwards compatability
	f.Name = defaults.String(f.Name, f.Field)
	if !f.Fn() {
		verrs.Add(GenerateKey(f.Name), fmt.Sprintf(f.Message, f.Field))
	}
}
