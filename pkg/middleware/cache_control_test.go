package middleware

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gobuffalo/buffalo"
)

func TestCacheControl(t *testing.T) {
	h := func(c buffalo.Context) error { return c.Render(200, nil) }
	a := buffalo.New(buffalo.Options{})
	a.GET("/test", h)

	expected := "private, no-store"
	a.Use(CacheControl(expected))

	w := httptest.NewRecorder()
	r, _ := http.NewRequest("GET", "/test", nil)
	a.ServeHTTP(w, r)

	given := w.Result().Header.Get("Cache-Control")
	if given != expected {
		t.Fatalf("expected cache-control header to be %v but got %v", expected, given)
	}
}
