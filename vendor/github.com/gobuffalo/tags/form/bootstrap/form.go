package bootstrap

import (
	"github.com/gobuffalo/tags"
	"github.com/gobuffalo/tags/form"
)

//Form is the bootstrap version of Form
type Form struct {
	*form.Form
}

//New creates a bootstrap Form from passed options
func New(opts tags.Options) *Form {
	return &Form{form.New(opts)}
}

//CheckboxTag builds a bootstrap checkbox with passed options
func (f Form) CheckboxTag(opts tags.Options) *tags.Tag {
	return divWrapper(opts, func(o tags.Options) tags.Body {
		return f.Form.CheckboxTag(o)
	})
}

//InputTag builds a bootstrap input[type=text] with passed options
func (f Form) InputTag(opts tags.Options) *tags.Tag {
	return divWrapper(opts, func(o tags.Options) tags.Body {
		return f.Form.InputTag(o)
	})
}

//FileTag builds a bootstrap input[type=file] with passed options
func (f Form) FileTag(opts tags.Options) *tags.Tag {
	return divWrapper(opts, func(o tags.Options) tags.Body {
		return f.Form.FileTag(o)
	})
}

//RadioButton builds a bootstrap input[type=radio] with passed options
func (f Form) RadioButton(opts tags.Options) *tags.Tag {
	return f.RadioButtonTag(opts)
}

//RadioButtonTag builds a bootstrap input[type=radio] with passed options
func (f Form) RadioButtonTag(opts tags.Options) *tags.Tag {
	return divWrapper(opts, func(o tags.Options) tags.Body {
		return f.Form.RadioButtonTag(o)
	})
}

//SelectTag builds a bootstrap select with passed options
func (f Form) SelectTag(opts tags.Options) *tags.Tag {
	return divWrapper(opts, func(o tags.Options) tags.Body {
		return f.Form.SelectTag(o)
	})
}

//TextArea builds a bootstrap textarea with passed options
func (f Form) TextArea(opts tags.Options) *tags.Tag {
	return f.TextAreaTag(opts)
}

//TextAreaTag builds a bootstrap textarea with passed options
func (f Form) TextAreaTag(opts tags.Options) *tags.Tag {
	return divWrapper(opts, func(o tags.Options) tags.Body {
		return f.Form.TextAreaTag(o)
	})
}

//HiddenTag adds a hidden input to the form
func (f Form) HiddenTag(opts tags.Options) *tags.Tag {
	return f.Form.HiddenTag(opts)
}
