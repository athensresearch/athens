package vm

import (
	"reflect"
	"strings"

	"github.com/mattn/anko/ast"
)

func invokeLetExpr(expr ast.Expr, rv reflect.Value, env *Env) (reflect.Value, error) {
	switch lhs := expr.(type) {
	case *ast.IdentExpr:
		if env.setValue(lhs.Lit, rv) != nil {
			if strings.Contains(lhs.Lit, ".") {
				return nilValue, newErrorf(expr, "Undefined symbol '%s'", lhs.Lit)
			}
			env.defineValue(lhs.Lit, rv)
		}
		return rv, nil
	case *ast.MemberExpr:
		v, err := invokeExpr(lhs.Expr, env)
		if err != nil {
			return nilValue, newError(expr, err)
		}

		if v.Kind() == reflect.Interface {
			v = v.Elem()
		}
		if v.Kind() == reflect.Slice {
			v = v.Index(0)
		}
		if !v.IsValid() {
			return nilValue, newStringError(expr, "type invalid does not support member operation")
		}
		if v.Kind() == reflect.Ptr {
			v = v.Elem()
		}
		if !v.IsValid() {
			return nilValue, newStringError(expr, "type invalid does not support member operation")
		}

		switch v.Kind() {
		case reflect.Struct:
			field, found := v.Type().FieldByName(lhs.Name)
			if !found {
				return nilValue, newStringError(expr, "no member named '"+lhs.Name+"' for struct")
			}
			v = v.FieldByIndex(field.Index)
			// From reflect CanSet:
			// A Value can be changed only if it is addressable and was not obtained by the use of unexported struct fields.
			// Often a struct has to be passed as a pointer to be set
			if !v.CanSet() {
				return nilValue, newStringError(expr, "struct member '"+lhs.Name+"' cannot be assigned")
			}

			if v.Type() == interfaceType || v.Type() == rv.Type() {
				v.Set(rv)
				return v, nil
			}

			if v.Kind() == reflect.Func && rv.Kind() == reflect.Func {
				rv, err = convertReflectValueToType(rv, v.Type())
				if err == nil {
					v.Set(rv)
					return v, nil
				}
			}

			return nilValue, newStringError(expr, "type "+rv.Type().String()+" cannot be assigned to type "+v.Type().String()+" for struct")

		case reflect.Map:
			if v.Type().Elem() != interfaceType && v.Type().Elem() != rv.Type() {
				return nilValue, newStringError(expr, "type "+rv.Type().String()+" cannot be assigned to type "+v.Type().Elem().String()+" for map")
			}
			if v.IsNil() {
				v = reflect.MakeMap(v.Type())
				v.SetMapIndex(reflect.ValueOf(lhs.Name), rv)
				return invokeLetExpr(lhs.Expr, v, env)
			}
			v.SetMapIndex(reflect.ValueOf(lhs.Name), rv)

		default:
			return nilValue, newStringError(expr, "type "+v.Kind().String()+" does not support member operation")
		}
		return v, nil

	case *ast.ItemExpr:
		v, err := invokeExpr(lhs.Value, env)
		if err != nil {
			return nilValue, newError(expr, err)
		}
		i, err := invokeExpr(lhs.Index, env)
		if err != nil {
			return nilValue, newError(expr, err)
		}
		if v.Kind() == reflect.Interface {
			v = v.Elem()
		}

		switch v.Kind() {
		case reflect.Slice, reflect.Array:
			ii, err := tryToInt(i)
			if err != nil {
				return nilValue, newStringError(expr, "index must be a number")
			}

			if ii == v.Len() {
				// try to do automatic append
				if v.Type().Elem() == rv.Type() {
					v = reflect.Append(v, rv)
					return invokeLetExpr(lhs.Value, v, env)
				}
				if rv.Type().ConvertibleTo(v.Type().Elem()) {
					v = reflect.Append(v, rv.Convert(v.Type().Elem()))
					return invokeLetExpr(lhs.Value, v, env)
				}
				if rv.Kind() != reflect.Slice && rv.Kind() != reflect.Array {
					return nilValue, newStringError(expr, "type "+rv.Type().String()+" cannot be assigned to type "+v.Type().Elem().String()+" for array index")
				}

				newSlice := reflect.MakeSlice(v.Type().Elem(), 0, rv.Len())
				newSlice, err = appendSlice(expr, newSlice, rv)
				if err != nil {
					return nilValue, err
				}
				v = reflect.Append(v, newSlice)
				return invokeLetExpr(lhs.Value, v, env)
			}

			if ii < 0 || ii >= v.Len() {
				return nilValue, newStringError(expr, "index out of range")
			}
			v = v.Index(ii)
			if !v.CanSet() {
				return nilValue, newStringError(expr, "index cannot be assigned")
			}

			if v.Type() == rv.Type() {
				v.Set(rv)
				return v, nil
			}
			if rv.Type().ConvertibleTo(v.Type()) {
				v.Set(rv.Convert(v.Type()))
				return v, nil
			}

			if rv.Kind() != reflect.Slice && rv.Kind() != reflect.Array {
				return nilValue, newStringError(expr, "type "+rv.Type().String()+" cannot be assigned to type "+v.Type().String()+" for array index")
			}

			newSlice := reflect.MakeSlice(v.Type(), 0, rv.Len())
			newSlice, err = appendSlice(expr, newSlice, rv)
			if err != nil {
				return nilValue, err
			}
			v.Set(newSlice)

		case reflect.Map:
			keyType := i.Type()
			if keyType == interfaceType && v.Type().Key() != interfaceType {
				if i.Elem().IsValid() && !i.Elem().IsNil() {
					keyType = i.Elem().Type()
				}
			}
			if keyType != v.Type().Key() {
				return nilValue, newStringError(expr, "index type "+keyType.String()+" cannot be used for map index type "+v.Type().Key().String())
			}

			if v.Type().Elem() != interfaceType && v.Type().Elem() != rv.Type() {
				return nilValue, newStringError(expr, "type "+rv.Type().String()+" cannot be assigned to type "+v.Type().Elem().String()+" for map")
			}
			if v.IsNil() {
				v = reflect.MakeMap(v.Type())
				v.SetMapIndex(i, rv)
				return invokeLetExpr(lhs.Value, v, env)
			}
			v.SetMapIndex(i, rv)

		case reflect.String:
			return nilValue, newStringError(expr, "type string does not support index operation for assignment")
		default:
			return nilValue, newStringError(expr, "type "+v.Kind().String()+" does not support index operation")
		}
		return v, nil
	case *ast.SliceExpr:
		v, err := invokeExpr(lhs.Value, env)
		if err != nil {
			return nilValue, newError(expr, err)
		}
		if v.Kind() == reflect.Interface {
			v = v.Elem()
		}
		switch v.Kind() {
		case reflect.Slice, reflect.Array:
			var rbi, rei int
			if lhs.Begin != nil {
				rb, err := invokeExpr(lhs.Begin, env)
				if err != nil {
					return nilValue, newError(expr, err)
				}
				rbi, err = tryToInt(rb)
				if err != nil {
					return nilValue, newStringError(expr, "index must be a number")
				}
				if rbi < 0 || rbi > v.Len() {
					return nilValue, newStringError(expr, "index out of range")
				}
			} else {
				rbi = 0
			}
			if lhs.End != nil {
				re, err := invokeExpr(lhs.End, env)
				if err != nil {
					return nilValue, newError(expr, err)
				}
				rei, err = tryToInt(re)
				if err != nil {
					return nilValue, newStringError(expr, "index must be a number")
				}
				if rei < 0 || rei > v.Len() {
					return nilValue, newStringError(expr, "index out of range")
				}
			} else {
				rei = v.Len()
			}
			if rbi > rei {
				return nilValue, newStringError(expr, "invalid slice index")
			}
			v = v.Slice(rbi, rei)
			if !v.CanSet() {
				return nilValue, newStringError(expr, "slice cannot be assigned")
			}
			v.Set(rv)
		case reflect.String:
			return nilValue, newStringError(expr, "type string does not support slice operation for assignment")
		default:
			return nilValue, newStringError(expr, "type "+v.Kind().String()+" does not support slice operation")
		}
		return v, nil
	case *ast.DerefExpr:
		v, err := invokeExpr(lhs.Expr, env)
		if err != nil {
			return nilValue, newError(expr, err)
		}
		v.Elem().Set(rv)
		return v, nil
	}
	return nilValue, newStringError(expr, "Invalid operation")
}
