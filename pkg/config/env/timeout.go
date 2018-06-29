package env

import (
	"strconv"
	"time"

	"github.com/gobuffalo/envy"
)

// Timeout is timeout for external network calls
func Timeout() time.Duration {
	t := envy.Get("ATHENS_TIMEOUT", "300")
	timeout, err := strconv.Atoi(t)
	if err != nil || timeout <= 0 {
		return 300 * time.Second
	}

	return time.Second * time.Duration(timeout)
}
