package form

import "github.com/gobuffalo/tags"

//FileTag builds an input[type=file] from the options passed, it also sets the form enctype to be "multipart/form-data"
func (f Form) FileTag(opts tags.Options) *tags.Tag {
	if opts["type"] == nil {
		opts["type"] = "file"
	}
	if opts["type"] == "file" {
		f.Options["enctype"] = "multipart/form-data"
	}
	return f.InputTag(opts)
}
