package core

import (
	"fmt"
	"reflect"
	"strconv"
	"strings"
	"time"

	"github.com/mattn/anko/vm"
)

// ImportToX adds all the toX to the env given
func ImportToX(env *vm.Env) {

	env.Define("toBool", func(v interface{}) bool {
		nt := reflect.TypeOf(true)
		rv := reflect.ValueOf(v)
		if rv.Type().ConvertibleTo(nt) {
			return rv.Convert(nt).Bool()
		}
		if rv.Type().ConvertibleTo(reflect.TypeOf(1.0)) && rv.Convert(reflect.TypeOf(1.0)).Float() > 0.0 {
			return true
		}
		if rv.Kind() == reflect.String {
			s := strings.ToLower(v.(string))
			if s == "y" || s == "yes" {
				return true
			}
			b, err := strconv.ParseBool(s)
			if err == nil {
				return b
			}
		}
		return false
	})

	env.Define("toString", func(v interface{}) string {
		if b, ok := v.([]byte); ok {
			return string(b)
		}
		return fmt.Sprint(v)
	})

	env.Define("toInt", func(v interface{}) int64 {
		nt := reflect.TypeOf(1)
		rv := reflect.ValueOf(v)
		if rv.Type().ConvertibleTo(nt) {
			return rv.Convert(nt).Int()
		}
		if rv.Kind() == reflect.String {
			i, err := strconv.ParseInt(v.(string), 10, 64)
			if err == nil {
				return i
			}
			f, err := strconv.ParseFloat(v.(string), 64)
			if err == nil {
				return int64(f)
			}
		}
		if rv.Kind() == reflect.Bool {
			if v.(bool) {
				return 1
			}
		}
		return 0
	})

	env.Define("toFloat", func(v interface{}) float64 {
		nt := reflect.TypeOf(1.0)
		rv := reflect.ValueOf(v)
		if rv.Type().ConvertibleTo(nt) {
			return rv.Convert(nt).Float()
		}
		if rv.Kind() == reflect.String {
			f, err := strconv.ParseFloat(v.(string), 64)
			if err == nil {
				return f
			}
		}
		if rv.Kind() == reflect.Bool {
			if v.(bool) {
				return 1.0
			}
		}
		return 0.0
	})

	env.Define("toChar", func(s rune) string {
		return string(s)
	})

	env.Define("toRune", func(s string) rune {
		if len(s) == 0 {
			return 0
		}
		return []rune(s)[0]
	})

	env.Define("toBoolSlice", func(v []interface{}) []bool {
		var result []bool
		toSlice(v, &result)
		return result
	})

	env.Define("toStringSlice", func(v []interface{}) []string {
		var result []string
		toSlice(v, &result)
		return result
	})

	env.Define("toIntSlice", func(v []interface{}) []int64 {
		var result []int64
		toSlice(v, &result)
		return result
	})

	env.Define("toFloatSlice", func(v []interface{}) []float64 {
		var result []float64
		toSlice(v, &result)
		return result
	})

	env.Define("toByteSlice", func(s string) []byte {
		return []byte(s)
	})

	env.Define("toRuneSlice", func(s string) []rune {
		return []rune(s)
	})

	env.Define("toDuration", func(v int64) time.Duration {
		return time.Duration(v)
	})

}

// toSlice takes in a "generic" slice and converts and copies
// it's elements into the typed slice pointed at by ptr.
// Note that this is a costly operation.
func toSlice(from []interface{}, ptr interface{}) {
	// Value of the pointer to the target
	obj := reflect.Indirect(reflect.ValueOf(ptr))
	// We can't just convert from interface{} to whatever the target is (diff memory layout),
	// so we need to create a New slice of the proper type and copy the values individually
	t := reflect.TypeOf(ptr).Elem()
	slice := reflect.MakeSlice(t, len(from), len(from))
	// Copying the data, val is an addressable Pointer of the actual target type
	val := reflect.Indirect(reflect.New(t.Elem()))
	for i := 0; i < len(from); i++ {
		v := reflect.ValueOf(from[i])
		val.Set(v)
		slice.Index(i).Set(v)
	}
	// Ok now assign our slice to the target pointer
	obj.Set(slice)
}
