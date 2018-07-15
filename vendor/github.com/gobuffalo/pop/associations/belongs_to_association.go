package associations

import (
	"fmt"
	"reflect"

	"github.com/gobuffalo/pop/nulls"
)

// belongsToAssociation is the implementation for the belongs_to
// association type in a model.
type belongsToAssociation struct {
	ownerModel reflect.Value
	ownerType  reflect.Type
	ownerID    reflect.Value
	fkID       string
	ownedModel interface{}
	*associationSkipable
	*associationComposite
}

func init() {
	associationBuilders["belongs_to"] = belongsToAssociationBuilder
}

func belongsToAssociationBuilder(p associationParams) (Association, error) {
	fval := p.modelValue.FieldByName(p.field.Name)
	ownerIDField := fmt.Sprintf("%s%s", p.field.Name, "ID")
	if p.popTags.Find("fk_id").Value != "" {
		ownerIDField = p.popTags.Find("fk_id").Value
	}

	if _, found := p.modelType.FieldByName(ownerIDField); !found {
		return nil, fmt.Errorf("there is no '%s' defined in model '%s'", ownerIDField, p.modelType.Name())
	}

	// Validates if ownerIDField is nil, this association will be skipped.
	var skipped bool
	f := p.modelValue.FieldByName(ownerIDField)
	if fieldIsNil(f) || isZero(f.Interface()) {
		skipped = true
	}

	return &belongsToAssociation{
		ownerModel: fval,
		ownerType:  fval.Type(),
		ownerID:    f,
		fkID:       ownerIDField,
		ownedModel: p.model,
		associationSkipable: &associationSkipable{
			skipped: skipped,
		},
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

func (b *belongsToAssociation) BeforeInterface() interface{} {
	if !b.skipped {
		return nil
	}

	if b.ownerModel.Kind() == reflect.Ptr {
		return b.ownerModel.Interface()
	}

	currentVal := b.ownerModel.Interface()
	zeroVal := reflect.Zero(b.ownerModel.Type()).Interface()
	if reflect.DeepEqual(zeroVal, currentVal) {
		return nil
	}

	return b.ownerModel.Addr().Interface()
}

func (b *belongsToAssociation) BeforeSetup() error {
	ownerID := reflect.Indirect(reflect.ValueOf(b.ownerModel.Interface())).FieldByName("ID").Interface()
	if b.ownerID.CanSet() {
		if n := nulls.New(b.ownerID.Interface()); n != nil {
			b.ownerID.Set(reflect.ValueOf(n.Parse(ownerID)))
		} else {
			b.ownerID.Set(reflect.ValueOf(ownerID))
		}
		return nil
	}
	return fmt.Errorf("could not set '%s' to '%s'", ownerID, b.ownerID)
}
