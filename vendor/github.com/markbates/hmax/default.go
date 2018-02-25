package hmax

import (
	"crypto/sha256"
	"net/http"
)

var defaultHMAX = HMAX{Header: "X-Signature", Hasher: sha256.New}

func Sign(secret, message []byte) string {
	defaultHMAX.Secret = secret
	return defaultHMAX.Sign(message)
}

func Verify(signature string, secret, message []byte) bool {
	defaultHMAX.Secret = secret
	b, _ := defaultHMAX.Verify(signature, message)
	return b
}

func SignRequest(req *http.Request, secret []byte) error {
	defaultHMAX.Secret = secret
	return defaultHMAX.SignRequest(req)
}

func VerifyRequest(req *http.Request, secret []byte) (bool, error) {
	defaultHMAX.Secret = secret
	return defaultHMAX.VerifyRequest(req)
}
