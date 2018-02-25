package ast

import (
	"bytes"
)

type Identifier struct {
	TokenAble
	Callee *Identifier
	Value  string
}

func (i *Identifier) expressionNode() {}

func (i *Identifier) String() string {
	out := &bytes.Buffer{}
	if i.Callee != nil {
		out.WriteString(i.Callee.String())
		out.WriteString(".")
	}
	out.WriteString(i.Value)
	return out.String()
}
