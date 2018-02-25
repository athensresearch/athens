package ast

import (
	"bytes"
)

type InfixExpression struct {
	TokenAble
	Left     Expression
	Operator string
	Right    Expression
}

func (oe *InfixExpression) expressionNode() {}

func (oe *InfixExpression) String() string {
	var out bytes.Buffer

	out.WriteString("(")
	if oe.Left != nil {
		out.WriteString(oe.Left.String())
	}
	out.WriteString(" " + oe.Operator + " ")
	if oe.Right != nil {
		out.WriteString(oe.Right.String())
	} else {
		out.WriteString(" !!MISSING '%>'!!")
	}
	out.WriteString(")")

	return out.String()
}
