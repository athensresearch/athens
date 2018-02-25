package form

import (
	"html/template"
	"reflect"

	"github.com/gobuffalo/tags"
)

// SelectTag describes a HTML <select> tag meta data.
type SelectTag struct {
	*tags.Tag
	SelectedValue      interface{}
	selectedValueCache map[interface{}]struct{}
	SelectOptions      SelectOptions
}

func (s SelectTag) String() string {
	for _, x := range s.SelectOptions {
		if _, ok := s.selectedValueCache[template.HTMLEscaper(x.Value)]; ok {
			x.Selected = true
		}
		s.Append(x.String())
	}
	return s.Tag.String()
}

// HTML gives the HTML template representation for the select tag.
func (s SelectTag) HTML() template.HTML {
	return template.HTML(s.String())
}

// NewSelectTag constructs a new `<select>` tag.
func NewSelectTag(opts tags.Options) *SelectTag {
	so := parseSelectOptions(opts)
	selected := opts["value"]
	delete(opts, "value")

	// Transform selected value(s) into an empty map with values as keys
	// (faster lookup than slice / array)
	selectedMap := make(map[interface{}]struct{})

	multiple, ok := opts["multiple"].(bool)
	if multiple && ok {
		// Set nil to use the empty attribute notation
		opts["multiple"] = nil

		rv := reflect.ValueOf(selected)
		if rv.Kind() == reflect.Ptr {
			rv = rv.Elem()
		}
		if rv.Kind() == reflect.Array || rv.Kind() == reflect.Slice {
			for i := 0; i < rv.Len(); i++ {
				x := rv.Index(i).Interface()
				if s, ok := x.(Selectable); ok {
					// Use Selectable value as the selected value
					x = s.SelectValue()
				}
				selectedMap[template.HTMLEscaper(x)] = struct{}{}
			}
		} else {
			// Set unique value as a map key
			selectedMap[template.HTMLEscaper(selected)] = struct{}{}
		}
	} else {
		if s, ok := selected.(Selectable); ok {
			selected = s.SelectValue()
		}
		// Set unique value as a map key
		selectedMap[template.HTMLEscaper(selected)] = struct{}{}
	}

	st := &SelectTag{
		Tag:                tags.New("select", opts),
		SelectOptions:      so,
		SelectedValue:      selected,
		selectedValueCache: selectedMap,
	}
	return st
}

// SelectTag constructs a new `<select>` tag from a form.
func (f Form) SelectTag(opts tags.Options) *SelectTag {
	return NewSelectTag(opts)
}

func parseSelectOptions(opts tags.Options) SelectOptions {
	if opts["options"] == nil {
		return SelectOptions{}
	}

	allowBlank := opts["allow_blank"]
	delete(opts, "allow_blank")

	sopts := opts["options"]
	delete(opts, "options")

	so := SelectOptions{}
	if aw, ok := allowBlank.(bool); ok && aw {
		so = append(so, SelectOption{
			Value: "",
			Label: "",
		})
	}

	if x, ok := sopts.(SelectOptions); ok {
		x = append(so, x...) // prepend blank SelectOption if present
		return x
	}

	rv := reflect.ValueOf(sopts)
	if rv.Kind() == reflect.Ptr {
		rv = rv.Elem()
	}

	switch rv.Kind() {
	case reflect.Slice, reflect.Array:
		selectableType := reflect.TypeOf((*Selectable)(nil)).Elem()

		for i := 0; i < rv.Len(); i++ {
			x := rv.Index(i).Interface()

			if rv.Index(i).Type().Implements(selectableType) {
				so = append(so, SelectOption{Value: x.(Selectable).SelectValue(), Label: x.(Selectable).SelectLabel()})
				continue
			}

			so = append(so, SelectOption{Value: x, Label: x})
		}
	case reflect.Map:
		keys := rv.MapKeys()
		for i := 0; i < len(keys); i++ {
			k := keys[i]
			so = append(so, SelectOption{
				Value: rv.MapIndex(k).Interface(),
				Label: k.Interface(),
			})
		}
	}
	return so
}
