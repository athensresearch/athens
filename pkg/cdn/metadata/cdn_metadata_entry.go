package metadata

import (
	"encoding/json"
	"time"

	"github.com/gobuffalo/pop"
	"github.com/gobuffalo/uuid"
	"github.com/gobuffalo/validate"
	"github.com/gobuffalo/validate/validators"
)

// CDNMetadataEntry stores the module name and cdn URL.
type CDNMetadataEntry struct {
	ID          uuid.UUID `json:"id" db:"id" bson:"id"`
	CreatedAt   time.Time `json:"created_at" db:"created_at" bson:"created_at"`
	UpdatedAt   time.Time `json:"updated_at" db:"updated_at" bson:"updated_at"`
	Module      string    `json:"module" db:"module" bson:"module"`
	RedirectURL string    `json:"redirect_url" db:"redirect_url" bson:"redirect_url"`
}

// String is not required by pop and may be deleted
func (e CDNMetadataEntry) String() string {
	je, _ := json.Marshal(e)
	return string(je)
}

// CdnMetadataEntries is not required by pop and may be deleted
type CdnMetadataEntries []CDNMetadataEntry

// Validate gets run every time you call a "pop.Validate*" (pop.ValidateAndSave, pop.ValidateAndCreate, pop.ValidateAndUpdate) method.
// This method is not required and may be deleted.
func (e *CDNMetadataEntry) Validate(tx *pop.Connection) (*validate.Errors, error) {
	return validate.Validate(
		&validators.StringIsPresent{Field: e.Module, Name: "Module"},
		&validators.StringIsPresent{Field: e.RedirectURL, Name: "RedirectURL"},
	), nil
}

// ValidateCreate gets run every time you call "pop.ValidateAndCreate" method.
// This method is not required and may be deleted.
func (e *CDNMetadataEntry) ValidateCreate(tx *pop.Connection) (*validate.Errors, error) {
	return validate.NewErrors(), nil
}

// ValidateUpdate gets run every time you call "pop.ValidateAndUpdate" method.
// This method is not required and may be deleted.
func (e *CDNMetadataEntry) ValidateUpdate(tx *pop.Connection) (*validate.Errors, error) {
	return validate.NewErrors(), nil
}

// TableName changes the default name which would be c_d_n_metadata_entry
func (e *CDNMetadataEntry) TableName() string {
	return "cdn_metadata_entries"
}
