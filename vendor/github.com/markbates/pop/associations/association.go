package associations

import (
	"reflect"

	"github.com/markbates/pop/columns"
)

// Association represents a definition of a model association
// field. It can represent a association of the type has_many
// belongs_to or has_one, and other customized types.
type Association interface {
	Kind() reflect.Kind
	Interface() interface{}
	Constraint() (string, []interface{})
}

// AssociationSortable a type to be sortable.
type AssociationSortable interface {
	OrderBy() string
	Association
}

// Associations a group of model associations.
type Associations []Association

// SkippedAssociation an empty association used to indicate
// an association should not be queried.
var SkippedAssociation = (Association)(nil)

// associationParams a wrapper for associations definition
// and creation.
type associationParams struct {
	field      reflect.StructField // an association field defined in model.
	modelType  reflect.Type        // the model type where this field is defined.
	modelValue reflect.Value       // the model value where this field is defined.
	popTags    columns.Tags        // the tags defined in this association field.
	model      interface{}         // the model, owner of the association.
}

// associationBuilder is a type representing an association builder implementation.
// see the builder defined in ./has_many_association.go as a guide of how to use it.
type associationBuilder func(associationParams) (Association, error)

// nullable means this type is a nullable association field.
type nullable interface {
	Interface() interface{}
}

// fieldIsNil validates if a field has a nil reference. Also
// it validates if a field implements nullable interface and
// it has a nil value.
func fieldIsNil(f reflect.Value) bool {
	null := (*nullable)(nil)
	t := reflect.TypeOf(f.Interface())
	if t.Implements(reflect.TypeOf(null).Elem()) {
		m := reflect.ValueOf(f.Interface()).MethodByName("Interface")
		out := m.Call([]reflect.Value{})
		idValue := out[0].Interface()
		return idValue == nil
	}
	return f.Interface() == nil
}
