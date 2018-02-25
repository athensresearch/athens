package httpx

import (
	"net/http"
	"strings"

	"github.com/markbates/going/defaults"
)

func ContentType(req *http.Request) string {
	ct := defaults.String(req.Header.Get("Content-Type"), req.Header.Get("Accept"))
	var cts []string
	if strings.Contains(ct, ",") {
		cts = strings.Split(ct, ",")
	} else {
		cts = strings.Split(ct, ";")
	}
	for _, c := range cts {
		c = strings.TrimSpace(c)
		if strings.HasPrefix(c, "*/*") {
			continue
		}
		return strings.ToLower(c)
	}
	return ct
}
