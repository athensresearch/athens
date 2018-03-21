package associations

import (
	"fmt"
	"reflect"
)

// belongsToAssociation is the implementation for the belongs_to
// association type in a model.
type belongsToAssociation struct {
	ownerModel reflect.Value
	ownerType  reflect.Type
	ownerID    reflect.Value
	owner      interface{}
	*associationComposite
}

func init() {
	associationBuilders["belongs_to"] = belongsToAssociationBuilder
}

func belongsToAssociationBuilder(p associationParams) (Association, error) {
	fval := p.modelValue.FieldByName(p.field.Name)
	ownerIDField := fmt.Sprintf("%s%s", p.field.Name, "ID")

	if _, found := p.modelType.FieldByName(ownerIDField); !found {
		return nil, fmt.Errorf("there is no '%s' defined in model '%s'", ownerIDField, p.modelType.Name())
	}

	// Validates if ownerIDField is nil, this association will be skipped.
	f := p.modelValue.FieldByName(ownerIDField)
	if fieldIsNil(f) {
		return SkippedAssociation, nil
	}

	return &belongsToAssociation{
		ownerModel:           fval,
		ownerType:            fval.Type(),
		ownerID:              f,
		owner:                p.model,
		associationComposite: &associationComposite{innerAssociations: p.innerAssociations},
	}, nil
}

func (b *belongsToAssociation) Kind() reflect.Kind {
	if b.ownerType.Kind() == reflect.Ptr {
		return b.ownerType.Elem().Kind()
	}
	return b.ownerType.Kind()
}

func (b *belongsToAssociation) Interface() interface{} {
	if b.ownerModel.Kind() == reflect.Ptr {
		val := reflect.New(b.ownerType.Elem())
		b.ownerModel.Set(val)
		return b.ownerModel.Interface()
	}
	return b.ownerModel.Addr().Interface()
}

// Constraint returns the content for a where clause, and the args
// needed to execute it.
func (b *belongsToAssociation) Constraint() (string, []interface{}) {
	return "id = ?", []interface{}{b.ownerID.Interface()}
}
