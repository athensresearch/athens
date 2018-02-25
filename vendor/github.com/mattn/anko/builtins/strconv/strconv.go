// Package strconv implements strconv interface for anko script.
package strconv

import (
	pkg "strconv"

	"github.com/mattn/anko/vm"
)

func Import(env *vm.Env) *vm.Env {
	m := env.NewPackage("strconv")

	m.Define("FormatBool", pkg.FormatBool)
	m.Define("FormatFloat", pkg.FormatFloat)
	m.Define("FormatInt", pkg.FormatInt)
	m.Define("FormatUint", pkg.FormatUint)

	m.Define("ParseBool", pkg.ParseBool)
	m.Define("ParseFloat", pkg.ParseFloat)
	m.Define("ParseInt", pkg.ParseInt)
	m.Define("ParseUint", pkg.ParseUint)

	return m
}
