package ast

import (
	"bytes"
)

type IfExpression struct {
	TokenAble
	Condition Expression
	Block     *BlockStatement
	ElseIf    []*ElseIfExpression
	ElseBlock *BlockStatement
}

type ElseIfExpression struct {
	TokenAble
	Condition Expression
	Block     *BlockStatement
}

func (ie *IfExpression) expressionNode() {}

func (ie *IfExpression) String() string {
	var out bytes.Buffer

	out.WriteString("if (")
	out.WriteString(ie.Condition.String())
	out.WriteString(") { ")
	out.WriteString(ie.Block.String())
	out.WriteString(" }")

	for _, elseIf := range ie.ElseIf {
		out.WriteString(" } else if (")
		out.WriteString(elseIf.Condition.String())
		out.WriteString(") { ")
		out.WriteString(elseIf.Block.String())
		out.WriteString(" }")
	}

	if ie.ElseBlock != nil {
		out.WriteString(" } else { ")
		out.WriteString(ie.ElseBlock.String())
		out.WriteString(" }")
	}

	return out.String()
}
