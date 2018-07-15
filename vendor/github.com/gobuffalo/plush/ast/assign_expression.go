package ast

import "fmt"

type AssignExpression struct {
	TokenAble
	Name  *Identifier
	Value Expression
}

func (ae *AssignExpression) expressionNode() {}

func (ae *AssignExpression) String() string {
	n, v := "?", "?"
	if ae.Name != nil {
		n = ae.Name.String()
	}
	if ae.Value != nil {
		v = ae.Value.String()
	}
	return fmt.Sprintf("%s = %s", n, v)
}
