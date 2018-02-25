package ast

type HTMLLiteral struct {
	TokenAble
	Value string
}

func (hl *HTMLLiteral) Printable() bool {
	return true
}

func (hl *HTMLLiteral) expressionNode() {}

func (hl *HTMLLiteral) String() string {
	return hl.Token.Literal
}
