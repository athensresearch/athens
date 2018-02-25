package ast

import "github.com/gobuffalo/plush/token"

type TokenAble struct {
	token.Token
}

func (t TokenAble) T() token.Token {
	return t.Token
}

func (t TokenAble) TokenLiteral() string {
	return t.Token.Literal
}

type Printable interface {
	Printable() bool
}

// The base Node interface
type Node interface {
	T() token.Token
	TokenLiteral() string
	String() string
}

// All statement nodes implement this
type Statement interface {
	Node
	statementNode()
}

// All expression nodes implement this
type Expression interface {
	Node
	expressionNode()
}
