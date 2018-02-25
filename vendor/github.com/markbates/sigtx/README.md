# sigtx [![Build Status](https://travis-ci.org/markbates/sigtx.svg?branch=master)](https://travis-ci.org/markbates/sigtx)

This package provides an implementation proposal for this Go proposal: [https://github.com/golang/go/issues/21521](https://github.com/golang/go/issues/21521).

## Usage

```go
package main

import (
	"context"
	"fmt"
	"os"

	"github.com/markbates/sigtx"
)

func main() {
	ctx, cancel := sigtx.WithCancel(context.Background(), os.Interrupt)
	defer cancel()
	select {
	case <-ctx.Done():
		fmt.Println("thanks for stopping me")
	}
}
```
