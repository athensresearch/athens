package grift

import (
	"context"
	"sync"
)

// Context used to pass information between grifts
type Context struct {
	context.Context
	Name    string
	Args    []string
	Verbose bool
	data    map[interface{}]interface{}
	moot    *sync.Mutex
}

// Value returns a value from the context for the given key
func (c *Context) Value(key interface{}) interface{} {
	if s, ok := key.(string); ok {
		if v, ok := c.data[s]; ok {
			return v
		}
	}
	return c.Context.Value(key)
}

// Set a piece of data onto the Context.
func (c *Context) Set(key string, val interface{}) {
	c.moot.Lock()
	defer c.moot.Unlock()
	c.data[key] = val
}

// NewContext builds and returns a new default Context.
func NewContext(name string) *Context {
	return NewContextWithContext(name, context.Background())
}

// NewContextWithContext builds and returns a new default Context given an existing context
func NewContextWithContext(name string, ctx context.Context) *Context {
	return &Context{
		Context: ctx,
		Name:    name,
		Args:    []string{},
		data:    map[interface{}]interface{}{},
		moot:    &sync.Mutex{},
	}
}
