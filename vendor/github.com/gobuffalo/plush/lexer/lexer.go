package lexer

import (
	"strings"

	"github.com/gobuffalo/plush/token"
)

// Lexer moves through the source input and tokenizes its content
type Lexer struct {
	input        string
	position     int  // current position in input (points to current char)
	readPosition int  // current reading position in input (after current char)
	ch           byte // current char under examination
	inside       bool
	curLine      int
}

// New Lexer from the input string
func New(input string) *Lexer {
	l := &Lexer{input: input, curLine: 1}
	l.readChar()
	return l
}

// NextToken from the source input
func (l *Lexer) NextToken() token.Token {
	if l.inside {
		return l.nextInsideToken()
	}
	var tok token.Token

	// l.skipWhitespace()
	if l.ch == 0 {
		tok.Literal = ""
		tok.Type = token.EOF
		tok.LineNumber = l.curLine
		return tok
	}

	if l.ch == '<' && l.peekChar() == '%' {
		l.inside = true
		return l.nextInsideToken()
	}

	tok.Type = token.HTML
	tok.Literal = l.readHTML()
	tok.LineNumber = l.curLine
	return tok
}

func (l *Lexer) nextInsideToken() token.Token {
	var tok token.Token

	l.skipWhitespace()

	switch l.ch {
	case '=':
		if l.peekChar() == '=' {
			ch := l.ch
			l.readChar()
			tok = token.Token{Type: token.EQ, Literal: string(ch) + string(l.ch), LineNumber: l.curLine}
		} else {
			tok = l.newToken(token.ASSIGN)
		}
	case '.':
		if isDigit(l.peekChar()) {
			tok.Literal = l.readNumber()
			tokSplit := strings.Split(tok.Literal, ".")
			switch {
			case len(tokSplit) > 2:
				return l.newIllegalTokenLiteral(token.ILLEGAL, tok.Literal)
			case len(tokSplit) == 2:
				tok.Type = "FLOAT"
			default:
				tok.Type = "INT"
			}

			break
		}
		tok = l.newToken(token.DOT)
	case '+':
		tok = l.newToken(token.PLUS)
	case '&':
		if l.peekChar() == '&' {
			l.readChar()
			tok = token.Token{Type: token.AND, Literal: "&&", LineNumber: l.curLine}
			break
		}
		tok = l.newToken(token.ILLEGAL)
	case '|':
		if l.peekChar() == '|' {
			l.readChar()
			tok = token.Token{Type: token.OR, Literal: "||", LineNumber: l.curLine}
			break
		}
		tok = l.newToken(token.ILLEGAL)
	case '-':
		tok = l.newToken(token.MINUS)
	case '!':
		if l.peekChar() == '=' {
			ch := l.ch
			l.readChar()
			tok = token.Token{Type: token.NOT_EQ, Literal: string(ch) + string(l.ch), LineNumber: l.curLine}
		} else {
			tok = l.newToken(token.BANG)
		}
	case '/':
		tok = l.newToken(token.SLASH)
	case '*':
		tok = l.newToken(token.ASTERISK)
	case '%':
		if l.peekChar() == '>' {
			l.inside = false
			l.readChar()
			tok = token.Token{Type: token.E_END, Literal: "%>", LineNumber: l.curLine}
			break
		}
		tok = l.newToken(token.ILLEGAL)
	case '<':
		if l.peekChar() == '%' {
			l.inside = true
			l.readChar()
			switch l.peekChar() {
			case '#':
				l.readChar()
				tok = token.Token{Type: token.C_START, Literal: "<%#", LineNumber: l.curLine}
			case '=':
				l.readChar()
				tok = token.Token{Type: token.E_START, Literal: "<%=", LineNumber: l.curLine}
			default:
				tok = token.Token{Type: token.S_START, Literal: "<%", LineNumber: l.curLine}
			}
			break
		}
		if l.peekChar() == '=' {
			l.readChar()
			tok = token.Token{Type: token.LTEQ, Literal: "<=", LineNumber: l.curLine}
			break
		}
		tok = l.newToken(token.LT)
	case '~':
		if l.peekChar() == '=' {
			l.readChar()
			tok = token.Token{Type: token.MATCHES, Literal: "~=", LineNumber: l.curLine}
			break
		}
		tok = l.newToken(token.MATCHES)
	case '>':
		if l.peekChar() == '=' {
			l.readChar()
			tok = token.Token{Type: token.GTEQ, Literal: ">=", LineNumber: l.curLine}
			break
		}
		tok = l.newToken(token.GT)
	case ';':
		tok = l.newToken(token.SEMICOLON)
	case ':':
		tok = l.newToken(token.COLON)
	case ',':
		tok = l.newToken(token.COMMA)
	case '{':
		tok = l.newToken(token.LBRACE)
	case '}':
		tok = l.newToken(token.RBRACE)
	case '(':
		tok = l.newToken(token.LPAREN)
	case ')':
		tok = l.newToken(token.RPAREN)
	case '"':
		tok.Type = token.STRING
		tok.Literal = l.readString()
	case '[':
		tok = l.newToken(token.LBRACKET)
	case ']':
		tok = l.newToken(token.RBRACKET)
	case 0:
		tok.Literal = ""
		tok.Type = token.EOF
	default:
		if isLetter(l.ch) {
			tok.Literal = l.readIdentifier()
			tok.Type = token.LookupIdent(tok.Literal)
			return tok
		} else if isDigit(l.ch) {
			tok.Literal = l.readNumber()
			tokSplit := strings.Split(tok.Literal, ".")
			switch {
			case len(tokSplit) > 2:
				return l.newIllegalTokenLiteral(token.ILLEGAL, tok.Literal)
			case len(tokSplit) == 2:
				tok.Type = "FLOAT"
			default:
				tok.Type = "INT"
			}

			return tok
		} else {
			tok = l.newToken(token.ILLEGAL)
		}
	}

	l.readChar()
	tok.LineNumber = l.curLine
	return tok
}

func (l *Lexer) skipWhitespace() {
	if l.readPosition >= len(l.input) {
		l.readChar()
		return
	}
	for l.ch == ' ' || l.ch == '\t' || l.ch == '\n' || l.ch == '\r' {
		l.readChar()
	}
}

func (l *Lexer) readChar() {
	if l.readPosition >= len(l.input) {
		l.ch = 0
	} else {
		l.ch = l.input[l.readPosition]
	}
	if l.ch == '\n' {
		l.curLine++
	}
	l.position = l.readPosition
	l.readPosition++
}

func (l *Lexer) peekChar() byte {

	if l.readPosition >= len(l.input) {
		return 0
	}
	return l.input[l.readPosition]
}

func (l *Lexer) readIdentifier() string {
	position := l.position
	for isLetter(l.ch) || isDigit(l.ch) {
		l.readChar()
	}
	return l.input[position:l.position]
}

func (l *Lexer) readNumber() string {
	position := l.position
	for isDigit(l.ch) || isDot(l.ch) {
		l.readChar()
	}
	return l.input[position:l.position]
}

func (l *Lexer) readString() string {
	position := l.position + 1
	for l.ch != 0 {
		l.readChar()
		// check for quote escapes
		if l.ch == '\\' && l.peekChar() == '"' {
			l.readChar()
			l.readChar()
		}
		if l.ch == '"' {
			break
		}
	}
	s := l.input[position:l.position]
	return strings.Replace(s, "\\\"", "\"", -1)
}

func (l *Lexer) readHTML() string {
	position := l.position

	for l.ch != 0 {
		// allow for expression escaping using \<% foo %>
		if l.ch == '\\' && l.peekChar() == '<' {
			l.readChar()
			l.readChar()
		}
		if l.ch == '<' && l.peekChar() == '%' {
			l.inside = true
			break
		}
		l.readChar()
	}
	return strings.Replace(l.input[position:l.position], "\\<%", "<%", -1)
}

func isLetter(ch byte) bool {
	return 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || ch == '_' || ch == '-'
}

func isDigit(ch byte) bool {
	return '0' <= ch && ch <= '9' || ch == '.'
}

func isDot(ch byte) bool {
	return '.' == ch
}

func (l *Lexer) newToken(tokenType token.Type) token.Token {
	return token.Token{Type: tokenType, Literal: string(l.ch), LineNumber: l.curLine}
}

func (l *Lexer) newIllegalTokenLiteral(tokenType token.Type, literal string) token.Token {
	return token.Token{Type: tokenType, Literal: literal, LineNumber: l.curLine}
}
