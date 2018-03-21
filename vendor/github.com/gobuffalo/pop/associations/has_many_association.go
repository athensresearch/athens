package associations

import (
	"fmt"
	"reflect"

	"github.com/markbates/inflect"
)

// hasManyAssociation is the implementation for the has_many
// association type in a model.
type hasManyAssociation struct {
	tableName string
	field     reflect.StructField
	value     reflect.Value
	ownerName string
	ownerID   interface{}
	fkID      string
	orderBy   string
	*associationComposite
}

func init() {
	associationBuilders["has_many"] = hasManyAssociationBuilder
}

func hasManyAssociationBuilder(p associationParams) (Association, error) {
	// Validates if ownerID is nil, this association will be skipped.
	ownerID := p.modelValue.FieldByName("ID")
	if fieldIsNil(ownerID) {
		return SkippedAssociation, nil
	}

	return &hasManyAssociation{
		tableName:            p.popTags.Find("has_many").Value,
		field:                p.field,
		value:                p.modelValue.FieldByName(p.field.Name),
		ownerName:            p.modelType.Name(),
		ownerID:              ownerID.Interface(),
		fkID:                 p.popTags.Find("fk_id").Value,
		orderBy:              p.popTags.Find("order_by").Value,
		associationComposite: &associationComposite{innerAssociations: p.innerAssociations},
	}, nil
}

func (a *hasManyAssociation) Kind() reflect.Kind {
	if a.field.Type.Kind() == reflect.Ptr {
		return a.field.Type.Elem().Kind()
	}
	return a.field.Type.Kind()
}

func (a *hasManyAssociation) Interface() interface{} {
	if a.value.Kind() == reflect.Ptr {
		val := reflect.New(a.field.Type.Elem())
		a.value.Set(val)
		return a.value.Interface()
	}
	return a.value.Addr().Interface()
}

// Constraint returns the content for a where clause, and the args
// needed to execute it.
func (a *hasManyAssociation) Constraint() (string, []interface{}) {
	tn := inflect.Underscore(a.ownerName)
	condition := fmt.Sprintf("%s_id = ?", tn)
	if a.fkID != "" {
		condition = fmt.Sprintf("%s = ?", a.fkID)
	}
	return condition, []interface{}{a.ownerID}
}

func (a *hasManyAssociation) OrderBy() string {
	return a.orderBy
}
