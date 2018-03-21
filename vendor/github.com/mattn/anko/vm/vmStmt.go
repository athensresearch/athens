package vm

import (
	"fmt"
	"reflect"

	"github.com/mattn/anko/ast"
)

// Run executes statements in the specified environment.
func Run(stmts []ast.Stmt, env *Env) (interface{}, error) {
	rv, err := run(stmts, env)
	if err == ReturnError {
		err = nil
	}
	if !rv.IsValid() || !rv.CanInterface() {
		return nil, err
	}
	return rv.Interface(), err
}

// run executes statements in the specified environment.
func run(stmts []ast.Stmt, env *Env) (reflect.Value, error) {
	rv := nilValue
	var err error
	for _, stmt := range stmts {
		switch stmt.(type) {
		case *ast.BreakStmt:
			return nilValue, BreakError
		case *ast.ContinueStmt:
			return nilValue, ContinueError
		case *ast.ReturnStmt:
			rv, err = runSingleStmt(stmt, env)
			if err != nil {
				return rv, err
			}
			return rv, ReturnError
		default:
			rv, err = runSingleStmt(stmt, env)
			if err != nil {
				return rv, err
			}
		}
	}
	return rv, nil
}

// RunSingleStmt executes one statement in the specified environment.
func RunSingleStmt(stmt ast.Stmt, env *Env) (interface{}, error) {
	rv, err := runSingleStmt(stmt, env)
	if !rv.IsValid() || !rv.CanInterface() {
		return nil, err
	}
	return rv.Interface(), err
}

// runSingleStmt executes one statement in the specified environment.
func runSingleStmt(stmt ast.Stmt, env *Env) (reflect.Value, error) {
	if *(env.interrupt) {
		return nilValue, InterruptError
	}
	switch stmt := stmt.(type) {
	case *ast.ExprStmt:
		rv, err := invokeExpr(stmt.Expr, env)
		if err != nil {
			return rv, newError(stmt, err)
		}
		return rv, nil
	case *ast.VarStmt:
		var err error
		rvs := make([]reflect.Value, len(stmt.Exprs))
		for i, expr := range stmt.Exprs {
			rvs[i], err = invokeExpr(expr, env)
			if err != nil {
				return nilValue, newError(expr, err)
			}
		}
		for i, name := range stmt.Names {
			if i >= len(rvs) {
				break
			}
			env.defineValue(name, rvs[i])
		}
		return rvs[len(rvs)-1], nil
	case *ast.LetsStmt:
		var err error
		rvs := make([]reflect.Value, len(stmt.Rhss))
		for i, rhs := range stmt.Rhss {
			rvs[i], err = invokeExpr(rhs, env)
			if err != nil {
				return nilValue, newError(rhs, err)
			}
		}
		if len(stmt.Lhss) > 1 && len(rvs) == 1 {
			v := rvs[0]
			if v.IsValid() && v.Kind() == reflect.Interface && !v.IsNil() {
				v = v.Elem()
			}
			if v.IsValid() && v.Kind() == reflect.Slice {
				rvs = make([]reflect.Value, v.Len())
				for i := 0; i < v.Len(); i++ {
					rvs[i] = v.Index(i)
				}
			}
		}
		for i, lhs := range stmt.Lhss {
			if i >= len(rvs) {
				break
			}
			v := rvs[i]
			if v.IsValid() && v.Kind() == reflect.Interface && !v.IsNil() {
				v = v.Elem()
			}
			_, err = invokeLetExpr(lhs, v, env)
			if err != nil {
				return nilValue, newError(lhs, err)
			}
		}
		return rvs[len(rvs)-1], nil
	case *ast.IfStmt:
		// If
		rv, err := invokeExpr(stmt.If, env)
		if err != nil {
			return rv, newError(stmt, err)
		}
		if toBool(rv) {
			// Then
			newenv := env.NewEnv()
			defer newenv.Destroy()
			rv, err = run(stmt.Then, newenv)
			if err != nil {
				return rv, newError(stmt, err)
			}
			return rv, nil
		}
		done := false
		if len(stmt.ElseIf) > 0 {
			for _, stmt := range stmt.ElseIf {
				stmt_if := stmt.(*ast.IfStmt)
				// ElseIf
				rv, err = invokeExpr(stmt_if.If, env)
				if err != nil {
					return rv, newError(stmt, err)
				}
				if !toBool(rv) {
					continue
				}
				// ElseIf Then
				done = true
				rv, err = run(stmt_if.Then, env)
				if err != nil {
					return rv, newError(stmt, err)
				}
				break
			}
		}
		if !done && len(stmt.Else) > 0 {
			// Else
			newenv := env.NewEnv()
			defer newenv.Destroy()
			rv, err = run(stmt.Else, newenv)
			if err != nil {
				return rv, newError(stmt, err)
			}
		}
		return rv, nil
	case *ast.TryStmt:
		newenv := env.NewEnv()
		defer newenv.Destroy()
		_, err := run(stmt.Try, newenv)
		if err != nil {
			// Catch
			cenv := env.NewEnv()
			defer cenv.Destroy()
			if stmt.Var != "" {
				cenv.defineValue(stmt.Var, reflect.ValueOf(err))
			}
			_, e1 := run(stmt.Catch, cenv)
			if e1 != nil {
				err = newError(stmt.Catch[0], e1)
			} else {
				err = nil
			}
		}
		if len(stmt.Finally) > 0 {
			// Finally
			fenv := env.NewEnv()
			defer fenv.Destroy()
			_, e2 := run(stmt.Finally, newenv)
			if e2 != nil {
				err = newError(stmt.Finally[0], e2)
			}
		}
		return nilValue, newError(stmt, err)
	case *ast.LoopStmt:
		newenv := env.NewEnv()
		defer newenv.Destroy()
		for {
			if *(env.interrupt) {
				return nilValue, InterruptError
			}
			if stmt.Expr != nil {
				ev, ee := invokeExpr(stmt.Expr, newenv)
				if ee != nil {
					return ev, ee
				}
				if !toBool(ev) {
					break
				}
			}

			rv, err := run(stmt.Stmts, newenv)
			if err != nil && err != ContinueError {
				if err == BreakError {
					break
				}
				if err == ReturnError {
					return rv, err
				}
				return nilValue, newError(stmt, err)
			}
		}
		return nilValue, nil
	case *ast.ForStmt:
		val, ee := invokeExpr(stmt.Value, env)
		if ee != nil {
			return val, ee
		}
		if val.Kind() == reflect.Interface && !val.IsNil() {
			val = val.Elem()
		}
		switch val.Kind() {
		case reflect.Slice, reflect.Array:
			newenv := env.NewEnv()
			defer newenv.Destroy()

			for i := 0; i < val.Len(); i++ {
				if *(env.interrupt) {
					return nilValue, InterruptError
				}
				iv := val.Index(i)
				if iv.Kind() == reflect.Ptr || (iv.Kind() == reflect.Interface && !iv.IsNil()) {
					iv = iv.Elem()
				}
				newenv.defineValue(stmt.Vars[0], iv)
				rv, err := run(stmt.Stmts, newenv)
				if err != nil && err != ContinueError {
					if err == BreakError {
						break
					}
					if err == ReturnError {
						return rv, err
					}
					return nilValue, newError(stmt, err)
				}
			}
			return nilValue, nil
		case reflect.Map:
			newenv := env.NewEnv()
			defer newenv.Destroy()

			keys := val.MapKeys()
			for i := 0; i < len(keys); i++ {
				if *(env.interrupt) {
					return nilValue, InterruptError
				}
				newenv.defineValue(stmt.Vars[0], keys[i])
				if len(stmt.Vars) > 1 {
					newenv.defineValue(stmt.Vars[1], val.MapIndex(keys[i]))
				}
				rv, err := run(stmt.Stmts, newenv)
				if err != nil && err != ContinueError {
					if err == BreakError {
						break
					}
					if err == ReturnError {
						return rv, err
					}
					return nilValue, newError(stmt, err)
				}
			}
			return nilValue, nil
		case reflect.Chan:
			newenv := env.NewEnv()
			defer newenv.Destroy()

			for {
				if *(env.interrupt) {
					return nilValue, InterruptError
				}
				iv, ok := val.Recv()
				if !ok {
					break
				}
				if iv.Kind() == reflect.Interface || iv.Kind() == reflect.Ptr {
					iv = iv.Elem()
				}
				newenv.defineValue(stmt.Vars[0], iv)
				rv, err := run(stmt.Stmts, newenv)
				if err != nil && err != ContinueError {
					if err == BreakError {
						break
					}
					if err == ReturnError {
						return rv, err
					}
					return nilValue, newError(stmt, err)
				}
			}
			return nilValue, nil
		default:
			return nilValue, newStringError(stmt, "for cannot loop over type "+val.Kind().String())
		}
	case *ast.CForStmt:
		newenv := env.NewEnv()
		defer newenv.Destroy()
		_, err := invokeExpr(stmt.Expr1, newenv)
		if err != nil {
			return nilValue, err
		}
		for {
			if *(env.interrupt) {
				return nilValue, InterruptError
			}
			fb, err := invokeExpr(stmt.Expr2, newenv)
			if err != nil {
				return nilValue, err
			}
			if !toBool(fb) {
				break
			}

			rv, err := run(stmt.Stmts, newenv)
			if err != nil && err != ContinueError {
				if err == BreakError {
					break
				}
				if err == ReturnError {
					return rv, err
				}
				return nilValue, newError(stmt, err)
			}
			_, err = invokeExpr(stmt.Expr3, newenv)
			if err != nil {
				return nilValue, err
			}
		}
		return nilValue, nil
	case *ast.ReturnStmt:
		var err error
		rv := nilValue
		switch len(stmt.Exprs) {
		case 0:
			return rv, nil
		case 1:
			rv, err = invokeExpr(stmt.Exprs[0], env)
			if err != nil {
				return rv, newError(stmt, err)
			}
			return rv, nil
		}
		rvs := make([]interface{}, len(stmt.Exprs))
		for i, expr := range stmt.Exprs {
			rv, err = invokeExpr(expr, env)
			if err != nil {
				return rv, newError(stmt, err)
			}
			if !rv.IsValid() || !rv.CanInterface() {
				rvs[i] = nil
			} else {
				rvs[i] = rv.Interface()
			}
		}
		return reflect.ValueOf(rvs), nil
	case *ast.ThrowStmt:
		rv, err := invokeExpr(stmt.Expr, env)
		if err != nil {
			return rv, newError(stmt, err)
		}
		if !rv.IsValid() {
			return nilValue, newError(stmt, err)
		}
		return rv, newStringError(stmt, fmt.Sprint(rv.Interface()))
	case *ast.ModuleStmt:
		newenv := env.NewEnv()
		newenv.SetName(stmt.Name)
		rv, err := run(stmt.Stmts, newenv)
		if err != nil {
			return rv, newError(stmt, err)
		}
		env.defineGlobalValue(stmt.Name, reflect.ValueOf(newenv))
		return rv, nil
	case *ast.SwitchStmt:
		rv, err := invokeExpr(stmt.Expr, env)
		if err != nil {
			return rv, newError(stmt, err)
		}
		done := false
		var default_stmt *ast.DefaultStmt
		for _, ss := range stmt.Cases {
			if ssd, ok := ss.(*ast.DefaultStmt); ok {
				default_stmt = ssd
				continue
			}
			case_stmt := ss.(*ast.CaseStmt)
			cv, err := invokeExpr(case_stmt.Expr, env)
			if err != nil {
				return nilValue, newError(stmt, err)
			}
			if !equal(rv, cv) {
				continue
			}
			rv, err = run(case_stmt.Stmts, env)
			if err != nil {
				return rv, newError(stmt, err)
			}
			done = true
			break
		}
		if !done && default_stmt != nil {
			rv, err = run(default_stmt.Stmts, env)
			if err != nil {
				return rv, newError(stmt, err)
			}
		}
		return rv, nil
	default:
		return nilValue, newStringError(stmt, "unknown statement")
	}
}
