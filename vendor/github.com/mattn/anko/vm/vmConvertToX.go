package vm

import (
	"fmt"
	"reflect"
)

func reflectValueSlicetoInterfaceSlice(valueSlice []reflect.Value) reflect.Value {
	interfaceSlice := make([]interface{}, 0, len(valueSlice))
	for _, value := range valueSlice {
		if !value.IsValid() {
			interfaceSlice = append(interfaceSlice, nil)
			continue
		}
		if value.Kind() == reflect.Interface && !value.IsNil() {
			value = value.Elem()
		}
		if value.CanInterface() {
			interfaceSlice = append(interfaceSlice, value.Interface())
		} else {
			interfaceSlice = append(interfaceSlice, nil)
		}
	}
	return reflect.ValueOf(interfaceSlice)
}

func convertReflectValueToType(rv reflect.Value, rt reflect.Type) (reflect.Value, error) {
	if !rv.IsValid() {
		return makeValue(rt)
	}
	if rt == interfaceType || rv.Type() == rt  {
		return rv, nil
	}
	if rv.Type().ConvertibleTo(rt) {
		return rv.Convert(rt), nil
	}
	if rv.Kind() == reflect.Func && rt.Kind() == reflect.Func {
		return convertVmFunctionToType(rv, rt)
	}
	if rv.Type() != interfaceType {
		return rv, fmt.Errorf("invalid type conversion")
	}
	if rv.IsNil() {
		return makeValue(rt)
	}

	rv = rv.Elem()
	if rv.Type() == rt {
		return rv, nil
	}
	if rv.Type().ConvertibleTo(rt) {
		return rv.Convert(rt), nil
	}
	if rv.Type() != interfaceType {
		return rv, fmt.Errorf("invalid type conversion")
	}
	if rv.IsNil() {
		return makeValue(rt)
	}
	return rv, fmt.Errorf("invalid type conversion")
}

func convertVmFunctionToType(rv reflect.Value, rt reflect.Type) (reflect.Value, error) {
	if !checkIfRunVmFunction(rv.Type()) {
		return rv, fmt.Errorf("invalid type conversion")
	}

	runVmConvertFunction := func(in []reflect.Value) []reflect.Value {
		args := make([]reflect.Value, 0, rt.NumIn())
		for i := 0; i < rt.NumIn(); i++ {
			args = append(args, reflect.ValueOf(in[i]))
		}

		rvs := rv.Call(args)

		rv, err := processCallReturnValues(rvs, true, false)
		if err != nil {
			panic("function run error: " + err.Error())
		}

		if rt.NumOut() < 1 {
			return []reflect.Value{}
		}
		if rt.NumOut() < 2 {
			rv, err = convertReflectValueToType(rv, rt.Out(0))
			if err != nil {
				panic("function wants return type " + rt.Out(0).String() + " but received type " + rv.Type().String())
			}
			return []reflect.Value{rv}
		}

		if rv.Type() != reflectValueSliceType {
			panic("bad function return type: " + rv.Type().String())
		}

		outValues := rv.Interface().([]reflect.Value)
		if len(outValues) < rt.NumOut() {
			panic(fmt.Sprintf("function wants %v return vavlues but recived %v values", rt.NumOut(), len(outValues)))
		}

		rvs = make([]reflect.Value, 0, rt.NumOut())
		for i := 0; i < rt.NumOut(); i++ {
			rv, err = convertReflectValueToType(outValues[i], rt.Out(i))
			if err != nil {
				panic("function wants return type " + rt.Out(i).String() + " but received type " + rv.Type().String())
			}
			rvs = append(rvs, rv)
		}
		return rvs
	}

	return reflect.MakeFunc(rt, runVmConvertFunction), nil
}
