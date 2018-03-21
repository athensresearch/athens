package vm

import (
	"fmt"
	"os"
	"reflect"

	"github.com/mattn/anko/ast"
)

func funcExpr(funcExpr *ast.FuncExpr, env *Env) (reflect.Value, error) {
	inTypes := make([]reflect.Type, len(funcExpr.Params), len(funcExpr.Params))
	for i := 0; i < len(inTypes); i++ {
		inTypes[i] = reflectValueType
	}
	if funcExpr.VarArg {
		inTypes[len(inTypes)-1] = interfaceSliceType
	}
	funcType := reflect.FuncOf(inTypes, []reflect.Type{reflectValueType, reflectValueType}, funcExpr.VarArg)

	runVmFunction := func(in []reflect.Value) []reflect.Value {
		var err error
		var rv reflect.Value

		newEnv := env.NewEnv()
		for i := 0; i < len(funcExpr.Params)-1; i++ {
			rv = in[i].Interface().(reflect.Value)
			err = newEnv.defineValue(funcExpr.Params[i], rv)
			if err != nil {
				return []reflect.Value{reflect.ValueOf(nilValue), reflect.ValueOf(reflect.ValueOf(newError(funcExpr, err)))}
			}
		}
		if len(funcExpr.Params) > 0 {
			if funcExpr.VarArg {
				rv = in[len(funcExpr.Params)-1]
				err = newEnv.defineValue(funcExpr.Params[len(funcExpr.Params)-1], rv)
				if err != nil {
					return []reflect.Value{reflect.ValueOf(nilValue), reflect.ValueOf(reflect.ValueOf(newError(funcExpr, err)))}
				}
			} else {
				rv = in[len(funcExpr.Params)-1].Interface().(reflect.Value)
				err = newEnv.defineValue(funcExpr.Params[len(funcExpr.Params)-1], rv)
				if err != nil {
					return []reflect.Value{reflect.ValueOf(nilValue), reflect.ValueOf(reflect.ValueOf(newError(funcExpr, err)))}
				}
			}
		}

		rv, err = run(funcExpr.Stmts, newEnv)
		if err != nil && err != ReturnError {
			err = newError(funcExpr, err)
			return []reflect.Value{reflect.ValueOf(nilValue), reflect.ValueOf(reflect.ValueOf(newError(funcExpr, err)))}
		}

		return []reflect.Value{reflect.ValueOf(rv), reflectValueErrorNilValue}
	}

	rv := reflect.MakeFunc(funcType, runVmFunction)

	if funcExpr.Name != "" {
		err := env.defineValue(funcExpr.Name, rv)
		if err != nil {
			return nilValue, newError(funcExpr, err)
		}
	}
	return rv, nil
}

func anonCallExpr(e *ast.AnonCallExpr, env *Env) (reflect.Value, error) {
	f, err := invokeExpr(e.Expr, env)
	if err != nil {
		return nilValue, newError(e, err)
	}
	if f.Kind() == reflect.Interface && !f.IsNil() {
		f = f.Elem()
	}
	if f.Kind() == reflect.Func {
		return invokeExpr(&ast.CallExpr{Func: f, SubExprs: e.SubExprs, VarArg: e.VarArg, Go: e.Go}, env)
	}
	if !f.IsValid() {
		return nilValue, newStringError(e, "cannot call type invalid")
	}
	return nilValue, newStringError(e, "cannot call type "+f.Type().String())
}

func callExpr(callExpr *ast.CallExpr, env *Env) (rv reflect.Value, err error) {
	// Note that if the function type looks the same as the VM function type, the returned values will probably be wrong

	rv = nilValue

	f := callExpr.Func
	if !f.IsValid() {
		f, err = env.get(callExpr.Name)
		if err != nil {
			err = newError(callExpr, err)
			return
		}
	}

	if f.Kind() == reflect.Interface && !f.IsNil() {
		f = f.Elem()
	}
	if !f.IsValid() {
		err = newStringError(callExpr, "cannot call type invalid")
		return
	}
	if f.Kind() != reflect.Func {
		err = newStringError(callExpr, "cannot call type "+f.Type().String())
		return
	}

	var rvs []reflect.Value
	var args []reflect.Value
	var useCallSlice bool
	fType := f.Type()
	isRunVmFunction := checkIfRunVmFunction(fType)
	args, useCallSlice, err = makeCallArgs(fType, isRunVmFunction, callExpr, env)
	if err != nil {
		return
	}

	defer func() {
		if os.Getenv("ANKO_DEBUG") == "" {
			if recoverResult := recover(); recoverResult != nil {
				err = fmt.Errorf("%v", recoverResult)
			}
		}
	}()

	if useCallSlice {
		if callExpr.Go {
			go f.CallSlice(args)
			return
		}
		rvs = f.CallSlice(args)
	} else {
		if callExpr.Go {
			go f.Call(args)
			return
		}
		rvs = f.Call(args)
	}

	// TOFIX: how VM pointers/addressing work
	// Untill then, this is a work around to set pointers back to VM variables
	// This will probably panic for some functions and/or calls that are variadic
	if !isRunVmFunction {
		for i, expr := range callExpr.SubExprs {
			if addrExpr, ok := expr.(*ast.AddrExpr); ok {
				if identExpr, ok := addrExpr.Expr.(*ast.IdentExpr); ok {
					invokeLetExpr(identExpr, args[i].Elem(), env)
				}
			}
		}
	}

	rv, err = processCallReturnValues(rvs, isRunVmFunction, true)

	return
}

func checkIfRunVmFunction(rt reflect.Type) bool {
	if rt.NumOut() != 2 || rt.Out(0) != reflectValueType || rt.Out(1) != reflectValueType {
		return false
	}
	if rt.NumIn() > 1 {
		if rt.IsVariadic() {
			if rt.In(rt.NumIn()-1) != interfaceSliceType {
				return false
			}
		} else {
			if rt.In(rt.NumIn()-1) != reflectValueType {
				return false
			}
		}
		for i := 0; i < rt.NumIn()-1; i++ {
			if rt.In(i) != reflectValueType {
				return false
			}
		}
	}
	return true
}

func makeCallArgs(rt reflect.Type, isRunVmFunction bool, callExpr *ast.CallExpr, env *Env) ([]reflect.Value, bool, error) {
	numIn := rt.NumIn()
	if numIn < 1 {
		return []reflect.Value{}, false, nil
	}
	numExprs := len(callExpr.SubExprs)
	if (!rt.IsVariadic() && !callExpr.VarArg && numIn != numExprs) ||
		(rt.IsVariadic() && callExpr.VarArg && (numIn < numExprs || numIn > numExprs+1)) ||
		(rt.IsVariadic() && !callExpr.VarArg && numIn > numExprs+1) ||
		(!rt.IsVariadic() && callExpr.VarArg && numIn < numExprs) {
		return []reflect.Value{}, false, newStringError(callExpr, fmt.Sprintf("function wants %v arguments but received %v", numIn, numExprs))
	}
	if rt.IsVariadic() && rt.In(numIn-1).Kind() != reflect.Slice && rt.In(numIn-1).Kind() != reflect.Array {
		return []reflect.Value{}, false, newStringError(callExpr, "function is variadic but last parameter is of type "+rt.In(numIn-1).String())
	}

	var err error
	var arg reflect.Value
	var args []reflect.Value
	if numIn > numExprs {
		args = make([]reflect.Value, 0, numIn)
	} else {
		args = make([]reflect.Value, 0, numExprs)
	}
	indexIn := 0
	indexExpr := 0
	for indexIn < numIn-1 && indexExpr < numExprs-1 {
		arg, err = invokeExpr(callExpr.SubExprs[indexExpr], env)
		if err != nil {
			return []reflect.Value{}, false, newError(callExpr.SubExprs[indexExpr], err)
		}
		if isRunVmFunction {
			args = append(args, reflect.ValueOf(arg))
		} else {
			arg, err = convertReflectValueToType(arg, rt.In(indexIn))
			if err != nil {
				return []reflect.Value{}, false, newStringError(callExpr.SubExprs[indexExpr],
					"function wants argument type "+rt.In(indexIn).String()+" but received type "+arg.Type().String())
			}
			args = append(args, arg)
		}
		indexIn++
		indexExpr++
	}

	if !rt.IsVariadic() && !callExpr.VarArg {
		arg, err = invokeExpr(callExpr.SubExprs[indexExpr], env)
		if err != nil {
			return []reflect.Value{}, false, newError(callExpr.SubExprs[indexExpr], err)
		}
		if isRunVmFunction {
			args = append(args, reflect.ValueOf(arg))
		} else {
			arg, err = convertReflectValueToType(arg, rt.In(indexIn))
			if err != nil {
				return []reflect.Value{}, false, newStringError(callExpr.SubExprs[indexExpr],
					"function wants argument type "+rt.In(indexIn).String()+" but received type "+arg.Type().String())
			}
			args = append(args, arg)
		}
		return args, false, nil
	}

	if (rt.IsVariadic() && !callExpr.VarArg) || (rt.IsVariadic() && callExpr.VarArg) {
		if indexExpr == numExprs {
			return args, false, nil
		}

		if numIn > numExprs {
			arg, err = invokeExpr(callExpr.SubExprs[indexExpr], env)
			if err != nil {
				return []reflect.Value{}, false, newError(callExpr.SubExprs[indexExpr], err)
			}
			if isRunVmFunction {
				args = append(args, reflect.ValueOf(arg))
			} else {
				arg, err = convertReflectValueToType(arg, rt.In(indexIn))
				if err != nil {
					return []reflect.Value{}, false, newStringError(callExpr.SubExprs[indexExpr],
						"function wants argument type "+rt.In(indexIn).String()+" but received type "+arg.Type().String())
				}
				args = append(args, arg)
			}
			return args, false, nil
		}

		if rt.IsVariadic() && !callExpr.VarArg {

			sliceType := rt.In(numIn - 1).Elem()
			for indexExpr < numExprs {
				arg, err = invokeExpr(callExpr.SubExprs[indexExpr], env)
				if err != nil {
					return []reflect.Value{}, false, newError(callExpr.SubExprs[indexExpr], err)
				}
				arg, err = convertReflectValueToType(arg, sliceType)
				if err != nil {
					return []reflect.Value{}, false, newStringError(callExpr.SubExprs[indexExpr],
						"function wants argument type "+rt.In(indexIn).String()+" but received type "+arg.Type().String())
				}
				args = append(args, arg)
				indexExpr++
			}
			return args, false, nil

		} else {

			// rt.IsVariadic() && callExpr.VarArg

			sliceType := rt.In(numIn - 1).Elem()
			arg, err = invokeExpr(callExpr.SubExprs[indexExpr], env)
			if err != nil {
				return []reflect.Value{}, false, newError(callExpr.SubExprs[indexExpr], err)
			}
			arg, err = convertReflectValueToType(arg, sliceType)
			if err != nil {
				return []reflect.Value{}, false, newStringError(callExpr.SubExprs[indexExpr],
					"function wants argument type "+rt.In(indexIn).String()+" but received type "+arg.Type().String())
			}
			args = append(args, arg)

			return args, true, nil

		}
	}

	//	!rt.IsVariadic() && callExpr.VarArg

	arg, err = invokeExpr(callExpr.SubExprs[indexExpr], env)
	if err != nil {
		return []reflect.Value{}, false, newError(callExpr.SubExprs[indexExpr], err)
	}
	if arg.Kind() != reflect.Slice && arg.Kind() != reflect.Array {
		return []reflect.Value{}, false, newStringError(callExpr, "call is variadic but last parameter is of type "+arg.Type().String())
	}
	if arg.Len() < numIn-indexIn {
		return []reflect.Value{}, false, newStringError(callExpr, fmt.Sprintf("function wants %v arguments but received %v", numIn, numExprs+arg.Len()-1))
	}

	indexSlice := 0
	for indexIn < numIn {
		if isRunVmFunction {
			args = append(args, reflect.ValueOf(arg.Index(indexSlice)))
		} else {
			arg, err = convertReflectValueToType(arg.Index(indexSlice), rt.In(indexIn))
			if err != nil {
				return []reflect.Value{}, false, newStringError(callExpr.SubExprs[indexExpr],
					"function wants argument type "+rt.In(indexIn).String()+" but received type "+arg.Type().String())
			}
			args = append(args, arg)
		}
		indexIn++
		indexSlice++
	}
	return args, false, nil
}

func processCallReturnValues(rvs []reflect.Value, isRunVmFunction bool, convertToInterfaceSlice bool) (reflect.Value, error) {
	if !isRunVmFunction {
		switch len(rvs) {
		case 0:
			return nilValue, nil
		case 1:
			return rvs[0], nil
		}
		if convertToInterfaceSlice {
			return reflectValueSlicetoInterfaceSlice(rvs), nil
		}
		return reflect.ValueOf(rvs), nil
	}

	if len(rvs) != 2 {
		return nilValue, fmt.Errorf("VM function did not return 2 values but returned %v values", len(rvs))
	}
	if !rvs[0].IsValid() {
		return nilValue, fmt.Errorf("VM function value 1 did not return reflect value type but returned invalid type")
	}
	if !rvs[1].IsValid() {
		return nilValue, fmt.Errorf("VM function value 2 did not return reflect value type but returned invalid type")
	}
	if rvs[0].Type() != reflectValueType {
		return nilValue, fmt.Errorf("VM function value 1 did not return reflect value type but returned %v type", rvs[0].Type().String())
	}
	if rvs[1].Type() != reflectValueType {
		return nilValue, fmt.Errorf("VM function value 2 did not return reflect value type but returned %v type", rvs[1].Type().String())
	}

	rvError := rvs[1].Interface().(reflect.Value)
	if !rvError.IsValid() {
		return nilValue, fmt.Errorf("VM function error type is invalid")
	}
	if rvError.Type() != errorType && rvError.Type() != vmErrorType {
		return nilValue, fmt.Errorf("VM function error type is %v", rvError.Type())
	}

	if rvError.IsNil() {
		return rvs[0].Interface().(reflect.Value), nil
	}

	if rvError.Type() == vmErrorType {
		return nilValue, rvError.Interface().(*Error)
	}
	return nilValue, rvError.Interface().(error)
}
