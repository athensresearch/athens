package vm

import (
	"errors"
	"fmt"
	"math"
	"os"
	"reflect"
	"strconv"
	"strings"

	"github.com/mattn/anko/ast"
)

func invokeLetExpr(expr ast.Expr, rv reflect.Value, env *Env) (reflect.Value, error) {
	switch lhs := expr.(type) {
	case *ast.IdentExpr:
		if env.setValue(lhs.Lit, rv) != nil {
			if strings.Contains(lhs.Lit, ".") {
				return NilValue, NewErrorf(expr, "Undefined symbol '%s'", lhs.Lit)
			}
			env.defineValue(lhs.Lit, rv)
		}
		return rv, nil
	case *ast.MemberExpr:
		v, err := invokeExpr(lhs.Expr, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}

		if v.Kind() == reflect.Interface {
			v = v.Elem()
		}
		if v.Kind() == reflect.Slice {
			v = v.Index(0)
		}
		if !v.IsValid() {
			return NilValue, NewStringError(expr, "type invalid does not support member operation")
		}
		if v.Kind() == reflect.Ptr {
			v = v.Elem()
		}
		if !v.IsValid() {
			return NilValue, NewStringError(expr, "type invalid does not support member operation")
		}

		switch v.Kind() {
		case reflect.Struct:
			field, found := v.Type().FieldByName(lhs.Name)
			if !found {
				return NilValue, NewStringError(expr, "no member named '"+lhs.Name+"' for struct")
			}
			v = v.FieldByIndex(field.Index)
			if !v.CanSet() {
				return NilValue, NewStringError(expr, "struct member '"+lhs.Name+"' cannot be assigned")
			}
			v.Set(rv)
		case reflect.Map:
			if v.Type().Elem() != InterfaceType && v.Type().Elem() != rv.Type() {
				return NilValue, NewStringError(expr, "type "+rv.Type().String()+" cannot be assigned to type "+v.Type().Elem().String()+" for map")
			}
			if v.IsNil() {
				v = reflect.MakeMap(v.Type())
				v.SetMapIndex(reflect.ValueOf(lhs.Name), rv)
				return invokeLetExpr(lhs.Expr, v, env)
			}
			v.SetMapIndex(reflect.ValueOf(lhs.Name), rv)
		default:
			return NilValue, NewStringError(expr, "type "+v.Kind().String()+" does not support member operation")
		}
		return v, nil
	case *ast.ItemExpr:
		v, err := invokeExpr(lhs.Value, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		i, err := invokeExpr(lhs.Index, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		if v.Kind() == reflect.Interface {
			v = v.Elem()
		}

		switch v.Kind() {
		case reflect.Array, reflect.Slice:
			ii, err := tryToInt(i)
			if err != nil {
				return NilValue, NewStringError(expr, "index must be a number")
			}
			if ii == v.Len() {
				// try to do automatic append
				if v.Type().Elem() == InterfaceType || v.Type().Elem() == rv.Type() {
					v = reflect.Append(v, rv)
					return invokeLetExpr(lhs.Value, v, env)
				}
				if !rv.Type().ConvertibleTo(v.Type().Elem()) {
					return NilValue, NewStringError(expr, "type "+rv.Type().String()+" cannot be assigned to type "+v.Type().Elem().String()+" for array index")
				}
				v = reflect.Append(v, rv.Convert(v.Type().Elem()))
				return invokeLetExpr(lhs.Value, v, env)
			}
			if ii < 0 || ii >= v.Len() {
				return NilValue, NewStringError(expr, "index out of range")
			}
			v = v.Index(ii)
			if !v.CanSet() {
				return NilValue, NewStringError(expr, "index cannot be assigned")
			}
			v.Set(rv)
		case reflect.Map:
			keyType := i.Type()
			if keyType == InterfaceType && v.Type().Key() != InterfaceType {
				if i.Elem().IsValid() && !i.Elem().IsNil() {
					keyType = i.Elem().Type()
				}
			}
			if keyType != v.Type().Key() {
				return NilValue, NewStringError(expr, "index type "+keyType.String()+" cannot be used for map index type "+v.Type().Key().String())
			}

			if v.Type().Elem() != InterfaceType && v.Type().Elem() != rv.Type() {
				return NilValue, NewStringError(expr, "type "+rv.Type().String()+" cannot be assigned to type "+v.Type().Elem().String()+" for map")
			}
			if v.IsNil() {
				v = reflect.MakeMap(v.Type())
				v.SetMapIndex(i, rv)
				return invokeLetExpr(lhs.Value, v, env)
			}
			v.SetMapIndex(i, rv)
		case reflect.String:
			return NilValue, NewStringError(expr, "type string does not support index operation for assignment")
		default:
			return NilValue, NewStringError(expr, "type "+v.Kind().String()+" does not support index operation")
		}
		return v, nil
	case *ast.SliceExpr:
		v, err := invokeExpr(lhs.Value, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		if v.Kind() == reflect.Interface {
			v = v.Elem()
		}
		switch v.Kind() {
		case reflect.Array, reflect.Slice:
			var rbi, rei int
			if lhs.Begin != nil {
				rb, err := invokeExpr(lhs.Begin, env)
				if err != nil {
					return NilValue, NewError(expr, err)
				}
				rbi, err = tryToInt(rb)
				if err != nil {
					return NilValue, NewStringError(expr, "index must be a number")
				}
				if rbi < 0 || rbi > v.Len() {
					return NilValue, NewStringError(expr, "index out of range")
				}
			} else {
				rbi = 0
			}
			if lhs.End != nil {
				re, err := invokeExpr(lhs.End, env)
				if err != nil {
					return NilValue, NewError(expr, err)
				}
				rei, err = tryToInt(re)
				if err != nil {
					return NilValue, NewStringError(expr, "index must be a number")
				}
				if rei < 0 || rei > v.Len() {
					return NilValue, NewStringError(expr, "index out of range")
				}
			} else {
				rei = v.Len()
			}
			if rbi > rei {
				return NilValue, NewStringError(expr, "invalid slice index")
			}
			v = v.Slice(rbi, rei)
			if !v.CanSet() {
				return NilValue, NewStringError(expr, "slice cannot be assigned")
			}
			v.Set(rv)
		case reflect.String:
			return NilValue, NewStringError(expr, "type string does not support slice operation for assignment")
		default:
			return NilValue, NewStringError(expr, "type "+v.Kind().String()+" does not support slice operation")
		}
		return v, nil
	}
	return NilValue, NewStringError(expr, "Invalid operation")
}

// invokeExpr evaluates one expression.
func invokeExpr(expr ast.Expr, env *Env) (reflect.Value, error) {
	switch e := expr.(type) {
	case *ast.NumberExpr:
		if strings.Contains(e.Lit, ".") || strings.Contains(e.Lit, "e") {
			v, err := strconv.ParseFloat(e.Lit, 64)
			if err != nil {
				return NilValue, NewError(expr, err)
			}
			return reflect.ValueOf(float64(v)), nil
		}
		var i int64
		var err error
		if strings.HasPrefix(e.Lit, "0x") {
			i, err = strconv.ParseInt(e.Lit[2:], 16, 64)
		} else {
			i, err = strconv.ParseInt(e.Lit, 10, 64)
		}
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		return reflect.ValueOf(i), nil
	case *ast.IdentExpr:
		return env.get(e.Lit)
	case *ast.StringExpr:
		return reflect.ValueOf(e.Lit), nil
	case *ast.ArrayExpr:
		a := make([]interface{}, len(e.Exprs))
		for i, expr := range e.Exprs {
			arg, err := invokeExpr(expr, env)
			if err != nil {
				return NilValue, NewError(expr, err)
			}
			a[i] = arg.Interface()
		}
		return reflect.ValueOf(a), nil
	case *ast.MapExpr:
		m := make(map[string]interface{}, len(e.MapExpr))
		for k, expr := range e.MapExpr {
			v, err := invokeExpr(expr, env)
			if err != nil {
				return NilValue, NewError(expr, err)
			}
			m[k] = v.Interface()
		}
		return reflect.ValueOf(m), nil
	case *ast.DerefExpr:
		v := NilValue
		var err error
		switch ee := e.Expr.(type) {
		case *ast.IdentExpr:
			v, err = env.get(ee.Lit)
			if err != nil {
				return v, err
			}
		case *ast.MemberExpr:
			v, err := invokeExpr(ee.Expr, env)
			if err != nil {
				return NilValue, NewError(expr, err)
			}
			if v.Kind() == reflect.Interface {
				v = v.Elem()
			}
			if v.Kind() == reflect.Slice {
				v = v.Index(0)
			}
			if v.IsValid() && v.CanInterface() {
				if vme, ok := v.Interface().(*Env); ok {
					m, err := vme.get(ee.Name)
					if !m.IsValid() || err != nil {
						return NilValue, NewStringError(expr, fmt.Sprintf("Invalid operation '%s'", ee.Name))
					}
					return m, nil
				}
			}

			m := v.MethodByName(ee.Name)
			if !m.IsValid() {
				if v.Kind() == reflect.Ptr {
					v = v.Elem()
				}
				if v.Kind() == reflect.Struct {
					field, found := v.Type().FieldByName(ee.Name)
					if !found {
						return NilValue, NewStringError(expr, "no member named '"+ee.Name+"' for struct")
					}
					return v.FieldByIndex(field.Index), nil
				} else if v.Kind() == reflect.Map {
					// From reflect MapIndex:
					// It returns the zero Value if key is not found in the map or if v represents a nil map.
					m = v.MapIndex(reflect.ValueOf(ee.Name))
				} else {
					return NilValue, NewStringError(expr, fmt.Sprintf("Invalid operation '%s'", ee.Name))
				}
				v = m
			} else {
				v = m
			}
		default:
			return NilValue, NewStringError(expr, "Invalid operation for the value")
		}
		if v.Kind() != reflect.Ptr {
			return NilValue, NewStringError(expr, "Cannot deference for the value")
		}
		return v.Elem(), nil
	case *ast.AddrExpr:
		v := NilValue
		var err error
		switch ee := e.Expr.(type) {
		case *ast.IdentExpr:
			v, err = env.get(ee.Lit)
			if err != nil {
				return v, err
			}
		case *ast.MemberExpr:
			v, err := invokeExpr(ee.Expr, env)
			if err != nil {
				return NilValue, NewError(expr, err)
			}
			if v.Kind() == reflect.Interface {
				v = v.Elem()
			}
			if v.Kind() == reflect.Slice {
				v = v.Index(0)
			}
			if v.IsValid() && v.CanInterface() {
				if vme, ok := v.Interface().(*Env); ok {
					m, err := vme.get(ee.Name)
					if !m.IsValid() || err != nil {
						return NilValue, NewStringError(expr, fmt.Sprintf("Invalid operation '%s'", ee.Name))
					}
					return m, nil
				}
			}

			m := v.MethodByName(ee.Name)
			if !m.IsValid() {
				if v.Kind() == reflect.Ptr {
					v = v.Elem()
				}
				if v.Kind() == reflect.Struct {
					m = v.FieldByName(ee.Name)
					if !m.IsValid() {
						return NilValue, NewStringError(expr, fmt.Sprintf("Invalid operation '%s'", ee.Name))
					}
				} else if v.Kind() == reflect.Map {
					// From reflect MapIndex:
					// It returns the zero Value if key is not found in the map or if v represents a nil map.
					m = v.MapIndex(reflect.ValueOf(ee.Name))
				} else {
					return NilValue, NewStringError(expr, fmt.Sprintf("Invalid operation '%s'", ee.Name))
				}
				v = m
			} else {
				v = m
			}
		default:
			return NilValue, NewStringError(expr, "Invalid operation for the value")
		}
		if !v.CanAddr() {
			i := v.Interface()
			return reflect.ValueOf(&i), nil
		}
		return v.Addr(), nil
	case *ast.UnaryExpr:
		v, err := invokeExpr(e.Expr, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		switch e.Operator {
		case "-":
			if v.Kind() == reflect.Int64 {
				return reflect.ValueOf(-v.Int()), nil
			}
			if v.Kind() == reflect.Float64 {
				return reflect.ValueOf(-v.Float()), nil
			}
			return reflect.ValueOf(-toFloat64(v)), nil
		case "^":
			return reflect.ValueOf(^toInt64(v)), nil
		case "!":
			return reflect.ValueOf(!toBool(v)), nil
		default:
			return NilValue, NewStringError(e, "Unknown operator ''")
		}
	case *ast.ParenExpr:
		v, err := invokeExpr(e.SubExpr, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		return v, nil
	case *ast.FuncExpr:
		f := reflect.ValueOf(func(expr *ast.FuncExpr, env *Env) Func {
			return func(args ...reflect.Value) (reflect.Value, error) {
				if !expr.VarArg {
					if len(args) != len(expr.Args) {
						return NilValue, NewStringError(expr, "expected "+fmt.Sprintf("%v", len(expr.Args))+" function arguments but received "+fmt.Sprintf("%v", len(args)))
					}
				}
				newenv := env.NewEnv()
				if expr.VarArg {
					newenv.defineValue(expr.Args[0], reflect.ValueOf(args))
				} else {
					for i, arg := range expr.Args {
						newenv.defineValue(arg, args[i])
					}
				}
				rr, err := run(expr.Stmts, newenv)
				if err == ReturnError {
					err = nil
				}
				return rr, err
			}
		}(e, env))
		env.defineValue(e.Name, f)
		return f, nil
	case *ast.MemberExpr:
		v, err := invokeExpr(e.Expr, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		if v.Kind() == reflect.Interface {
			v = v.Elem()
		}
		if v.Kind() == reflect.Slice {
			v = v.Index(0)
		}
		if v.IsValid() && v.CanInterface() {
			if vme, ok := v.Interface().(*Env); ok {
				m, err := vme.get(e.Name)
				if !m.IsValid() || err != nil {
					return NilValue, NewStringError(expr, fmt.Sprintf("Invalid operation '%s'", e.Name))
				}
				return m, nil
			}
		}

		method, found := v.Type().MethodByName(e.Name)
		if found {
			return v.Method(method.Index), nil
		}

		if v.Kind() == reflect.Ptr {
			v = v.Elem()
		}
		switch v.Kind() {
		case reflect.Struct:
			field, found := v.Type().FieldByName(e.Name)
			if !found {
				return NilValue, NewStringError(expr, "no member named '"+e.Name+"' for struct")
			}
			return v.FieldByIndex(field.Index), nil
		case reflect.Map:
			v = getMapIndex(reflect.ValueOf(e.Name), v)
			return v, nil
		default:
			return NilValue, NewStringError(expr, "type "+v.Kind().String()+" does not support member operation")
		}
	case *ast.ItemExpr:
		v, err := invokeExpr(e.Value, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		i, err := invokeExpr(e.Index, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		if v.Kind() == reflect.Interface {
			v = v.Elem()
		}
		switch v.Kind() {
		case reflect.Array, reflect.Slice, reflect.String:
			ii, err := tryToInt(i)
			if err != nil {
				return NilValue, NewStringError(expr, "index must be a number")
			}
			if ii < 0 || ii >= v.Len() {
				return NilValue, NewStringError(expr, "index out of range")
			}
			if v.Kind() != reflect.String {
				return v.Index(ii), nil
			}
			v = v.Index(ii)
			if v.Type().ConvertibleTo(StringType) {
				return v.Convert(StringType), nil
			} else {
				return NilValue, NewStringError(expr, "invalid type conversion")
			}
		case reflect.Map:
			v = getMapIndex(i, v)
			return v, nil
		default:
			return NilValue, NewStringError(expr, "type "+v.Kind().String()+" does not support index operation")
		}

	case *ast.SliceExpr:
		v, err := invokeExpr(e.Value, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		if v.Kind() == reflect.Interface {
			v = v.Elem()
		}
		switch v.Kind() {
		case reflect.Array, reflect.Slice, reflect.String:
			var rbi, rei int
			if e.Begin != nil {
				rb, err := invokeExpr(e.Begin, env)
				if err != nil {
					return NilValue, NewError(expr, err)
				}
				rbi, err = tryToInt(rb)
				if err != nil {
					return NilValue, NewStringError(expr, "index must be a number")
				}
				if rbi < 0 || rbi > v.Len() {
					return NilValue, NewStringError(expr, "index out of range")
				}
			} else {
				rbi = 0
			}
			if e.End != nil {
				re, err := invokeExpr(e.End, env)
				if err != nil {
					return NilValue, NewError(expr, err)
				}
				rei, err = tryToInt(re)
				if err != nil {
					return NilValue, NewStringError(expr, "index must be a number")
				}
				if rei < 0 || rei > v.Len() {
					return NilValue, NewStringError(expr, "index out of range")
				}
			} else {
				rei = v.Len()
			}
			if rbi > rei {
				return NilValue, NewStringError(expr, "invalid slice index")
			}
			return v.Slice(rbi, rei), nil
		default:
			return NilValue, NewStringError(expr, "type "+v.Kind().String()+" does not support slice operation")
		}
	case *ast.AssocExpr:
		switch e.Operator {
		case "++":
			if alhs, ok := e.Lhs.(*ast.IdentExpr); ok {
				v, err := env.get(alhs.Lit)
				if err != nil {
					return v, err
				}
				switch v.Kind() {
				case reflect.Float64, reflect.Float32:
					v = reflect.ValueOf(v.Float() + 1)
				case reflect.Int64, reflect.Int32, reflect.Int16, reflect.Int8, reflect.Int:
					v = reflect.ValueOf(v.Int() + 1)
				case reflect.Bool:
					if v.Bool() {
						v = reflect.ValueOf(int64(2))
					} else {
						v = reflect.ValueOf(int64(1))
					}
				default:
					v = reflect.ValueOf(toInt64(v) + 1)
				}
				err = env.setValue(alhs.Lit, v)
				if err != nil {
					return v, err
				}
				return v, nil
			}
		case "--":
			if alhs, ok := e.Lhs.(*ast.IdentExpr); ok {
				v, err := env.get(alhs.Lit)
				if err != nil {
					return v, err
				}
				switch v.Kind() {
				case reflect.Float64, reflect.Float32:
					v = reflect.ValueOf(v.Float() - 1)
				case reflect.Int64, reflect.Int32, reflect.Int16, reflect.Int8, reflect.Int:
					v = reflect.ValueOf(v.Int() - 1)
				case reflect.Bool:
					if v.Bool() {
						v = reflect.ValueOf(int64(0))
					} else {
						v = reflect.ValueOf(int64(-1))
					}
				default:
					v = reflect.ValueOf(toInt64(v) - 1)
				}
				err = env.setValue(alhs.Lit, v)
				if err != nil {
					return v, err
				}
				return v, nil
			}
		}

		if e.Rhs == nil {
			// TODO: Can this be fixed in the parser so that Rhs is not nil?
			e.Rhs = &ast.NumberExpr{Lit: "1"}
		}
		v, err := invokeExpr(&ast.BinOpExpr{Lhs: e.Lhs, Operator: e.Operator[0:1], Rhs: e.Rhs}, env)
		if err != nil {
			return v, err
		}
		if v.Kind() == reflect.Interface {
			v = v.Elem()
		}
		return invokeLetExpr(e.Lhs, v, env)
	case *ast.LetExpr:
		rv, err := invokeExpr(e.Rhs, env)
		if err != nil {
			return NilValue, NewError(e, err)
		}
		if rv.Kind() == reflect.Interface {
			rv = rv.Elem()
		}
		return invokeLetExpr(e.Lhs, rv, env)
	case *ast.LetsExpr:
		var err error
		rvs := make([]reflect.Value, len(e.Rhss))
		for i, rhs := range e.Rhss {
			rvs[i], err = invokeExpr(rhs, env)
			if err != nil {
				return NilValue, NewError(rhs, err)
			}
		}
		for i, lhs := range e.Lhss {
			if i >= len(rvs) {
				break
			}
			v := rvs[i]
			if v.IsValid() && v.Kind() == reflect.Interface && !v.IsNil() {
				v = v.Elem()
			}
			_, err = invokeLetExpr(lhs, v, env)
			if err != nil {
				return NilValue, NewError(lhs, err)
			}
		}
		return rvs[len(rvs)-1], nil
	case *ast.NewExpr:
		rt, err := env.Type(e.Type)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		return reflect.New(rt), nil
	case *ast.BinOpExpr:
		lhsV := NilValue
		rhsV := NilValue
		var err error

		lhsV, err = invokeExpr(e.Lhs, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		if lhsV.Kind() == reflect.Interface && !rhsV.IsNil() {
			lhsV = lhsV.Elem()
		}
		if e.Rhs != nil {
			rhsV, err = invokeExpr(e.Rhs, env)
			if err != nil {
				return NilValue, NewError(expr, err)
			}
			if rhsV.Kind() == reflect.Interface && !rhsV.IsNil() {
				rhsV = rhsV.Elem()
			}
		}
		switch e.Operator {
		case "+":
			if (lhsV.Kind() == reflect.Array || lhsV.Kind() == reflect.Slice) && (rhsV.Kind() != reflect.Array && rhsV.Kind() != reflect.Slice) {
				rhsT := rhsV.Type()
				lhsT := lhsV.Type().Elem()
				if lhsT.Kind() != rhsT.Kind() {
					if !rhsT.ConvertibleTo(lhsT) {
						return NilValue, NewStringError(expr, "invalid type conversion")
					}
					rhsV = rhsV.Convert(lhsT)
				}
				return reflect.Append(lhsV, rhsV), nil
			}
			if (lhsV.Kind() == reflect.Array || lhsV.Kind() == reflect.Slice) && (rhsV.Kind() == reflect.Array || rhsV.Kind() == reflect.Slice) {
				return appendSlice(e, lhsV, rhsV)
			}
			if lhsV.Kind() == reflect.String || rhsV.Kind() == reflect.String {
				return reflect.ValueOf(toString(lhsV) + toString(rhsV)), nil
			}
			if lhsV.Kind() == reflect.Float64 || rhsV.Kind() == reflect.Float64 {
				return reflect.ValueOf(toFloat64(lhsV) + toFloat64(rhsV)), nil
			}
			return reflect.ValueOf(toInt64(lhsV) + toInt64(rhsV)), nil
		case "-":
			if lhsV.Kind() == reflect.Float64 || rhsV.Kind() == reflect.Float64 {
				return reflect.ValueOf(toFloat64(lhsV) - toFloat64(rhsV)), nil
			}
			return reflect.ValueOf(toInt64(lhsV) - toInt64(rhsV)), nil
		case "*":
			if lhsV.Kind() == reflect.String && (rhsV.Kind() == reflect.Int || rhsV.Kind() == reflect.Int32 || rhsV.Kind() == reflect.Int64) {
				return reflect.ValueOf(strings.Repeat(toString(lhsV), int(toInt64(rhsV)))), nil
			}
			if lhsV.Kind() == reflect.Float64 || rhsV.Kind() == reflect.Float64 {
				return reflect.ValueOf(toFloat64(lhsV) * toFloat64(rhsV)), nil
			}
			return reflect.ValueOf(toInt64(lhsV) * toInt64(rhsV)), nil
		case "/":
			return reflect.ValueOf(toFloat64(lhsV) / toFloat64(rhsV)), nil
		case "%":
			return reflect.ValueOf(toInt64(lhsV) % toInt64(rhsV)), nil
		case "==":
			return reflect.ValueOf(equal(lhsV, rhsV)), nil
		case "!=":
			return reflect.ValueOf(equal(lhsV, rhsV) == false), nil
		case ">":
			return reflect.ValueOf(toFloat64(lhsV) > toFloat64(rhsV)), nil
		case ">=":
			return reflect.ValueOf(toFloat64(lhsV) >= toFloat64(rhsV)), nil
		case "<":
			return reflect.ValueOf(toFloat64(lhsV) < toFloat64(rhsV)), nil
		case "<=":
			return reflect.ValueOf(toFloat64(lhsV) <= toFloat64(rhsV)), nil
		case "|":
			return reflect.ValueOf(toInt64(lhsV) | toInt64(rhsV)), nil
		case "||":
			if toBool(lhsV) {
				return lhsV, nil
			}
			return rhsV, nil
		case "&":
			return reflect.ValueOf(toInt64(lhsV) & toInt64(rhsV)), nil
		case "&&":
			if toBool(lhsV) {
				return rhsV, nil
			}
			return lhsV, nil
		case "**":
			if lhsV.Kind() == reflect.Float64 {
				return reflect.ValueOf(math.Pow(lhsV.Float(), toFloat64(rhsV))), nil
			}
			return reflect.ValueOf(int64(math.Pow(toFloat64(lhsV), toFloat64(rhsV)))), nil
		case ">>":
			return reflect.ValueOf(toInt64(lhsV) >> uint64(toInt64(rhsV))), nil
		case "<<":
			return reflect.ValueOf(toInt64(lhsV) << uint64(toInt64(rhsV))), nil
		default:
			return NilValue, NewStringError(expr, "Unknown operator")
		}
	case *ast.ConstExpr:
		switch e.Value {
		case "true":
			return TrueValue, nil
		case "false":
			return FalseValue, nil
		}
		return NilValue, nil
	case *ast.AnonCallExpr:
		f, err := invokeExpr(e.Expr, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		if f.Kind() == reflect.Interface {
			f = f.Elem()
		}
		if f.Kind() != reflect.Func {
			return NilValue, NewStringError(expr, "can not call type "+f.Type().String())
		}
		return invokeExpr(&ast.CallExpr{Func: f, SubExprs: e.SubExprs, VarArg: e.VarArg, Go: e.Go}, env)
	case *ast.CallExpr:
		var err error
		f := NilValue

		if e.Func != nil {
			f = e.Func.(reflect.Value)
		} else {
			f, err = env.get(e.Name)
			if err != nil {
				return NilValue, err
			}
		}
		if f.Kind() != reflect.Func {
			return NilValue, NewStringError(expr, "can not call type "+f.Type().String())
		}
		_, isReflect := f.Interface().(Func)

		var arg reflect.Value
		args := []reflect.Value{}
		l := len(e.SubExprs)
		for i, expr := range e.SubExprs {

			arg, err = invokeExpr(expr, env)
			if err != nil {
				return NilValue, NewError(expr, err)
			}

			if i < f.Type().NumIn() {
				if !f.Type().IsVariadic() {
					iType := f.Type().In(i)
					if arg.Kind() == reflect.Interface && !arg.IsNil() {
						arg = arg.Elem()
					}
					if arg.Type() == UnsafePointerType {
						arg = reflect.New(iType).Elem()
					}
					if !arg.IsValid() {
						arg = reflect.Zero(iType)
					} else if arg.Kind() == reflect.Func {
						if _, isFunc := arg.Interface().(Func); isFunc {
							rfunc := arg
							arg = reflect.MakeFunc(iType, func(args []reflect.Value) []reflect.Value {
								for i := range args {
									args[i] = reflect.ValueOf(args[i])
								}
								if e.Go {
									go func() {
										rfunc.Call(args)
									}()
									return []reflect.Value{}
								}
								var rets []reflect.Value
								for _, v := range rfunc.Call(args)[:iType.NumOut()] {
									rets = append(rets, v.Interface().(reflect.Value))
								}
								return rets
							})
						}
					} else if iType != InterfaceType && arg.Type() != iType {
						if arg.Type().ConvertibleTo(iType) {
							arg = arg.Convert(iType)
						} else {
							return NilValue, NewStringError(expr, "argument type "+arg.Type().String()+" cannot be used for function argument type "+iType.String())
						}
					}
				}
			}

			if !arg.IsValid() {
				arg = NilValue
			}

			if isReflect {
				if arg.Kind() == reflect.Interface && !arg.IsNil() {
					arg = arg.Elem()
				}
				if e.VarArg && i == l-1 {
					for j := 0; j < arg.Len(); j++ {
						args = append(args, reflect.ValueOf(arg.Index(j).Elem()))
					}
				} else {
					args = append(args, reflect.ValueOf(arg))
				}
			} else {
				if e.VarArg && i == l-1 {
					for j := 0; j < arg.Len(); j++ {
						args = append(args, arg.Index(j).Elem())
					}
				} else {
					args = append(args, arg)
				}
			}

		}

		ret := NilValue
		fnc := func() {
			defer func() {
				if os.Getenv("ANKO_DEBUG") == "" {
					if ex := recover(); ex != nil {
						if e, ok := ex.(error); ok {
							err = e
						} else {
							err = errors.New(fmt.Sprint(ex))
						}
					}
				}
			}()
			if f.Kind() == reflect.Interface {
				f = f.Elem()
			}
			rets := f.Call(args)
			if isReflect {
				ev := rets[1].Interface()
				if ev != nil {
					err = ev.(error)
				}
				ret = rets[0].Interface().(reflect.Value)
			} else {
				for i, expr := range e.SubExprs {
					if ae, ok := expr.(*ast.AddrExpr); ok {
						if id, ok := ae.Expr.(*ast.IdentExpr); ok {
							invokeLetExpr(id, args[i].Elem().Elem(), env)
						}
					}
				}
				if f.Type().NumOut() == 0 {
				} else if f.Type().NumOut() == 1 {
					ret = rets[0]
				} else {
					var result []interface{}
					for _, r := range rets {
						result = append(result, r.Interface())
					}
					ret = reflect.ValueOf(result)
				}
			}
		}

		if e.Go {
			go fnc()
			return NilValue, nil
		}
		fnc()
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		return ret, nil
	case *ast.TernaryOpExpr:
		rv, err := invokeExpr(e.Expr, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		if toBool(rv) {
			lhsV, err := invokeExpr(e.Lhs, env)
			if err != nil {
				return NilValue, NewError(expr, err)
			}
			return lhsV, nil
		}
		rhsV, err := invokeExpr(e.Rhs, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		return rhsV, nil
	case *ast.MakeExpr:
		rt, err := env.Type(e.Type)
		if err != nil {
			return NilValue, NewError(expr, err)
		}
		if rt == nil {
			return NilValue, NewStringError(expr, fmt.Sprintf("invalid type for make"))
		}
		if rt.Kind() == reflect.Map {
			return reflect.MakeMap(reflect.MapOf(rt.Key(), rt.Elem())).Convert(rt), nil
		}
		return reflect.Zero(rt), nil
	case *ast.MakeChanExpr:
		typ, err := env.Type(e.Type)
		if err != nil {
			return NilValue, err
		}
		var size int
		if e.SizeExpr != nil {
			rv, err := invokeExpr(e.SizeExpr, env)
			if err != nil {
				return NilValue, err
			}
			size = int(toInt64(rv))
		}
		return func() (reflect.Value, error) {
			defer func() {
				if os.Getenv("ANKO_DEBUG") == "" {
					if ex := recover(); ex != nil {
						if e, ok := ex.(error); ok {
							err = e
						} else {
							err = errors.New(fmt.Sprint(ex))
						}
					}
				}
			}()
			return reflect.MakeChan(reflect.ChanOf(reflect.BothDir, typ), size), nil
		}()
	case *ast.MakeArrayExpr:
		typ, err := env.Type(e.Type)
		if err != nil {
			return NilValue, err
		}
		if typ == nil {
			return NilValue, NewStringError(expr, fmt.Sprintf("invalid type for make array"))
		}
		var alen int
		if e.LenExpr != nil {
			rv, err := invokeExpr(e.LenExpr, env)
			if err != nil {
				return NilValue, err
			}
			alen = toInt(rv)
		}
		var acap int
		if e.CapExpr != nil {
			rv, err := invokeExpr(e.CapExpr, env)
			if err != nil {
				return NilValue, err
			}
			acap = toInt(rv)
		} else {
			acap = alen
		}
		return reflect.MakeSlice(reflect.SliceOf(typ), alen, acap), nil
	case *ast.ChanExpr:
		rhs, err := invokeExpr(e.Rhs, env)
		if err != nil {
			return NilValue, NewError(expr, err)
		}

		if e.Lhs == nil {
			if rhs.Kind() == reflect.Chan {
				rv, _ := rhs.Recv()
				return rv, nil
			}
		} else {
			lhs, err := invokeExpr(e.Lhs, env)
			if err != nil {
				return NilValue, NewError(expr, err)
			}
			if lhs.Kind() == reflect.Chan {
				lhs.Send(rhs)
				return NilValue, nil
			} else if rhs.Kind() == reflect.Chan {
				rv, ok := rhs.Recv()
				if !ok {
					return NilValue, NewErrorf(expr, "Failed to send to channel")
				}
				return invokeLetExpr(e.Lhs, rv, env)
			}
		}
		return NilValue, NewStringError(expr, "Invalid operation for chan")
	default:
		return NilValue, NewStringError(expr, "Unknown expression")
	}
}
