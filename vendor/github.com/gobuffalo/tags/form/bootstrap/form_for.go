package bootstrap

import (
	"fmt"
	"strings"

	"github.com/gobuffalo/tags"
	"github.com/gobuffalo/tags/form"
	"github.com/gobuffalo/validate/validators"
)

//FormFor is the FormFor version for bootstrap
type FormFor struct {
	*form.FormFor
}

//CheckboxTag adds a checkbox to a form wrapped with a form-control and a label
func (f FormFor) CheckboxTag(field string, opts tags.Options) *tags.Tag {

	label := field
	if opts["label"] != nil {
		label = fmt.Sprint(opts["label"])
	}
	hl := opts["hide_label"]
	delete(opts, "label")

	fieldKey := validators.GenerateKey(field)
	if err := f.Errors.Get(fieldKey); err != nil {
		opts["errors"] = err
	}

	return divWrapper(opts, func(o tags.Options) tags.Body {
		if o["class"] != nil {
			cls := strings.Split(o["class"].(string), " ")
			ncls := make([]string, 0, len(cls))
			for _, c := range cls {
				if c != "form-control" {
					ncls = append(ncls, c)
				}
			}
			o["class"] = strings.Join(ncls, " ")
		}
		if label != "" {
			o["label"] = label
		}
		if hl != nil {
			o["hide_label"] = hl
		}
		return f.FormFor.CheckboxTag(field, o)
	})
}

//InputTag builds an input[type=text] by default wrapped with a form-control and a label
func (f FormFor) InputTag(field string, opts tags.Options) *tags.Tag {
	opts = f.buildOptions(field, opts)
	if opts["type"] == "hidden" {
		return f.HiddenTag(field, opts)
	}

	return divWrapper(opts, func(o tags.Options) tags.Body {
		return f.FormFor.InputTag(field, opts)
	})
}

//HiddenTag adds an input[type=hidden] to the formFor
func (f FormFor) HiddenTag(field string, opts tags.Options) *tags.Tag {
	opts = f.buildOptions(field, opts)
	return f.FormFor.HiddenTag(field, opts)
}

//FileTag adds a bootstrap input[type=file] wrapped with a form-control and a label
func (f FormFor) FileTag(field string, opts tags.Options) *tags.Tag {
	opts = f.buildOptions(field, opts)
	return divWrapper(opts, func(o tags.Options) tags.Body {
		return f.FormFor.FileTag(field, opts)
	})
}

//RadioButton adds a bootstrap input[type=radio] wrapped with a form-control and a label
func (f FormFor) RadioButton(field string, opts tags.Options) *tags.Tag {
	return f.RadioButtonTag(field, opts)
}

//RadioButtonTag adds a bootstrap input[type=radio] wrapped with a form-control and a label
func (f FormFor) RadioButtonTag(field string, opts tags.Options) *tags.Tag {
	opts = f.buildOptions(field, opts)
	return divWrapper(opts, func(o tags.Options) tags.Body {
		return f.FormFor.RadioButtonTag(field, opts)
	})
}

//SelectTag adds a bootstrap select tag wrapped with a form-control and a label
func (f FormFor) SelectTag(field string, opts tags.Options) *tags.Tag {
	opts = f.buildOptions(field, opts)
	return divWrapper(opts, func(o tags.Options) tags.Body {
		return f.FormFor.SelectTag(field, opts)
	})
}

//TextArea adds a bootstrap textarea tag wrapped with a form-control and a label
func (f FormFor) TextArea(field string, opts tags.Options) *tags.Tag {
	return f.TextAreaTag(field, opts)
}

//TextAreaTag adds a bootstrap textarea tag wrapped with a form-control and a label
func (f FormFor) TextAreaTag(field string, opts tags.Options) *tags.Tag {
	opts = f.buildOptions(field, opts)
	return divWrapper(opts, func(o tags.Options) tags.Body {
		return f.FormFor.TextAreaTag(field, opts)
	})
}

//SubmitTag returns a tag for input type submit without wrapping
func (f FormFor) SubmitTag(value string, opts tags.Options) *tags.Tag {
	return f.FormFor.SubmitTag(value, opts)
}

//NewFormFor builds a form for a passed model
func NewFormFor(model interface{}, opts tags.Options) *FormFor {
	return &FormFor{form.NewFormFor(model, opts)}
}

func (f FormFor) buildOptions(field string, opts tags.Options) tags.Options {
	opts["tags-field"] = field
	fieldName := validators.GenerateKey(field)
	if err := f.Errors.Get(fieldName); err != nil {
		opts["errors"] = err
	}

	return opts
}
