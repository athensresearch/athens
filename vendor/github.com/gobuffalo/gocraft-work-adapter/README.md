# gocraft/work Adapter for Buffalo

This package implements the `github.com/gobuffalo/buffalo/worker.Worker` interface using the [`github.com/gocraft/work`](https://github.com/gocraft/work) package.

## Setup

```go
import "github.com/gobuffalo/gocraft-work-adapter"
import "github.com/garyburd/redigo/redis"

// ...

buffalo.New(buffalo.Options{
  // ...
  Worker: gwa.New(gwa.Options{
    Pool: &redis.Pool{
      MaxActive: 5,
      MaxIdle:   5,
      Wait:      true,
      Dial: func() (redis.Conn, error) {
        return redis.Dial("tcp", ":6379")
      },
    },
    Name:           "myapp",
    MaxConcurrency: 25,
  }),
  // ...
})
```
