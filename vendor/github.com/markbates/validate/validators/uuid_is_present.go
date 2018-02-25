package validators

import (
	"fmt"
	"strings"

	"github.com/markbates/validate"
	"github.com/satori/go.uuid"
)

type UUIDIsPresent struct {
	Name  string
	Field uuid.UUID
}

func (v *UUIDIsPresent) IsValid(errors *validate.Errors) {
	s := v.Field.String()
	if strings.TrimSpace(s) == "" || v.Field == uuid.Nil {
		errors.Add(GenerateKey(v.Name), fmt.Sprintf("%s can not be blank.", v.Name))
	}
}
