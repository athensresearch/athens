package ast

import (
	"bytes"
)

type ForExpression struct {
	TokenAble
	KeyName   string
	ValueName string
	Block     *BlockStatement
	Iterable  Expression
}

func (fe *ForExpression) expressionNode() {}

func (fe *ForExpression) String() string {
	var out bytes.Buffer
	out.WriteString("for (")
	out.WriteString(fe.KeyName)
	out.WriteString(", ")
	out.WriteString(fe.ValueName)
	out.WriteString(") in ")
	out.WriteString(fe.Iterable.String())
	out.WriteString(" { ")
	if fe.Block != nil {
		out.WriteString(fe.Block.String())
	}
	out.WriteString(" }")
	return out.String()
}
