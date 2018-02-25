package ast

type Boolean struct {
	TokenAble
	Value bool
}

func (b *Boolean) expressionNode() {}

func (b *Boolean) String() string {
	return b.Token.Literal
}
