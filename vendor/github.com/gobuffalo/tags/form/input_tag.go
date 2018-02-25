package form

import "github.com/gobuffalo/tags"

//InputTag generates an input tag with passed options, by default will be type=text.
func (f Form) InputTag(opts tags.Options) *tags.Tag {
	if opts["type"] == "hidden" {
		return f.HiddenTag(opts)
	}

	if opts["type"] == nil {
		opts["type"] = "text"
	}

	if opts["type"] == "file" {
		f.Options["enctype"] = "multipart/form-data"
	}

	delete(opts, "tag_only")
	return tags.New("input", opts)
}

//DateTimeTag generates a tag with type datetime-local and adds default format to be 2006-01-02T03:04
func (f Form) DateTimeTag(opts tags.Options) *tags.Tag {
	if opts["type"] == nil {
		opts["type"] = "datetime-local"
	}
	if opts["format"] == nil {
		opts["format"] = "2006-01-02T03:04"
	}

	delete(opts, "tag_only")
	return tags.New("input", opts)
}

//HiddenTag generates and input tag with type hidden
func (f Form) HiddenTag(opts tags.Options) *tags.Tag {
	opts["type"] = "hidden"
	return tags.New("input", opts)
}
