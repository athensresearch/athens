package hmax

import (
	"bytes"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/base64"
	"fmt"
	"hash"
	"io/ioutil"
	"net/http"
)

type HMAX struct {
	Header string
	Hasher func() hash.Hash
	Secret []byte
}

func New(h string, s []byte) HMAX {
	return HMAX{
		Header: h,
		Secret: s,
		Hasher: sha256.New,
	}
}

func (h HMAX) Sign(message []byte) string {
	hm := hmac.New(h.Hasher, h.Secret)
	hm.Write(message)
	return base64.StdEncoding.EncodeToString(hm.Sum(nil))
}

func (h HMAX) Verify(signature string, message []byte) (bool, error) {
	s := h.Sign(message)
	a := []byte(s)
	b := []byte(signature)
	eq := hmac.Equal(a, b)
	if !eq {
		return false, fmt.Errorf("signatures did not match: %s %s", s, signature)
	}
	return true, nil
}

func (h HMAX) SignRequest(req *http.Request) error {
	b, err := h.readBody(req)
	if err != nil {
		return err
	}

	s := h.Sign(b)
	req.Header.Set(h.Header, s)
	return nil
}

func (h HMAX) VerifyRequest(req *http.Request) (bool, error) {
	b, err := h.readBody(req)
	if err != nil {
		return false, err
	}
	return h.Verify(req.Header.Get(h.Header), b)
}

func (h HMAX) readBody(req *http.Request) ([]byte, error) {
	var b []byte
	var err error
	if req.Body != nil {
		b, err = ioutil.ReadAll(req.Body)
	}

	// Restore the io.ReadCloser to its original state
	req.Body = ioutil.NopCloser(bytes.NewBuffer(b))
	return b, err
}
