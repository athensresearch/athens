package vm

import (
	"errors"
	"fmt"
	"reflect"
	"strings"

	"github.com/mattn/anko/ast"
	"github.com/mattn/anko/parser"
)

type (
	// Error provides a convenient interface for handling runtime error.
	// It can be Error interface with type cast which can call Pos().
	Error struct {
		Message string
		Pos     ast.Position
	}
)

var (
	nilType               = reflect.TypeOf(nil)
	stringType            = reflect.TypeOf("a")
	interfaceType         = reflect.ValueOf([]interface{}{int64(1)}).Index(0).Type()
	interfaceSliceType    = reflect.TypeOf([]interface{}{})
	reflectValueType      = reflect.TypeOf(reflect.Value{})
	reflectValueSliceType = reflect.TypeOf([]reflect.Value{})
	errorType             = reflect.ValueOf([]error{nil}).Index(0).Type()
	vmErrorType           = reflect.TypeOf(&Error{})

	nilValue                  = reflect.New(reflect.TypeOf((*interface{})(nil)).Elem()).Elem()
	trueValue                 = reflect.ValueOf(true)
	falseValue                = reflect.ValueOf(false)
	zeroValue                 = reflect.Value{}
	reflectValueErrorNilValue = reflect.ValueOf(reflect.New(errorType).Elem())

	// BreakError when there is an unexpected break statement
	BreakError = errors.New("Unexpected break statement")
	// ContinueError when there is an unexpected continue statement
	ContinueError = errors.New("Unexpected continue statement")
	// ReturnError when there is an unexpected return statement
	ReturnError = errors.New("Unexpected return statement")
	// InterruptError when execution has been interrupted
	InterruptError = errors.New("Execution interrupted")
)

// newStringError makes error interface with message.
func newStringError(pos ast.Pos, err string) error {
	if pos == nil {
		return &Error{Message: err, Pos: ast.Position{1, 1}}
	}
	return &Error{Message: err, Pos: pos.Position()}
}

// newErrorf makes error interface with message.
func newErrorf(pos ast.Pos, format string, args ...interface{}) error {
	return &Error{Message: fmt.Sprintf(format, args...), Pos: pos.Position()}
}

// newError makes error interface with message.
// This doesn't overwrite last error.
func newError(pos ast.Pos, err error) error {
	if err == nil {
		return nil
	}
	if err == BreakError || err == ContinueError || err == ReturnError {
		return err
	}
	if pe, ok := err.(*parser.Error); ok {
		return pe
	}
	if ee, ok := err.(*Error); ok {
		return ee
	}
	return &Error{Message: err.Error(), Pos: pos.Position()}
}

// Error returns the error message.
func (e *Error) Error() string {
	return e.Message
}

// Interrupt interrupts the execution of any running statements in the specified environment.
// This includes all parent & child environments.
// Note that the execution is not instantly aborted: after a call to Interrupt,
// the current running statement will finish, but the next statement will not run,
// and instead will return a nilValue and an InterruptError.
func Interrupt(env *Env) {
	env.Lock()
	*(env.interrupt) = true
	env.Unlock()
}

// ClearInterrupt removes the interrupt flag from specified environment.
// This includes all parent & child environments.
func ClearInterrupt(env *Env) {
	env.Lock()
	*(env.interrupt) = false
	env.Unlock()
}

func isNil(v reflect.Value) bool {
	if !v.IsValid() {
		return false
	}
	switch v.Kind() {
	case reflect.Chan, reflect.Func, reflect.Interface, reflect.Map, reflect.Ptr, reflect.Slice:
		// from reflect IsNil:
		// Note that IsNil is not always equivalent to a regular comparison with nil in Go.
		// For example, if v was created by calling ValueOf with an uninitialized interface variable i,
		// i==nil will be true but v.IsNil will panic as v will be the zero Value.
		return v.IsNil()
	default:
		return false
	}
}

func isNum(v reflect.Value) bool {
	switch v.Kind() {
	case reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64,
		reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64, reflect.Uintptr,
		reflect.Float32, reflect.Float64:
		return true
	}
	return false
}

// equal returns true when lhsV and rhsV is same value.
func equal(lhsV, rhsV reflect.Value) bool {
	lhsNotValid, rhsVNotValid := !lhsV.IsValid(), !rhsV.IsValid()
	if lhsNotValid && rhsVNotValid {
		return true
	}
	if (!lhsNotValid && rhsVNotValid) || (lhsNotValid && !rhsVNotValid) {
		return false
	}

	lhsIsNil, rhsIsNil := isNil(lhsV), isNil(rhsV)
	if lhsIsNil && rhsIsNil {
		return true
	}
	if (!lhsIsNil && rhsIsNil) || (lhsIsNil && !rhsIsNil) {
		return false
	}
	if lhsV.Kind() == reflect.Interface || lhsV.Kind() == reflect.Ptr {
		lhsV = lhsV.Elem()
	}
	if rhsV.Kind() == reflect.Interface || rhsV.Kind() == reflect.Ptr {
		rhsV = rhsV.Elem()
	}

	// Compare a string and a number.
	// This will attempt to convert the string to a number,
	// while leaving the other side alone. Code further
	// down takes care of converting ints and floats as needed.
	if isNum(lhsV) && rhsV.Kind() == reflect.String {
		rhsF, err := tryToFloat64(rhsV)
		if err != nil {
			// Couldn't convert RHS to a float, they can't be compared.
			return false
		}
		rhsV = reflect.ValueOf(rhsF)
	} else if lhsV.Kind() == reflect.String && isNum(rhsV) {
		// If the LHS is a string formatted as an int, try that before trying float
		lhsI, err := tryToInt64(lhsV)
		if err != nil {
			// if LHS is a float, e.g. "1.2", we need to set lhsV to a float64
			lhsF, err := tryToFloat64(lhsV)
			if err != nil {
				return false
			}
			lhsV = reflect.ValueOf(lhsF)
		} else {
			lhsV = reflect.ValueOf(lhsI)
		}
	}

	if isNum(lhsV) && isNum(rhsV) {
		if rhsV.Type().ConvertibleTo(lhsV.Type()) {
			rhsV = rhsV.Convert(lhsV.Type())
		}
	}

	// Try to compare bools to strings and numbers
	if lhsV.Kind() == reflect.Bool || rhsV.Kind() == reflect.Bool {
		lhsB, err := tryToBool(lhsV, false)
		if err != nil {
			return false
		}
		rhsB, err := tryToBool(rhsV, false)
		if err != nil {
			return false
		}
		return lhsB == rhsB
	}

	if lhsV.CanInterface() && rhsV.CanInterface() {
		return reflect.DeepEqual(lhsV.Interface(), rhsV.Interface())
	}
	return reflect.DeepEqual(lhsV, rhsV)
}

func getMapIndex(key reflect.Value, aMap reflect.Value) reflect.Value {
	if !aMap.IsValid() || aMap.IsNil() {
		return nilValue
	}

	keyType := key.Type()
	if keyType == interfaceType && aMap.Type().Key() != interfaceType {
		if key.Elem().IsValid() && !key.Elem().IsNil() {
			keyType = key.Elem().Type()
		}
	}
	if keyType != aMap.Type().Key() {
		return nilValue
	}

	// From reflect MapIndex:
	// It returns the zero Value if key is not found in the map or if v represents a nil map.
	value := aMap.MapIndex(key)

	if value.IsValid() && value.CanInterface() && aMap.Type().Elem() == interfaceType && !value.IsNil() {
		value = reflect.ValueOf(value.Interface())
	}

	// Note if the map is of reflect.Value, it will incorrectly return nil when zero value
	// Unware of any other way for this to be done to correct that
	if value == zeroValue {
		return nilValue
	}

	return value
}

func appendSlice(expr ast.Expr, lhsV reflect.Value, rhsV reflect.Value) (reflect.Value, error) {
	lhsT := lhsV.Type().Elem()
	rhsT := rhsV.Type().Elem()

	if lhsT == rhsT {
		return reflect.AppendSlice(lhsV, rhsV), nil
	}

	if rhsT.ConvertibleTo(lhsT) {
		for i := 0; i < rhsV.Len(); i++ {
			lhsV = reflect.Append(lhsV, rhsV.Index(i).Convert(lhsT))
		}
		return lhsV, nil
	}

	leftHasSubArray := lhsT.Kind() == reflect.Slice || lhsT.Kind() == reflect.Array
	rightHasSubArray := rhsT.Kind() == reflect.Slice || rhsT.Kind() == reflect.Array

	if leftHasSubArray != rightHasSubArray && lhsT != interfaceType && rhsT != interfaceType {
		return nilValue, newStringError(expr, "invalid type conversion")
	}

	if !leftHasSubArray && !rightHasSubArray {
		for i := 0; i < rhsV.Len(); i++ {
			value := rhsV.Index(i)
			if rhsT == interfaceType {
				value = value.Elem()
			}
			if lhsT == value.Type() {
				lhsV = reflect.Append(lhsV, value)
			} else if value.Type().ConvertibleTo(lhsT) {
				lhsV = reflect.Append(lhsV, value.Convert(lhsT))
			} else {
				return nilValue, newStringError(expr, "invalid type conversion")
			}
		}
		return lhsV, nil
	}

	if (leftHasSubArray || lhsT == interfaceType) && (rightHasSubArray || rhsT == interfaceType) {
		for i := 0; i < rhsV.Len(); i++ {
			value := rhsV.Index(i)
			if rhsT == interfaceType {
				value = value.Elem()
				if value.Kind() != reflect.Slice && value.Kind() != reflect.Array {
					return nilValue, newStringError(expr, "invalid type conversion")
				}
			}
			newSlice, err := appendSlice(expr, reflect.MakeSlice(lhsT, 0, value.Len()), value)
			if err != nil {
				return nilValue, err
			}
			lhsV = reflect.Append(lhsV, newSlice)
		}
		return lhsV, nil
	}

	return nilValue, newStringError(expr, "invalid type conversion")
}

func getTypeFromString(env *Env, name string) (reflect.Type, error) {
	env, typeString, err := getEnvFromString(env, name)
	if err != nil {
		return nilType, err
	}
	t, err := env.Type(typeString)
	if err != nil {
		return nilType, err
	}
	return t, nil
}

func getEnvFromString(env *Env, name string) (*Env, string, error) {
	nameSplit := strings.SplitN(name, ".", 2)
	for len(nameSplit) > 1 {
		e, found := env.env[nameSplit[0]]
		if !found {
			return nil, "", fmt.Errorf("no namespace called: %v", nameSplit[0])
		}
		env = e.Interface().(*Env)
		nameSplit = strings.SplitN(nameSplit[1], ".", 2)
	}
	return env, nameSplit[0], nil
}

func makeValue(t reflect.Type) (reflect.Value, error) {
	switch t.Kind() {
	case reflect.Chan:
		return reflect.MakeChan(t, 0), nil
	case reflect.Func:
		return reflect.MakeFunc(t, nil), nil
	case reflect.Map:
		// note creating slice as work around to create map
		// just doing MakeMap can give incorrect type for defined types
		value := reflect.MakeSlice(reflect.SliceOf(t), 0, 1)
		value = reflect.Append(value, reflect.MakeMap(reflect.MapOf(t.Key(), t.Elem())))
		return value.Index(0), nil
	case reflect.Ptr:
		ptrV := reflect.New(t.Elem())
		v, err := makeValue(t.Elem())
		if err != nil {
			return nilValue, err
		}
		if !ptrV.Elem().CanSet() {
			return nilValue, fmt.Errorf("type " + t.String() + " cannot be assigned")
		}
		ptrV.Elem().Set(v)
		return ptrV, nil
	case reflect.Slice:
		return reflect.MakeSlice(t, 0, 0), nil
	case reflect.Struct:
		structV := reflect.New(t).Elem()
		for i := 0; i < structV.NumField(); i++ {
			v, err := makeValue(structV.Field(i).Type())
			if err != nil {
				return nilValue, err
			}
			if !structV.Field(i).CanSet() {
				return nilValue, fmt.Errorf("struct member '" + t.Field(i).Name + "' cannot be assigned")
			}
			structV.Field(i).Set(v)
		}
		return structV, nil
	}
	return reflect.New(t).Elem(), nil
}
