package vm

import (
	"fmt"
	"math"
	"reflect"
	"strconv"
	"strings"

	"github.com/mattn/anko/ast"
)

// invokeExpr evaluates one expression.
func invokeExpr(expr ast.Expr, env *Env) (reflect.Value, error) {
	switch e := expr.(type) {

	case *ast.NumberExpr:
		if strings.Contains(e.Lit, ".") || strings.Contains(e.Lit, "e") {
			v, err := strconv.ParseFloat(e.Lit, 64)
			if err != nil {
				return nilValue, newError(e, err)
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
			return nilValue, newError(e, err)
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
				return nilValue, newError(expr, err)
			}
			a[i] = arg.Interface()
		}
		return reflect.ValueOf(a), nil

	case *ast.MapExpr:
		m := make(map[string]interface{}, len(e.MapExpr))
		for k, expr := range e.MapExpr {
			v, err := invokeExpr(expr, env)
			if err != nil {
				return nilValue, newError(expr, err)
			}
			m[k] = v.Interface()
		}
		return reflect.ValueOf(m), nil

	case *ast.DerefExpr:
		v := nilValue
		var err error
		switch ee := e.Expr.(type) {

		case *ast.IdentExpr:
			v, err = env.get(ee.Lit)
			if err != nil {
				return nilValue, newError(e, err)
			}

		case *ast.MemberExpr:
			v, err := invokeExpr(ee.Expr, env)
			if err != nil {
				return nilValue, newError(ee.Expr, err)
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
						return nilValue, newStringError(e, fmt.Sprintf("Invalid operation '%s'", ee.Name))
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
						return nilValue, newStringError(e, "no member named '"+ee.Name+"' for struct")
					}
					return v.FieldByIndex(field.Index), nil
				} else if v.Kind() == reflect.Map {
					// From reflect MapIndex:
					// It returns the zero Value if key is not found in the map or if v represents a nil map.
					m = v.MapIndex(reflect.ValueOf(ee.Name))
				} else {
					return nilValue, newStringError(e, fmt.Sprintf("Invalid operation '%s'", ee.Name))
				}
				v = m
			} else {
				v = m
			}
		default:
			return nilValue, newStringError(e, "Invalid operation for the value")
		}
		if v.Kind() != reflect.Ptr {
			return nilValue, newStringError(e, "Cannot deference for the value")
		}
		return v.Elem(), nil

	case *ast.AddrExpr:
		v := nilValue
		var err error
		switch ee := e.Expr.(type) {

		case *ast.IdentExpr:
			v, err = env.get(ee.Lit)
			if err != nil {
				return nilValue, newError(e, err)
			}

		case *ast.MemberExpr:
			v, err := invokeExpr(ee.Expr, env)
			if err != nil {
				return nilValue, newError(ee.Expr, err)
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
						return nilValue, newStringError(e, fmt.Sprintf("Invalid operation '%s'", ee.Name))
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
						return nilValue, newStringError(e, fmt.Sprintf("Invalid operation '%s'", ee.Name))
					}
				} else if v.Kind() == reflect.Map {
					// From reflect MapIndex:
					// It returns the zero Value if key is not found in the map or if v represents a nil map.
					m = v.MapIndex(reflect.ValueOf(ee.Name))
				} else {
					return nilValue, newStringError(e, fmt.Sprintf("Invalid operation '%s'", ee.Name))
				}
				v = m
			} else {
				v = m
			}
		default:
			return nilValue, newStringError(e, "Invalid operation for the value")
		}
		if !v.CanAddr() {
			i := v.Interface()
			return reflect.ValueOf(&i), nil
		}
		return v.Addr(), nil

	case *ast.UnaryExpr:
		v, err := invokeExpr(e.Expr, env)
		if err != nil {
			return nilValue, newError(e.Expr, err)
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
			return nilValue, newStringError(e, "Unknown operator ''")
		}

	case *ast.ParenExpr:
		v, err := invokeExpr(e.SubExpr, env)
		if err != nil {
			return nilValue, newError(e.SubExpr, err)
		}
		return v, nil

	case *ast.MemberExpr:
		v, err := invokeExpr(e.Expr, env)
		if err != nil {
			return nilValue, newError(e.Expr, err)
		}
		if v.Kind() == reflect.Interface {
			v = v.Elem()
		}
		if v.Kind() == reflect.Slice {
			v = v.Index(0)
		}
		if !v.IsValid() {
			return nilValue, newStringError(e, "type invalid does not support member operation")
		}
		if v.IsValid() && v.CanInterface() {
			if vme, ok := v.Interface().(*Env); ok {
				m, err := vme.get(e.Name)
				if !m.IsValid() || err != nil {
					return nilValue, newStringError(e, fmt.Sprintf("Invalid operation '%s'", e.Name))
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
				return nilValue, newStringError(e, "no member named '"+e.Name+"' for struct")
			}
			return v.FieldByIndex(field.Index), nil
		case reflect.Map:
			v = getMapIndex(reflect.ValueOf(e.Name), v)
			return v, nil
		default:
			return nilValue, newStringError(e, "type "+v.Kind().String()+" does not support member operation")
		}

	case *ast.ItemExpr:
		v, err := invokeExpr(e.Value, env)
		if err != nil {
			return nilValue, newError(e.Value, err)
		}
		i, err := invokeExpr(e.Index, env)
		if err != nil {
			return nilValue, newError(e.Index, err)
		}
		if v.Kind() == reflect.Interface {
			v = v.Elem()
		}
		switch v.Kind() {
		case reflect.String, reflect.Slice, reflect.Array:
			ii, err := tryToInt(i)
			if err != nil {
				return nilValue, newStringError(e, "index must be a number")
			}
			if ii < 0 || ii >= v.Len() {
				return nilValue, newStringError(e, "index out of range")
			}
			if v.Kind() != reflect.String {
				return v.Index(ii), nil
			}
			v = v.Index(ii)
			if v.Type().ConvertibleTo(stringType) {
				return v.Convert(stringType), nil
			}
			return nilValue, newStringError(e, "invalid type conversion")
		case reflect.Map:
			v = getMapIndex(i, v)
			return v, nil
		default:
			return nilValue, newStringError(e, "type "+v.Kind().String()+" does not support index operation")
		}

	case *ast.SliceExpr:
		v, err := invokeExpr(e.Value, env)
		if err != nil {
			return nilValue, newError(e.Value, err)
		}
		if v.Kind() == reflect.Interface {
			v = v.Elem()
		}
		switch v.Kind() {
		case reflect.String, reflect.Slice, reflect.Array:
			var rbi, rei int
			if e.Begin != nil {
				rb, err := invokeExpr(e.Begin, env)
				if err != nil {
					return nilValue, newError(e.Begin, err)
				}
				rbi, err = tryToInt(rb)
				if err != nil {
					return nilValue, newStringError(e, "index must be a number")
				}
				if rbi < 0 || rbi > v.Len() {
					return nilValue, newStringError(e, "index out of range")
				}
			} else {
				rbi = 0
			}
			if e.End != nil {
				re, err := invokeExpr(e.End, env)
				if err != nil {
					return nilValue, newError(e.End, err)
				}
				rei, err = tryToInt(re)
				if err != nil {
					return nilValue, newStringError(e, "index must be a number")
				}
				if rei < 0 || rei > v.Len() {
					return nilValue, newStringError(e, "index out of range")
				}
			} else {
				rei = v.Len()
			}
			if rbi > rei {
				return nilValue, newStringError(e, "invalid slice index")
			}
			return v.Slice(rbi, rei), nil
		default:
			return nilValue, newStringError(e, "type "+v.Kind().String()+" does not support slice operation")
		}

	case *ast.AssocExpr:
		switch e.Operator {
		case "++":
			if alhs, ok := e.Lhs.(*ast.IdentExpr); ok {
				v, err := env.get(alhs.Lit)
				if err != nil {
					return nilValue, newError(e, err)
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
					return nilValue, newError(e, err)
				}
				return v, nil
			}
		case "--":
			if alhs, ok := e.Lhs.(*ast.IdentExpr); ok {
				v, err := env.get(alhs.Lit)
				if err != nil {
					return nilValue, newError(e, err)
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
					return nilValue, newError(e, err)
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
			return nilValue, newError(e, err)
		}
		if v.Kind() == reflect.Interface {
			v = v.Elem()
		}
		return invokeLetExpr(e.Lhs, v, env)

	case *ast.LetExpr:
		rv, err := invokeExpr(e.Rhs, env)
		if err != nil {
			return nilValue, newError(e.Rhs, err)
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
				return nilValue, newError(rhs, err)
			}
		}
		for i, lhs := range e.Lhss {
			if i >= len(rvs) {
				break
			}
			v := rvs[i]
			if v.Kind() == reflect.Interface && !v.IsNil() {
				v = v.Elem()
			}
			_, err = invokeLetExpr(lhs, v, env)
			if err != nil {
				return nilValue, newError(lhs, err)
			}
		}
		return rvs[len(rvs)-1], nil

	case *ast.BinOpExpr:
		lhsV := nilValue
		rhsV := nilValue
		var err error

		lhsV, err = invokeExpr(e.Lhs, env)
		if err != nil {
			return nilValue, newError(e.Lhs, err)
		}
		if lhsV.Kind() == reflect.Interface && !lhsV.IsNil() {
			lhsV = lhsV.Elem()
		}
		if e.Rhs != nil {
			rhsV, err = invokeExpr(e.Rhs, env)
			if err != nil {
				return nilValue, newError(e.Rhs, err)
			}
			if rhsV.Kind() == reflect.Interface && !rhsV.IsNil() {
				rhsV = rhsV.Elem()
			}
		}
		switch e.Operator {
		case "+":
			if (lhsV.Kind() == reflect.Slice || lhsV.Kind() == reflect.Array) && (rhsV.Kind() != reflect.Slice && rhsV.Kind() != reflect.Array) {
				rhsT := rhsV.Type()
				lhsT := lhsV.Type().Elem()
				if lhsT.Kind() != rhsT.Kind() {
					if !rhsT.ConvertibleTo(lhsT) {
						return nilValue, newStringError(e, "invalid type conversion")
					}
					rhsV = rhsV.Convert(lhsT)
				}
				return reflect.Append(lhsV, rhsV), nil
			}
			if (lhsV.Kind() == reflect.Slice || lhsV.Kind() == reflect.Array) && (rhsV.Kind() == reflect.Slice || rhsV.Kind() == reflect.Array) {
				return appendSlice(expr, lhsV, rhsV)
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
			return nilValue, newStringError(e, "Unknown operator")
		}

	case *ast.ConstExpr:
		switch e.Value {
		case "true":
			return trueValue, nil
		case "false":
			return falseValue, nil
		}
		return nilValue, nil

	case *ast.TernaryOpExpr:
		rv, err := invokeExpr(e.Expr, env)
		if err != nil {
			return nilValue, newError(e.Expr, err)
		}
		if toBool(rv) {
			lhsV, err := invokeExpr(e.Lhs, env)
			if err != nil {
				return nilValue, newError(e.Lhs, err)
			}
			return lhsV, nil
		}
		rhsV, err := invokeExpr(e.Rhs, env)
		if err != nil {
			return nilValue, newError(e.Rhs, err)
		}
		return rhsV, nil

	case *ast.LenExpr:
		rv, err := invokeExpr(e.Expr, env)
		if err != nil {
			return nilValue, newError(e.Expr, err)
		}

		if rv.Kind() == reflect.Interface && !rv.IsNil() {
			rv = rv.Elem()
		}

		switch rv.Kind() {
		case reflect.Slice, reflect.Array, reflect.Map, reflect.String, reflect.Chan:
			return reflect.ValueOf(int64(rv.Len())), nil
		}
		return nilValue, newStringError(e, "type "+rv.Kind().String()+" does not support len operation")

	case *ast.NewExpr:
		t, err := getTypeFromString(env, e.Type)
		if err != nil {
			return nilValue, newError(e, err)
		}
		if t == nil {
			return nilValue, newErrorf(expr, "type cannot be nil for new")
		}

		return reflect.New(t), nil

	case *ast.MakeExpr:
		t, err := getTypeFromString(env, e.Type)
		if err != nil {
			return nilValue, newError(e, err)
		}
		if t == nil {
			return nilValue, newErrorf(expr, "type cannot be nil for make")
		}

		for i := 1; i < e.Dimensions; i++ {
			t = reflect.SliceOf(t)
		}
		if e.Dimensions < 1 {
			v, err := makeValue(t)
			if err != nil {
				return nilValue, newError(e, err)
			}
			return v, nil
		}

		var alen int
		if e.LenExpr != nil {
			rv, err := invokeExpr(e.LenExpr, env)
			if err != nil {
				return nilValue, newError(e.LenExpr, err)
			}
			alen = toInt(rv)
		}

		var acap int
		if e.CapExpr != nil {
			rv, err := invokeExpr(e.CapExpr, env)
			if err != nil {
				return nilValue, newError(e.CapExpr, err)
			}
			acap = toInt(rv)
		} else {
			acap = alen
		}

		return reflect.MakeSlice(reflect.SliceOf(t), alen, acap), nil

	case *ast.MakeTypeExpr:
		rv, err := invokeExpr(e.Type, env)
		if err != nil {
			return nilValue, newError(e, err)
		}
		if !rv.IsValid() || rv.Type() == nil {
			return nilValue, newErrorf(expr, "type cannot be nil for make type")
		}

		// if e.Name has a dot in it, it should give a syntax error
		// so no needs to check err
		env.DefineReflectType(e.Name, rv.Type())

		return reflect.ValueOf(rv.Type()), nil

	case *ast.MakeChanExpr:
		t, err := getTypeFromString(env, e.Type)
		if err != nil {
			return nilValue, newError(e, err)
		}
		if t == nil {
			return nilValue, newErrorf(expr, "type cannot be nil for make chan")
		}

		var size int
		if e.SizeExpr != nil {
			rv, err := invokeExpr(e.SizeExpr, env)
			if err != nil {
				return nilValue, newError(e.SizeExpr, err)
			}
			size = int(toInt64(rv))
		}

		return reflect.MakeChan(reflect.ChanOf(reflect.BothDir, t), size), nil

	case *ast.ChanExpr:
		rhs, err := invokeExpr(e.Rhs, env)
		if err != nil {
			return nilValue, newError(e.Rhs, err)
		}

		if e.Lhs == nil {
			if rhs.Kind() == reflect.Chan {
				rv, _ := rhs.Recv()
				return rv, nil
			}
		} else {
			lhs, err := invokeExpr(e.Lhs, env)
			if err != nil {
				return nilValue, newError(e.Lhs, err)
			}
			if lhs.Kind() == reflect.Chan {
				lhs.Send(rhs)
				return nilValue, nil
			} else if rhs.Kind() == reflect.Chan {
				rv, ok := rhs.Recv()
				if !ok {
					return nilValue, newErrorf(expr, "Failed to send to channel")
				}
				return invokeLetExpr(e.Lhs, rv, env)
			}
		}

		return nilValue, newStringError(e, "Invalid operation for chan")

	case *ast.FuncExpr:
		return funcExpr(e, env)

	case *ast.AnonCallExpr:
		return anonCallExpr(e, env)

	case *ast.CallExpr:
		return callExpr(e, env)

	case *ast.DeleteExpr:
		mapExpr, err := invokeExpr(e.MapExpr, env)
		if err != nil {
			return nilValue, newError(e.MapExpr, err)
		}
		keyExpr, err := invokeExpr(e.KeyExpr, env)
		if err != nil {
			return nilValue, newError(e.KeyExpr, err)
		}

		if mapExpr.Kind() != reflect.Map {
			return nilValue, newStringError(e, "first argument to delete must be map; have "+mapExpr.Kind().String())
		}
		if mapExpr.IsNil() {
			return nilValue, nil
		}
		if mapExpr.Type().Key() != keyExpr.Type() {
			keyExpr, err = convertReflectValueToType(keyExpr, mapExpr.Type().Key())
			if err != nil {
				return nilValue, newStringError(e, "cannot use type "+mapExpr.Type().Key().String()+" as type "+keyExpr.Type().String()+" in delete")
			}
		}

		mapExpr.SetMapIndex(keyExpr, reflect.Value{})
		return nilValue, nil

	default:
		return nilValue, newStringError(e, "Unknown expression")
	}
}
