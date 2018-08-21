package env

import (
	"fmt"
	"strconv"
	"time"

	"github.com/gobuffalo/envy"
)

// MongoConnectionString returns Athens Mongo Storage connection string defined by ATHENS_MONGO_CONNECTION_STRING
func MongoConnectionString() (string, error) {
	env, err := envy.MustGet("ATHENS_MONGO_CONNECTION_STRING")
	if err != nil {
		return "", fmt.Errorf("missing mongo connection string: %s", err)
	}

	return env, nil
}

// MongoCertPath returns Athens Mongo Storage cert path string defined by ATHENS_MONGO_CERT_PATH
func MongoCertPath() string {
	env := envy.Get("ATHENS_MONGO_CERT_PATH", "")
	return env
}

// MongoHost returns Athens Mongo host defined by MONGO_HOST
func MongoHost() (string, error) {
	env, err := envy.MustGet("MONGO_HOST")
	if err != nil {
		return "", fmt.Errorf("missing mongo host: %s", err)
	}

	return env, nil
}

// MongoPort returns Athens Mongo port defined by MONGO_PORT
func MongoPort() (string, error) {
	env, err := envy.MustGet("MONGO_PORT")
	if err != nil {
		return "", fmt.Errorf("missing mongo port: %s", err)
	}

	return env, nil
}

// MongoUser returns Athens Mongo Storage user defined by MONGO_USER
func MongoUser() (string, error) {
	env, err := envy.MustGet("MONGO_USER")
	if err != nil {
		return "", fmt.Errorf("missing mongo user: %s", err)
	}

	return env, nil
}

// MongoPassword returns Athens Mongo Storage user password defined by MONGO_PASSWORD
func MongoPassword() (string, error) {
	env, err := envy.MustGet("MONGO_PASSWORD")
	if err != nil {
		return "", fmt.Errorf("missing mongo user password: %s", err)
	}

	return env, nil
}

// MongoConnectionTimeoutSecWithDefault returns Athens Mongo Storage connection timeout defined by MONGO_CONN_TIMEOUT_SEC.
// Values are in seconds.
func MongoConnectionTimeoutSecWithDefault(defTimeout int) time.Duration {
	timeoutConf := envy.Get("MONGO_CONN_TIMEOUT_SEC", strconv.Itoa(defTimeout))
	timeout, err := strconv.ParseInt(timeoutConf, 10, 32)
	if err != nil {
		return time.Duration(defTimeout) * time.Second
	}
	return time.Duration(timeout) * time.Second
}

// MongoSSLWithDefault returns Athens Mongo Storage SSL flag defined by MONGO_SSL.
// Defines whether or not SSL should be used.
func MongoSSLWithDefault(value string) string {
	return envy.Get("MONGO_SSL", value)
}
