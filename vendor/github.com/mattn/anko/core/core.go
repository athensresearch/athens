// Package core implements core interface for anko script.
package core

import (
	"fmt"
	"io/ioutil"
	"os"
	"reflect"

	"github.com/mattn/anko/parser"
	"github.com/mattn/anko/vm"
)

// Import defines core language builtins - keys, range, println,  etc.
func Import(env *vm.Env) *vm.Env {
	env.Define("keys", func(v interface{}) []string {
		rv := reflect.ValueOf(v)
		if rv.Kind() == reflect.Interface {
			rv = rv.Elem()
		}
		mapKeysValue := rv.MapKeys()
		mapKeys := make([]string, len(mapKeysValue))
		for i := 0; i < len(mapKeysValue); i++ {
			mapKeys[i] = mapKeysValue[i].String()
		}
		return mapKeys
	})

	env.Define("range", func(args ...int64) []int64 {
		if len(args) < 1 {
			panic("Missing arguments")
		}
		if len(args) > 2 {
			panic("Too many arguments")
		}
		var min, max int64
		if len(args) == 1 {
			min = 0
			max = args[0] - 1
		} else {
			min = args[0]
			max = args[1]
		}
		arr := []int64{}
		for i := min; i <= max; i++ {
			arr = append(arr, i)
		}
		return arr
	})

	env.Define("typeOf", func(v interface{}) string {
		return reflect.TypeOf(v).String()
	})

	env.Define("kindOf", func(v interface{}) string {
		typeOf := reflect.TypeOf(v)
		if typeOf == nil {
			return "nil"
		}
		return typeOf.Kind().String()
	})

	env.Define("chanOf", func(t reflect.Type) reflect.Value {
		return reflect.MakeChan(t, 1)
	})

	env.Define("defined", func(s string) bool {
		_, err := env.Get(s)
		return err == nil
	})

	env.Define("load", func(s string) interface{} {
		body, err := ioutil.ReadFile(s)
		if err != nil {
			panic(err)
		}
		scanner := new(parser.Scanner)
		scanner.Init(string(body))
		stmts, err := parser.Parse(scanner)
		if err != nil {
			if pe, ok := err.(*parser.Error); ok {
				pe.Filename = s
				panic(pe)
			}
			panic(err)
		}
		rv, err := vm.Run(stmts, env)
		if err != nil {
			panic(err)
		}
		return rv
	})

	env.Define("panic", func(e interface{}) {
		os.Setenv("ANKO_DEBUG", "1")
		panic(e)
	})

	env.Define("print", fmt.Print)
	env.Define("println", fmt.Println)
	env.Define("printf", fmt.Printf)
	env.Define("close", func(e interface{}) {
		reflect.ValueOf(e).Close()
	})

	ImportToX(env)

	return env
}
