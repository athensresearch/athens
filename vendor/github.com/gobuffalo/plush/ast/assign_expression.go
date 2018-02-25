package ast

import "fmt"

type AssignExpression struct {
	TokenAble
	Name  *Identifier
	Value Expression
}

func (ae *AssignExpression) expressionNode() {}

func (ae *AssignExpression) String() string {
	return fmt.Sprintf("%s = %s", ae.Name.String(), ae.Value.String())
}
