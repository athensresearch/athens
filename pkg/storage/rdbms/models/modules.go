package models

import (
	"encoding/json"
	"time"

	"github.com/gobuffalo/pop"
	"github.com/gobuffalo/uuid"
	"github.com/gobuffalo/validate"
	"github.com/gobuffalo/validate/validators"
)

// Module is a model where data is stored.
type Module struct {
	ID        uuid.UUID `json:"id" db:"id"`
	CreatedAt time.Time `json:"created_at" db:"created_at"`
	UpdatedAt time.Time `json:"updated_at" db:"updated_at"`
	Module    string    `json:"module" db:"module"`
	Version   string    `json:"version" db:"version"`
	Mod       []byte    `json:"mod" db:"mod"`
	Zip       []byte    `json:"zip" db:"zip"`
	Info      []byte    `json:"info" db:"info"`
}

// String is not required by pop and may be deleted
func (m Module) String() string {
	jm, _ := json.Marshal(m)
	return string(jm)
}

// Modules is not required by pop and may be deleted
type Modules []Module

// String is not required by pop and may be deleted
func (m Modules) String() string {
	jm, _ := json.Marshal(m)
	return string(jm)
}

// Validate gets run every time you call a "pop.Validate*" (pop.ValidateAndSave, pop.ValidateAndCreate, pop.ValidateAndUpdate) method.
// This method is not required and may be deleted.
func (m *Module) Validate(tx *pop.Connection) (*validate.Errors, error) {
	return validate.Validate(
		&validators.StringIsPresent{Field: m.Module, Name: "Module"},
		&validators.StringIsPresent{Field: m.Version, Name: "Version"},
		&validators.BytesArePresent{Field: m.Mod, Name: "Mod"},
		&validators.BytesArePresent{Field: m.Zip, Name: "Zip"},
		&validators.BytesArePresent{Field: m.Info, Name: "Info"},
	), nil
}

// ValidateCreate gets run every time you call "pop.ValidateAndCreate" method.
// This method is not required and may be deleted.
func (m *Module) ValidateCreate(tx *pop.Connection) (*validate.Errors, error) {
	return validate.NewErrors(), nil
}

// ValidateUpdate gets run every time you call "pop.ValidateAndUpdate" method.
// This method is not required and may be deleted.
func (m *Module) ValidateUpdate(tx *pop.Connection) (*validate.Errors, error) {
	return validate.NewErrors(), nil
}
