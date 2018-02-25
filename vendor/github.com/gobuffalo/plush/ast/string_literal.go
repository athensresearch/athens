package ast

type StringLiteral struct {
	TokenAble
	Value string
}

func (sl *StringLiteral) expressionNode() {}

func (sl *StringLiteral) String() string {
	return sl.Token.Literal
}
