package makr

import (
	"encoding/json"
	"html/template"
	"strings"

	"github.com/markbates/inflect"
)

// Helpers that are automatically injected into templates.
var Helpers = map[string]interface{}{
	"js_escape":   template.JSEscapeString,
	"html_escape": template.HTMLEscapeString,
	"json":        toJSON,
	"upcase":      strings.ToUpper,
	"downcase":    strings.ToLower,
}

func init() {
	for k, v := range inflect.Helpers {
		Helpers[k] = v
	}
}

// ToJSON converts an interface into a string.
func toJSON(v interface{}) string {
	b, err := json.Marshal(v)
	if err != nil {
		return err.Error()
	}
	return string(b)
}
