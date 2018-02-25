package ast

import (
	"bytes"
)

type IfExpression struct {
	TokenAble
	Condition Expression
	Block     *BlockStatement
	ElseBlock *BlockStatement
}

func (ie *IfExpression) expressionNode() {}

func (ie *IfExpression) String() string {
	var out bytes.Buffer

	out.WriteString("if (")
	out.WriteString(ie.Condition.String())
	out.WriteString(") { ")
	out.WriteString(ie.Block.String())
	out.WriteString(" }")

	if ie.ElseBlock != nil {
		out.WriteString(" } else { ")
		out.WriteString(ie.ElseBlock.String())
		out.WriteString(" }")
	}

	return out.String()
}
