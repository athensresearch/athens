package associations

import (
	"fmt"
	"reflect"
	"regexp"
	"strings"

	"github.com/gobuffalo/pop/columns"
)

// If a field match with the regexp, it will be considered as a valid field definition.
// e.g: "MyField"             => valid.
// e.g: "MyField.NestedField" => valid.
// e.g: "MyField."            => not valid.
// e.g: "MyField.*"           => not valid for now.
var validAssociationExpRegexp = regexp.MustCompile(`^(([a-zA-Z0-9]*)(\.[a-zA-Z0-9]+)?)+$`)

// associationBuilders is a map that helps to aisle associations finding process
// with the associations implementation. Every association MUST register its builder
// in this map using its init() method. see ./has_many_association.go as a guide.
var associationBuilders = map[string]associationBuilder{}

// AssociationsForStruct returns all associations for
// the struct specified. It takes into account tags
// associations like has_many, belongs_to, has_one.
// it throws an error when it finds a field that does
// not exist for a model.
func AssociationsForStruct(s interface{}, fields ...string) (Associations, error) {
	associations := Associations{}
	innerAssociations := InnerAssociations{}

	t, v := getModelDefinition(s)
	fields = trimFields(fields)

	// validate if fields contains a non existing field in struct.
	// and vefiry is it has inner associations.
	for i := range fields {
		var innerField, field string

		if !validAssociationExpRegexp.MatchString(fields[i]) {
			return associations, fmt.Errorf("association '%s' does not match the format %s", fields[i], "'<field>' or '<field>.<nested-field>'")
		}

		if strings.Contains(fields[i], ".") {
			field = fields[i][:strings.Index(fields[i], ".")]
			innerField = fields[i][strings.Index(fields[i], ".")+1:]
			fields[i] = field
		}
		if _, ok := t.FieldByName(fields[i]); !ok {
			return associations, fmt.Errorf("field %s does not exist in model %s", fields[i], t.Name())
		}

		if innerField != "" {
			innerAssociations = append(innerAssociations, InnerAssociation{fields[i], innerField})
		}
	}

	for i := 0; i < t.NumField(); i++ {
		f := t.Field(i)

		// ignores those fields not included in fields list.
		if len(fields) > 0 && fieldIgnoredIn(fields, f.Name) {
			continue
		}

		tags := columns.TagsFor(f)

		for name, builder := range associationBuilders {
			tag := tags.Find(name)
			if !tag.Empty() {
				params := associationParams{
					field:             f,
					model:             s,
					modelType:         t,
					modelValue:        v,
					popTags:           tags,
					innerAssociations: innerAssociations,
				}

				a, err := builder(params)
				if err != nil {
					return associations, err
				}

				associations = append(associations, a)
				break
			}
		}
	}

	return associations, nil
}

func getModelDefinition(s interface{}) (reflect.Type, reflect.Value) {
	v := reflect.ValueOf(s)
	v = reflect.Indirect(v)
	t := v.Type()
	return t, v
}

func trimFields(fields []string) []string {
	trimFields := []string{}
	for _, f := range fields {
		if strings.TrimSpace(f) != "" {
			trimFields = append(trimFields, strings.TrimSpace(f))
		}
	}
	return trimFields
}

func fieldIgnoredIn(fields []string, field string) bool {
	for _, f := range fields {
		if f == field {
			return false
		}
	}
	return true
}
