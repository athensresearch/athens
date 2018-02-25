package form

import (
	"bytes"
	"html/template"
)

// SelectOption describes a HTML <select> tag <option> meta data.
type SelectOption struct {
	Value    interface{}
	Label    interface{}
	Selected bool
}

func (s SelectOption) String() string {
	v := template.HTMLEscaper(s.Value)
	l := template.HTMLEscaper(s.Label)
	bb := &bytes.Buffer{}
	bb.WriteString(`<option value="`)
	bb.WriteString(v)
	bb.WriteString(`"`)
	if s.Selected {
		bb.WriteString(` selected`)
	}
	bb.WriteString(`>`)
	bb.WriteString(l)
	bb.WriteString("</option>")
	return bb.String()
}

// SelectOptions is a slice of SelectOption
type SelectOptions []SelectOption
