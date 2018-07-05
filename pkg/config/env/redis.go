package env

import "github.com/gobuffalo/envy"

// RedisQueuePortWithDefault returns Redis queue port used by workers defined by ATHENS_REDIS_QUEUE_PORT.
// Standard port is 6379
func RedisQueuePortWithDefault(value string) string {
	return envy.Get("ATHENS_REDIS_QUEUE_PORT", value)
}

// OlympusRedisQueuePortWithDefault returns Redis queue port used by workers defined by ATHENS_REDIS_QUEUE_PORT.
// Standard port is 6379
func OlympusRedisQueuePortWithDefault(value string) string {
	return envy.Get("OLYMPUS_REDIS_QUEUE_PORT", value)
}
