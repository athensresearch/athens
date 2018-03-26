package ast

// Token is used in the lexer to split characters into a string called a token
type Token struct {
	PosImpl // PosImpl provides get/set the position function.
	Tok     int
	Lit     string
}
