package tags

//StylesheetTag builds the <link> tag to load styles on the page.
func StylesheetTag(opts Options) *Tag {
	if opts["rel"] == nil {
		opts["rel"] = "stylesheet"
	}

	return New("link", opts)
}

//JavascriptTag builds JS tags based in passed options
func JavascriptTag(opts Options) *Tag {
	if opts["src"] != nil {
		delete(opts, "body")
	}

	return New("script", opts)
}
