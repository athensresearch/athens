package tags

import (
	"fmt"
	"html/template"
	"sort"
	"strings"
)

// Options is a map used to configure tags
type Options map[string]interface{}

func (o Options) String() string {
	var out = make([]string, 0, len(o))
	var tmp = make([]string, 2)
	for k, v := range o {
		tmp[0] = template.HTMLEscaper(k)
		if v != nil {
			tmp[1] = fmt.Sprintf("\"%s\"", template.HTMLEscaper(v))
			out = append(out, strings.Join(tmp, "="))
		} else {
			// nil attribute value is interpreted as empty attribute notation
			// https://www.w3.org/TR/html5/syntax.html#elements-attributes
			out = append(out, tmp[0])
		}
	}
	sort.Strings(out)
	return strings.Join(out, " ")
}
