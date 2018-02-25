package ast

import (
	"bytes"
)

type ReturnStatement struct {
	TokenAble
	ReturnValue Expression
}

func (rs *ReturnStatement) Printable() bool {
	return true
}

func (rs *ReturnStatement) statementNode() {}

func (rs *ReturnStatement) String() string {
	var out bytes.Buffer

	out.WriteString("return ")

	if rs.ReturnValue != nil {
		out.WriteString(rs.ReturnValue.String())
	}

	out.WriteString(";")

	return out.String()
}
