package form

import (
	"github.com/gobuffalo/tags"
)

//TextArea creates a textarea for a form with passed options
func (f Form) TextArea(opts tags.Options) *tags.Tag {
	return f.TextAreaTag(opts)
}

//TextAreaTag creates a textarea for a form with passed options
func (f Form) TextAreaTag(opts tags.Options) *tags.Tag {
	if opts["value"] != nil {
		opts["body"] = opts["value"]
		delete(opts, "value")
	}

	delete(opts, "tag_only")
	return tags.New("textarea", opts)
}
