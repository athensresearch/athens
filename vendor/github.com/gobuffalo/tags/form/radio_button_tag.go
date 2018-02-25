package form

import (
	"fmt"
	"html/template"
	"strings"

	"github.com/gobuffalo/tags"
)

//RadioButton creates a radio button for a form with the passed options
func (f Form) RadioButton(opts tags.Options) *tags.Tag {
	return f.RadioButtonTag(opts)
}

//RadioButtonTag creates a radio button for a form with the passed options
func (f Form) RadioButtonTag(opts tags.Options) *tags.Tag {
	opts["type"] = "radio"

	var label string
	if opts["label"] != nil {
		label = fmt.Sprint(opts["label"])
		delete(opts, "label")
	}
	var ID string
	if opts["id"] != nil {
		ID = fmt.Sprint(opts["id"])
	}

	value := opts["value"]
	checked := opts["checked"]
	delete(opts, "checked")

	if opts["tag_only"] == true {
		ct := f.InputTag(opts)
		ct.Checked = template.HTMLEscaper(value) == template.HTMLEscaper(checked)
		return ct
	}

	ct := f.InputTag(opts)
	ct.Checked = template.HTMLEscaper(value) == template.HTMLEscaper(checked)
	labelOptions := tags.Options{
		"body": strings.Join([]string{ct.String(), label}, " "),
	}
	// If the ID is provided, give it to the label's for attribute
	if ID != "" {
		labelOptions["for"] = ID
	}
	tag := tags.New("label", labelOptions)
	return tag
}
