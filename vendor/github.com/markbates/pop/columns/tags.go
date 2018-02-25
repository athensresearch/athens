package columns

import (
	"reflect"
	"strings"
)

var tags = "db rw select belongs_to has_many has_one fk_id order_by many_to_many"

// Tag represents a field tag defined exclusively for pop package.
type Tag struct {
	Value string
	Name  string
}

// Empty validates if this pop tag is empty.
func (t Tag) Empty() bool {
	return t.Value == ""
}

// Ignored validates if this pop tag is ignored.
// assuming an ignored tag as "-".
func (t Tag) Ignored() bool {
	return t.Value == "-"
}

// Tags is a group of pop tags defined in just one model field.
type Tags []Tag

// Find find for a specific tag with the name passed as
// a param. returns an empty Tag in case it is not found.
func (t Tags) Find(name string) Tag {
	for _, popTag := range t {
		if popTag.Name == name {
			return popTag
		}
	}
	return Tag{}
}

// TagsFor is a function which returns all tags defined
// in model field.
func TagsFor(field reflect.StructField) Tags {
	pTags := Tags{}
	for _, tag := range strings.Fields(tags) {
		if valTag := field.Tag.Get(tag); valTag != "" {
			pTags = append(pTags, Tag{valTag, tag})
		}
	}

	if len(pTags) == 0 {
		pTags = append(pTags, Tag{field.Name, "db"})
	}
	return pTags
}
