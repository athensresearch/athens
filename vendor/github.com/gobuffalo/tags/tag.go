package tags

import (
	"bytes"
	"database/sql/driver"
	"fmt"
	"html/template"
	"reflect"
	"strings"
	"time"
)

// void tags https://www.w3.org/TR/html5/syntax.html#void-elements
const voidTags = " area base br col embed hr img input keygen link meta param source track wbr "

// Body is a Tag inner content.
type Body interface{}

// BeforeTag is content placed right before the Tag
type BeforeTag interface{}

// AfterTag is content placed right after the Tag
type AfterTag interface{}

// Tag describes a HTML tag meta data.
type Tag struct {
	Name      string
	Options   Options
	Selected  bool
	Checked   bool
	BeforeTag []BeforeTag
	Body      []Body
	AfterTag  []AfterTag
}

// Append adds new Body part(s) after the current Tag inner contents.
func (t *Tag) Append(b ...Body) {
	t.Body = append(t.Body, b...)
}

// Prepend adds new Body part(s) before the current Tag inner contents.
func (t *Tag) Prepend(b ...Body) {
	t.Body = append(b, t.Body...)
}

type interfacer interface {
	Interface() interface{}
}

type htmler interface {
	HTML() template.HTML
}

func parseTagEmbed(b interface{}) string {
	switch tb := b.(type) {
	case htmler:
		return fmt.Sprint(tb.HTML())
	case fmt.Stringer:
		return tb.String()
	case interfacer:
		val := tb.Interface()
		if tb.Interface() == nil {
			val = ""
		}

		return fmt.Sprint(val)
	default:
		return fmt.Sprint(tb)
	}
}

func (t Tag) String() string {
	bb := &bytes.Buffer{}

	for _, bt := range t.BeforeTag {
		bb.WriteString(parseTagEmbed(bt))
	}

	bb.WriteString("<")
	bb.WriteString(t.Name)
	if len(t.Options) > 0 {
		bb.WriteString(" ")
		bb.WriteString(t.Options.String())
	}
	if t.Selected {
		bb.WriteString(" selected")
	}
	if t.Checked {
		bb.WriteString(" checked")
	}
	if len(t.Body) > 0 {
		bb.WriteString(">")

		for _, b := range t.Body {
			bb.WriteString(parseTagEmbed(b))
		}

		bb.WriteString("</")
		bb.WriteString(t.Name)
		bb.WriteString(">")

		for _, at := range t.AfterTag {
			bb.WriteString(parseTagEmbed(at))
		}

		return bb.String()
	}
	if !strings.Contains(voidTags, " "+t.Name+" ") {
		bb.WriteString("></")
		bb.WriteString(t.Name)
		bb.WriteString(">")

		for _, at := range t.AfterTag {
			bb.WriteString(parseTagEmbed(at))
		}

		return bb.String()
	}
	bb.WriteString(" />")

	for _, at := range t.AfterTag {
		bb.WriteString(parseTagEmbed(at))
	}

	return bb.String()
}

// HTML gets the Tag string representation as a HTML template.
func (t Tag) HTML() template.HTML {
	return template.HTML(t.String())
}

// New creates a new Tag with given name and options.
func New(name string, opts Options) *Tag {
	tag := &Tag{
		Name:    name,
		Options: opts,
	}
	if tag.Options["body"] != nil {
		tag.Body = []Body{tag.Options["body"]}
		delete(tag.Options, "body")
	}
	if tag.Options["before_tag"] != nil {
		tag.BeforeTag = []BeforeTag{tag.Options["before_tag"]}
		delete(tag.Options, "before_tag")
	}

	if tag.Options["after_tag"] != nil {
		tag.AfterTag = []AfterTag{tag.Options["after_tag"]}
		delete(tag.Options, "after_tag")
	}

	if tag.Options["value"] != nil {
		val := tag.Options["value"]

		switch val.(type) {
		case time.Time:
			format := tag.Options["format"]
			if format == nil || format.(string) == "" {
				format = "2006-01-02"
			}

			delete(tag.Options, "format")
			tag.Options["value"] = val.(time.Time).Format(format.(string))
		default:

			r := reflect.ValueOf(val)
			if r.IsValid() == false {
				tag.Options["value"] = ""
			}

			i := r.Interface()
			if dv, ok := i.(driver.Valuer); ok {
				value, _ := dv.Value()

				if value == nil {
					tag.Options["value"] = ""
				}

				tag.Options["value"] = fmt.Sprintf("%v", value)
			}

		}
	}

	if tag.Options["selected"] != nil {
		tag.Selected = template.HTMLEscaper(opts["value"]) == template.HTMLEscaper(opts["selected"])
		delete(tag.Options, "selected")
	}

	return tag
}
