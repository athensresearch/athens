package form

import (
	"database/sql/driver"
	"fmt"
	"reflect"
	"strings"
	"sync"

	"github.com/gobuffalo/tags"
	"github.com/markbates/inflect"
	"github.com/markbates/validate"
	"github.com/satori/go.uuid"
)

//FormFor is a form made for a struct
type FormFor struct {
	*Form
	Model      interface{}
	name       string
	dashedName string
	reflection reflect.Value
	Errors     *validate.Errors
}

//NewFormFor creates a new Formfor with passed options, it also creates the id of the form from the struct name and adds errors if present.
func NewFormFor(model interface{}, opts tags.Options) *FormFor {
	rv := reflect.ValueOf(model)
	if rv.Kind() == reflect.Ptr {
		rv = rv.Elem()
	}
	name := rv.Type().Name()
	dashedName := inflect.Dasherize(name)

	if opts["id"] == nil {
		opts["id"] = fmt.Sprintf("%s-form", dashedName)
	}

	errors := loadErrors(opts)
	delete(opts, "errors")

	return &FormFor{
		Form:       New(opts),
		Model:      model,
		name:       name,
		dashedName: dashedName,
		reflection: rv,
		Errors:     errors,
	}
}

func loadErrors(opts tags.Options) *validate.Errors {
	errors := validate.NewErrors()
	if opts["errors"] != nil {
		switch t := opts["errors"].(type) {
		default:
			fmt.Printf("Unexpected errors type %T, please\n", t) // %T prints whatever type t has
		case map[string][]string:
			errors = &validate.Errors{
				Errors: opts["errors"].(map[string][]string),
				Lock:   new(sync.RWMutex),
			}
		case *validate.Errors:
			errors = opts["errors"].(*validate.Errors)
		}
	}

	return errors
}

//CheckboxTag creates a checkbox for a field on the form Struct
func (f FormFor) CheckboxTag(field string, opts tags.Options) *tags.Tag {
	f.buildOptions(field, opts)
	return f.Form.CheckboxTag(opts)
}

//InputTag creates an input for a field on the form Struct
func (f FormFor) InputTag(field string, opts tags.Options) *tags.Tag {
	f.buildOptions(field, opts)
	f.addFormatTag(field, opts)
	return f.Form.InputTag(opts)
}

//HiddenTag adds a wrappter for input type hidden on the form
func (f FormFor) HiddenTag(field string, opts tags.Options) *tags.Tag {
	f.buildOptions(field, opts)
	return f.Form.HiddenTag(opts)
}

//FileTag creates a input[type=file] for a field name passed
func (f FormFor) FileTag(field string, opts tags.Options) *tags.Tag {
	f.buildOptions(field, opts)
	f.addFormatTag(field, opts)
	return f.Form.FileTag(opts)
}

//DateTimeTag creates a input[type=datetime-local] for a field name passed
func (f FormFor) DateTimeTag(field string, opts tags.Options) *tags.Tag {
	f.buildOptions(field, opts)
	f.addFormatTag(field, opts)
	return f.Form.DateTimeTag(opts)
}

func (f FormFor) addFormatTag(field string, opts tags.Options) {
	if opts["format"] != nil {
		return
	}

	toff := reflect.TypeOf(f.Model)
	if toff.Kind() == reflect.Ptr {
		toff = toff.Elem()
	}

	if toff.Kind() == reflect.Struct {
		fi, found := toff.FieldByName(field)

		if !found {
			return
		}

		if format, ok := fi.Tag.Lookup("format"); ok && format != "" {
			opts["format"] = format
		}
	}
}

//RadioButton creates a radio button for a struct field
func (f FormFor) RadioButton(field string, opts tags.Options) *tags.Tag {
	return f.RadioButton(field, opts)
}

//RadioButtonTag creates a radio button for a struct field
func (f FormFor) RadioButtonTag(field string, opts tags.Options) *tags.Tag {
	f.buildOptions(field, opts)
	return f.Form.RadioButton(opts)
}

//SelectTag creates a select tag for a specified struct field and loads options from the options opject
func (f FormFor) SelectTag(field string, opts tags.Options) *SelectTag {
	f.buildOptions(field, opts)
	return f.Form.SelectTag(opts)
}

//TextArea creates text area for the specified struct field
func (f FormFor) TextArea(field string, opts tags.Options) *tags.Tag {
	return f.TextAreaTag(field, opts)
}

//TextAreaTag creates text area for the specified struct field
func (f FormFor) TextAreaTag(field string, opts tags.Options) *tags.Tag {
	f.buildOptions(field, opts)
	return f.Form.TextArea(opts)
}

//SubmitTag adds a submit button to the form
func (f FormFor) SubmitTag(value string, opts tags.Options) *tags.Tag {
	return f.Form.SubmitTag(value, opts)
}

func (f FormFor) buildOptions(field string, opts tags.Options) {

	if opts["value"] == nil {
		opts["value"] = f.value(field)
	}

	if opts["name"] == nil {
		opts["name"] = f.findFieldNameFor(field)
	}

	if opts["id"] == nil {
		opts["id"] = fmt.Sprintf("%s-%s", f.dashedName, opts["name"])
	}

}

type interfacer interface {
	Interface() interface{}
}

type tagValuer interface {
	TagValue() string
}

func (f FormFor) value(field string) interface{} {
	fn := f.reflection.FieldByName(field)

	if fn.IsValid() == false {
		dots := strings.Split(field, ".")
		if len(dots) == 1 {
			if !strings.HasSuffix(field, "ID") {
				return f.value(field + "ID")
			}
			return ""
		}
		fn = f.reflection.FieldByName(dots[0])
		if fn.IsValid() {
			ff := NewFormFor(fn.Interface(), f.Options)
			return ff.value(strings.Join(dots[1:], "."))
		}
	}

	fn = reflect.Indirect(fn)

	i := fn.Interface()
	switch t := i.(type) {
	case uuid.UUID:
		return t.String()
	case tagValuer:
		return t.TagValue()
	case driver.Valuer:
		value, _ := t.Value()

		if value == nil {
			return ""
		}

		return fmt.Sprintf("%v", value)
	case interfacer:
		return fmt.Sprintf("%v", t.Interface())
	}
	return i
}

func (f FormFor) findFieldNameFor(field string) string {
	ty := reflect.TypeOf(f.Model)

	if ty.Kind() == reflect.Ptr {
		ty = ty.Elem()
	}

	rf, ok := ty.FieldByName(field)
	if !ok {
		if rf, ok = ty.FieldByName(field + "ID"); !ok {
			return field
		}
		field = field + "ID"
	}

	formDefined := string(rf.Tag.Get("form"))
	if formDefined != "" && formDefined != "-" {
		return formDefined
	}

	schemaDefined := string(rf.Tag.Get("schema"))
	if schemaDefined != "" && schemaDefined != "-" {
		return schemaDefined
	}

	return field
}
