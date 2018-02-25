package willie

import (
	"bytes"
	"encoding/xml"
	"net/http"
	"net/http/httptest"

	"github.com/markbates/hmax"
)

type XML struct {
	URL     string
	Willie  *Willie
	Headers map[string]string
}

type XMLResponse struct {
	*Response
}

func (r *XMLResponse) Bind(x interface{}) {
	xml.NewDecoder(r.Body).Decode(&x)
}

func (r *XML) Get() *XMLResponse {
	req, _ := http.NewRequest("GET", r.URL, nil)
	return r.perform(req)
}

func (r *XML) Delete() *XMLResponse {
	req, _ := http.NewRequest("DELETE", r.URL, nil)
	return r.perform(req)
}

func (r *XML) Post(body interface{}) *XMLResponse {
	b, _ := xml.Marshal(body)
	req, _ := http.NewRequest("POST", r.URL, bytes.NewReader(b))
	return r.perform(req)
}

func (r *XML) Put(body interface{}) *XMLResponse {
	b, _ := xml.Marshal(body)
	req, _ := http.NewRequest("PUT", r.URL, bytes.NewReader(b))
	return r.perform(req)
}

func (r *XML) Patch(body interface{}) *XMLResponse {
	b, _ := xml.Marshal(body)
	req, _ := http.NewRequest("PATCH", r.URL, bytes.NewReader(b))
	return r.perform(req)
}

func (r *XML) perform(req *http.Request) *XMLResponse {
	if r.Willie.HmaxSecret != "" {
		hmax.SignRequest(req, []byte(r.Willie.HmaxSecret))
	}
	res := &XMLResponse{&Response{httptest.NewRecorder()}}
	for key, value := range r.Headers {
		req.Header.Set(key, value)
	}
	req.Header.Set("Cookie", r.Willie.Cookies)
	r.Willie.ServeHTTP(res, req)
	r.Willie.Cookies = res.Header().Get("Set-Cookie")
	return res
}
