package metadata

import (
	"time"

	"github.com/gobuffalo/uuid"
)

// CDNMetadataEntry stores the module name and cdn URL.
type CDNMetadataEntry struct {
	ID          uuid.UUID `json:"id" bson:"id"`
	CreatedAt   time.Time `json:"created_at" bson:"created_at"`
	UpdatedAt   time.Time `json:"updated_at" bson:"updated_at"`
	Module      string    `json:"module" bson:"module"`
	RedirectURL string    `json:"redirect_url" bson:"redirect_url"`
}

// TableName changes the default name which would be c_d_n_metadata_entry
func (e *CDNMetadataEntry) TableName() string {
	return "cdn_metadata_entries"
}
