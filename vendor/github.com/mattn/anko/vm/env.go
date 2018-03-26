package vm

import (
	"fmt"
	"reflect"
	"strings"
	"sync"

	"github.com/mattn/anko/parser"
)

// EnvResolver provides an interface for extrenal values and types
type EnvResolver interface {
	Get(string) (reflect.Value, error)
	Type(string) (reflect.Type, error)
}

// Env provides interface to run VM. This mean function scope and blocked-scope.
// If stack goes to blocked-scope, it will make new Env.
type Env struct {
	name      string
	env       map[string]reflect.Value
	typ       map[string]reflect.Type
	parent    *Env
	interrupt *bool
	external  EnvResolver
	sync.RWMutex
}

var basicTypes = []struct {
	name string
	typ  reflect.Type
}{
	{name: "interface", typ: reflect.ValueOf([]interface{}{int64(1)}).Index(0).Type()},
	{name: "bool", typ: reflect.TypeOf(true)},
	{name: "string", typ: reflect.TypeOf("a")},
	{name: "int", typ: reflect.TypeOf(int(1))},
	{name: "int32", typ: reflect.TypeOf(int32(1))},
	{name: "int64", typ: reflect.TypeOf(int64(1))},
	{name: "uint", typ: reflect.TypeOf(uint(1))},
	{name: "uint32", typ: reflect.TypeOf(uint32(1))},
	{name: "uint64", typ: reflect.TypeOf(uint64(1))},
	{name: "byte", typ: reflect.TypeOf(byte(1))},
	{name: "rune", typ: reflect.TypeOf('a')},
	{name: "float32", typ: reflect.TypeOf(float32(1))},
	{name: "float64", typ: reflect.TypeOf(float64(1))},
}

func newBasicTypes() map[string]reflect.Type {
	types := make(map[string]reflect.Type, len(basicTypes))
	for i := 0; i < len(basicTypes); i++ {
		types[basicTypes[i].name] = basicTypes[i].typ
	}
	return types
}

// NewEnv creates new global scope.
func NewEnv() *Env {
	b := false

	return &Env{
		env:       make(map[string]reflect.Value),
		typ:       newBasicTypes(),
		parent:    nil,
		interrupt: &b,
	}
}

// NewEnv creates new child scope.
func (e *Env) NewEnv() *Env {
	return &Env{
		env:       make(map[string]reflect.Value),
		typ:       make(map[string]reflect.Type),
		parent:    e,
		name:      e.name,
		interrupt: e.interrupt,
	}
}

// NewPackage creates a new env with a name
func NewPackage(n string) *Env {
	b := false

	return &Env{
		env:       make(map[string]reflect.Value),
		typ:       make(map[string]reflect.Type),
		parent:    nil,
		name:      n,
		interrupt: &b,
	}
}

// NewPackage creates a new env with a name under the parent env
func (e *Env) NewPackage(n string) *Env {
	return &Env{
		env:       make(map[string]reflect.Value),
		typ:       make(map[string]reflect.Type),
		parent:    e,
		name:      n,
		interrupt: e.interrupt,
	}
}

// AddPackage creates a new env with a name that has methods and types in it. Created under the parent env
func (e *Env) AddPackage(name string, methods map[string]interface{}, types map[string]interface{}) (*Env, error) {
	if strings.Contains(name, ".") {
		return nil, fmt.Errorf("Unknown symbol '%s'", name)
	}
	var err error
	pack := e.NewPackage(name)

	for methodName, methodValue := range methods {
		err = pack.Define(methodName, methodValue)
		if err != nil {
			return pack, err
		}
	}
	for typeName, typeValue := range types {
		err = pack.DefineType(typeName, typeValue)
		if err != nil {
			return pack, err
		}
	}

	// can ignore error from Define because of check at the start of the function
	e.Define(name, pack)
	return pack, nil
}

// SetExternal sets an external resolver
func (e *Env) SetExternal(res EnvResolver) {
	e.external = res
}

// Destroy deletes current scope.
func (e *Env) Destroy() {
	e.Lock()
	defer e.Unlock()

	if e.parent == nil {
		return
	}
	for k, v := range e.parent.env {
		if v.IsValid() && v.Interface() == e {
			delete(e.parent.env, k)
		}
	}
	e.parent = nil
	e.env = nil
}

// NewModule creates new module scope as global.
func (e *Env) NewModule(n string) *Env {
	m := &Env{
		env:    make(map[string]reflect.Value),
		parent: e,
		name:   n,
	}
	e.Define(n, m)
	return m
}

// SetName sets a name of the scope. This means that the scope is module.
func (e *Env) SetName(n string) {
	e.Lock()
	e.name = n
	e.Unlock()
}

// GetName returns module name.
func (e *Env) GetName() string {
	e.RLock()
	defer e.RUnlock()

	return e.name
}

// Addr returns pointer value which specified symbol. It goes to upper scope until
// found or returns error.
func (e *Env) Addr(k string) (reflect.Value, error) {
	e.RLock()
	defer e.RUnlock()

	if v, ok := e.env[k]; ok {
		if v.CanAddr() {
			return v.Addr(), nil
		}
		return nilValue, fmt.Errorf("Unaddressable")
	}
	if e.external != nil {
		v, err := e.external.Get(k)
		if err == nil {
			if v.CanAddr() {
				return v.Addr(), nil
			}
			return nilValue, fmt.Errorf("Unaddressable")
		}
	}
	if e.parent == nil {
		return nilValue, fmt.Errorf("Undefined symbol '%s'", k)
	}
	return e.parent.Addr(k)
}

// Type returns type which specified symbol. It goes to upper scope until
// found or returns error.
func (e *Env) Type(k string) (reflect.Type, error) {
	e.RLock()
	defer e.RUnlock()

	if v, ok := e.typ[k]; ok {
		return v, nil
	}
	if e.external != nil {
		v, err := e.external.Type(k)
		if err == nil {
			return v, nil
		}
	}
	if e.parent == nil {
		return nilType, fmt.Errorf("Undefined type '%s'", k)
	}
	return e.parent.Type(k)
}

// Get returns value which specified symbol. It goes to upper scope until
// found or returns error.
func (e *Env) Get(k string) (interface{}, error) {
	rv, err := e.get(k)
	if !rv.IsValid() || !rv.CanInterface() {
		return nil, err
	}
	return rv.Interface(), err
}

func (e *Env) get(k string) (reflect.Value, error) {
	e.RLock()
	defer e.RUnlock()

	if v, ok := e.env[k]; ok {
		return v, nil
	}
	if e.external != nil {
		v, err := e.external.Get(k)
		if err == nil {
			return v, nil
		}
	}
	if e.parent == nil {
		return nilValue, fmt.Errorf("Undefined symbol '%s'", k)
	}
	return e.parent.get(k)
}

// Set modifies value which specified as symbol. It goes to upper scope until
// found or returns error.
func (e *Env) Set(k string, v interface{}) error {
	if v == nil {
		return e.setValue(k, nilValue)
	}
	return e.setValue(k, reflect.ValueOf(v))
}

func (e *Env) setValue(k string, v reflect.Value) error {
	e.RLock()
	_, ok := e.env[k]
	e.RUnlock()
	if ok {
		e.Lock()
		e.env[k] = v
		e.Unlock()
		return nil
	}
	if e.parent == nil {
		return fmt.Errorf("Unknown symbol '%s'", k)
	}
	return e.parent.setValue(k, v)
}

// DefineGlobal defines symbol in global scope.
func (e *Env) DefineGlobal(k string, v interface{}) error {
	for e.parent != nil {
		e = e.parent
	}
	return e.Define(k, v)
}

// defineGlobalValue defines symbol in global scope.
func (e *Env) defineGlobalValue(k string, v reflect.Value) error {
	for e.parent != nil {
		e = e.parent
	}
	return e.defineValue(k, v)
}

// Define defines symbol in current scope.
func (e *Env) Define(k string, v interface{}) error {
	if v == nil {
		return e.defineValue(k, nilValue)
	}
	return e.defineValue(k, reflect.ValueOf(v))
}

// defineValue defines symbol in current scope.
func (e *Env) defineValue(k string, v reflect.Value) error {
	if strings.Contains(k, ".") {
		return fmt.Errorf("Unknown symbol '%s'", k)
	}

	e.Lock()
	e.env[k] = v
	e.Unlock()

	return nil
}

// DefineGlobalType defines type in global scope.
func (e *Env) DefineGlobalType(k string, t interface{}) error {
	for e.parent != nil {
		e = e.parent
	}
	return e.DefineType(k, t)
}

// DefineGlobalReflectType defines type in global scope.
func (e *Env) DefineGlobalReflectType(k string, t reflect.Type) error {
	for e.parent != nil {
		e = e.parent
	}
	return e.DefineReflectType(k, t)
}

// DefineType defines type in current scope.
func (e *Env) DefineType(k string, t interface{}) error {
	var typ reflect.Type
	if t == nil {
		typ = nilType
	} else {
		var ok bool
		typ, ok = t.(reflect.Type)
		if !ok {
			typ = reflect.TypeOf(t)
		}
	}

	return e.DefineReflectType(k, typ)
}

// DefineReflectType defines type in current scope.
func (e *Env) DefineReflectType(k string, t reflect.Type) error {
	if strings.Contains(k, ".") {
		return fmt.Errorf("Unknown symbol '%s'", k)
	}

	e.Lock()
	e.typ[k] = t
	e.Unlock()

	return nil
}

// String return the name of current scope.
func (e *Env) String() string {
	e.RLock()
	defer e.RUnlock()

	return e.name
}

// Dump show symbol values in the scope.
func (e *Env) Dump() {
	e.RLock()
	fmt.Printf("Name: %v\n", e.name)
	fmt.Printf("Has parent: %v\n", e.parent != nil)
	for k, v := range e.env {
		fmt.Printf("%v = %#v\n", k, v)
	}
	e.RUnlock()
}

// Execute parses and runs source in current scope.
func (e *Env) Execute(src string) (interface{}, error) {
	stmts, err := parser.ParseSrc(src)
	if err != nil {
		return nilValue, err
	}
	return Run(stmts, e)
}
