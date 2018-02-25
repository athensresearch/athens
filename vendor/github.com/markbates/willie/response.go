package willie

import "net/http/httptest"

type Response struct {
	*httptest.ResponseRecorder
}

func (r *Response) Location() string {
	return r.Header().Get("Location")
}

func (r *Response) CloseNotify() <-chan bool {
	return make(chan bool)
}
