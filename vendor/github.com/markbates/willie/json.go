package willie

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"

	"github.com/markbates/hmax"
)

type JSON struct {
	URL     string
	Willie  *Willie
	Headers map[string]string
}

type JSONResponse struct {
	*Response
}

func (r *JSONResponse) Bind(x interface{}) {
	json.NewDecoder(r.Body).Decode(&x)
}

func (r *JSON) Get() *JSONResponse {
	req, _ := http.NewRequest("GET", r.URL, nil)
	return r.perform(req)
}

func (r *JSON) Delete() *JSONResponse {
	req, _ := http.NewRequest("DELETE", r.URL, nil)
	return r.perform(req)
}

func (r *JSON) Post(body interface{}) *JSONResponse {
	b, _ := json.Marshal(body)
	req, _ := http.NewRequest("POST", r.URL, bytes.NewReader(b))
	return r.perform(req)
}

func (r *JSON) Put(body interface{}) *JSONResponse {
	b, _ := json.Marshal(body)
	req, _ := http.NewRequest("PUT", r.URL, bytes.NewReader(b))
	return r.perform(req)
}

func (r *JSON) Patch(body interface{}) *JSONResponse {
	b, _ := json.Marshal(body)
	req, _ := http.NewRequest("PATCH", r.URL, bytes.NewReader(b))
	return r.perform(req)
}

func (r *JSON) perform(req *http.Request) *JSONResponse {
	if r.Willie.HmaxSecret != "" {
		hmax.SignRequest(req, []byte(r.Willie.HmaxSecret))
	}
	res := &JSONResponse{&Response{httptest.NewRecorder()}}
	for key, value := range r.Headers {
		req.Header.Set(key, value)
	}
	req.Header.Set("Cookie", r.Willie.Cookies)
	r.Willie.ServeHTTP(res, req)
	r.Willie.Cookies = res.Header().Get("Set-Cookie")
	return res
}
