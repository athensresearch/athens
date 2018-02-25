package ast

type IntegerLiteral struct {
	TokenAble
	Value int
}

func (il *IntegerLiteral) expressionNode() {}

func (il *IntegerLiteral) String() string {
	return il.Token.Literal
}
