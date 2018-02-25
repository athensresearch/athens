package web

import (
	"html/template"
	"io/ioutil"
	"net/http"

	"github.com/markbates/refresh/refresh"
)

var id = refresh.ID()
var lpath = refresh.ErrorLogPath()
var tmpl *template.Template

func init() {
	tmpl, _ = template.New("template").Parse(html)
}

func ErrorChecker(h http.Handler) http.Handler {
	return http.HandlerFunc(func(res http.ResponseWriter, req *http.Request) {
		ee, err := ioutil.ReadFile(lpath)
		if err != nil || ee == nil {
			h.ServeHTTP(res, req)
			return
		}
		res.WriteHeader(500)
		err = tmpl.Execute(res, string(ee))
	})
}

var html = `
<html>
<head>
	<title>Refresh Build Error!</title>
	<style>
		body {
			margin-top: 20px;
			font-family: Helvetica;
		}
		h1 {
			text-align: center;
		}
		pre {
			border: 1px #B22222 solid;
			background-color: #FFB6C1;
			padding: 5px;
			font-size: 32px;
		}
	</style>
</head>

<h1>Oops!! There was a build error!</h1>

<pre><code>{{.}}</code></pre>

</html>
`
