//line parser.go.y:2
package parser

import __yyfmt__ "fmt"

//line parser.go.y:2
import (
	"github.com/mattn/anko/ast"
)

//line parser.go.y:27
type yySymType struct {
	yys          int
	compstmt     []ast.Stmt
	stmt_if      ast.Stmt
	stmt_default ast.Stmt
	stmt_case    ast.Stmt
	stmt_cases   []ast.Stmt
	stmts        []ast.Stmt
	stmt         ast.Stmt
	expr         ast.Expr
	exprs        []ast.Expr
	expr_many    []ast.Expr
	expr_lets    ast.Expr
	expr_pair    ast.Expr
	expr_pairs   []ast.Expr
	expr_idents  []string
	expr_type    string
	tok          ast.Token
	term         ast.Token
	terms        ast.Token
	opt_terms    ast.Token
	array_count  ast.ArrayCount
}

const IDENT = 57346
const NUMBER = 57347
const STRING = 57348
const ARRAY = 57349
const VARARG = 57350
const FUNC = 57351
const RETURN = 57352
const VAR = 57353
const THROW = 57354
const IF = 57355
const ELSE = 57356
const FOR = 57357
const IN = 57358
const EQEQ = 57359
const NEQ = 57360
const GE = 57361
const LE = 57362
const OROR = 57363
const ANDAND = 57364
const NEW = 57365
const TRUE = 57366
const FALSE = 57367
const NIL = 57368
const MODULE = 57369
const TRY = 57370
const CATCH = 57371
const FINALLY = 57372
const PLUSEQ = 57373
const MINUSEQ = 57374
const MULEQ = 57375
const DIVEQ = 57376
const ANDEQ = 57377
const OREQ = 57378
const BREAK = 57379
const CONTINUE = 57380
const PLUSPLUS = 57381
const MINUSMINUS = 57382
const POW = 57383
const SHIFTLEFT = 57384
const SHIFTRIGHT = 57385
const SWITCH = 57386
const CASE = 57387
const DEFAULT = 57388
const GO = 57389
const CHAN = 57390
const MAKE = 57391
const OPCHAN = 57392
const TYPE = 57393
const LEN = 57394
const DELETE = 57395
const UNARY = 57396

var yyToknames = [...]string{
	"$end",
	"error",
	"$unk",
	"IDENT",
	"NUMBER",
	"STRING",
	"ARRAY",
	"VARARG",
	"FUNC",
	"RETURN",
	"VAR",
	"THROW",
	"IF",
	"ELSE",
	"FOR",
	"IN",
	"EQEQ",
	"NEQ",
	"GE",
	"LE",
	"OROR",
	"ANDAND",
	"NEW",
	"TRUE",
	"FALSE",
	"NIL",
	"MODULE",
	"TRY",
	"CATCH",
	"FINALLY",
	"PLUSEQ",
	"MINUSEQ",
	"MULEQ",
	"DIVEQ",
	"ANDEQ",
	"OREQ",
	"BREAK",
	"CONTINUE",
	"PLUSPLUS",
	"MINUSMINUS",
	"POW",
	"SHIFTLEFT",
	"SHIFTRIGHT",
	"SWITCH",
	"CASE",
	"DEFAULT",
	"GO",
	"CHAN",
	"MAKE",
	"OPCHAN",
	"TYPE",
	"LEN",
	"DELETE",
	"'='",
	"'?'",
	"':'",
	"','",
	"'>'",
	"'<'",
	"'+'",
	"'-'",
	"'*'",
	"'/'",
	"'%'",
	"UNARY",
	"'{'",
	"'}'",
	"';'",
	"'['",
	"']'",
	"'.'",
	"'!'",
	"'^'",
	"'&'",
	"'('",
	"')'",
	"'|'",
	"'\\n'",
}
var yyStatenames = [...]string{}

const yyEofCode = 1
const yyErrCode = 2
const yyInitialStackSize = 16

//line parser.go.y:762

//line yacctab:1
var yyExca = [...]int{
	-1, 0,
	1, 3,
	-2, 132,
	-1, 1,
	1, -1,
	-2, 0,
	-1, 2,
	57, 51,
	-2, 1,
	-1, 10,
	57, 52,
	-2, 24,
	-1, 45,
	57, 51,
	-2, 133,
	-1, 87,
	67, 3,
	-2, 132,
	-1, 90,
	57, 52,
	-2, 48,
	-1, 91,
	16, 43,
	57, 43,
	-2, 55,
	-1, 93,
	67, 3,
	-2, 132,
	-1, 100,
	1, 60,
	8, 60,
	45, 60,
	46, 60,
	54, 60,
	56, 60,
	57, 60,
	66, 60,
	67, 60,
	68, 60,
	70, 60,
	76, 60,
	78, 60,
	-2, 55,
	-1, 102,
	1, 62,
	8, 62,
	45, 62,
	46, 62,
	54, 62,
	56, 62,
	57, 62,
	66, 62,
	67, 62,
	68, 62,
	70, 62,
	76, 62,
	78, 62,
	-2, 55,
	-1, 132,
	17, 0,
	18, 0,
	-2, 87,
	-1, 133,
	17, 0,
	18, 0,
	-2, 88,
	-1, 153,
	57, 52,
	-2, 48,
	-1, 155,
	67, 3,
	-2, 132,
	-1, 157,
	67, 3,
	-2, 132,
	-1, 159,
	67, 1,
	-2, 39,
	-1, 162,
	67, 3,
	-2, 132,
	-1, 189,
	67, 3,
	-2, 132,
	-1, 237,
	57, 53,
	-2, 49,
	-1, 238,
	1, 50,
	45, 50,
	46, 50,
	54, 50,
	57, 54,
	67, 50,
	68, 50,
	78, 50,
	-2, 55,
	-1, 247,
	1, 54,
	8, 54,
	45, 54,
	46, 54,
	57, 54,
	67, 54,
	68, 54,
	70, 54,
	76, 54,
	78, 54,
	-2, 55,
	-1, 249,
	67, 3,
	-2, 132,
	-1, 251,
	67, 3,
	-2, 132,
	-1, 266,
	67, 3,
	-2, 132,
	-1, 276,
	1, 108,
	8, 108,
	45, 108,
	46, 108,
	54, 108,
	56, 108,
	57, 108,
	66, 108,
	67, 108,
	68, 108,
	70, 108,
	76, 108,
	78, 108,
	-2, 106,
	-1, 278,
	1, 112,
	8, 112,
	45, 112,
	46, 112,
	54, 112,
	56, 112,
	57, 112,
	66, 112,
	67, 112,
	68, 112,
	70, 112,
	76, 112,
	78, 112,
	-2, 110,
	-1, 293,
	67, 3,
	-2, 132,
	-1, 298,
	67, 3,
	-2, 132,
	-1, 299,
	67, 3,
	-2, 132,
	-1, 304,
	1, 107,
	8, 107,
	45, 107,
	46, 107,
	54, 107,
	56, 107,
	57, 107,
	66, 107,
	67, 107,
	68, 107,
	70, 107,
	76, 107,
	78, 107,
	-2, 105,
	-1, 305,
	1, 111,
	8, 111,
	45, 111,
	46, 111,
	54, 111,
	56, 111,
	57, 111,
	66, 111,
	67, 111,
	68, 111,
	70, 111,
	76, 111,
	78, 111,
	-2, 109,
	-1, 312,
	67, 3,
	-2, 132,
	-1, 313,
	67, 3,
	-2, 132,
	-1, 316,
	45, 3,
	46, 3,
	67, 3,
	-2, 132,
	-1, 320,
	67, 3,
	-2, 132,
	-1, 328,
	45, 3,
	46, 3,
	67, 3,
	-2, 132,
	-1, 341,
	67, 3,
	-2, 132,
	-1, 342,
	67, 3,
	-2, 132,
}

const yyPrivate = 57344

const yyLast = 3037

var yyAct = [...]int{

	83, 175, 256, 10, 47, 257, 181, 42, 6, 223,
	11, 305, 1, 304, 300, 267, 84, 262, 7, 90,
	6, 94, 82, 88, 97, 98, 99, 101, 103, 92,
	7, 221, 242, 229, 172, 115, 108, 110, 230, 105,
	277, 114, 6, 113, 117, 275, 10, 269, 112, 282,
	121, 122, 7, 124, 125, 126, 127, 128, 129, 130,
	131, 132, 133, 134, 135, 136, 137, 138, 139, 140,
	141, 142, 143, 219, 284, 144, 145, 146, 147, 211,
	149, 151, 153, 2, 193, 281, 148, 44, 229, 154,
	111, 271, 152, 283, 154, 96, 120, 167, 182, 229,
	158, 95, 161, 166, 280, 96, 164, 346, 278, 171,
	104, 178, 180, 276, 173, 268, 187, 106, 107, 235,
	153, 183, 120, 176, 185, 345, 258, 259, 154, 338,
	190, 335, 292, 154, 334, 331, 330, 327, 160, 317,
	342, 218, 186, 156, 311, 310, 287, 212, 255, 273,
	253, 197, 194, 250, 248, 208, 202, 200, 341, 320,
	10, 204, 205, 232, 153, 313, 299, 298, 199, 215,
	201, 159, 266, 155, 207, 206, 93, 220, 119, 120,
	285, 120, 154, 116, 303, 295, 226, 227, 225, 237,
	231, 233, 163, 241, 293, 81, 157, 243, 5, 246,
	188, 8, 239, 46, 191, 258, 259, 340, 336, 254,
	86, 176, 279, 260, 240, 263, 261, 234, 182, 48,
	217, 216, 123, 85, 4, 184, 274, 174, 45, 66,
	67, 68, 69, 70, 71, 89, 209, 286, 198, 57,
	17, 3, 0, 0, 46, 0, 0, 118, 80, 210,
	0, 0, 0, 291, 0, 0, 0, 222, 224, 294,
	0, 0, 289, 0, 290, 0, 0, 79, 0, 51,
	0, 246, 76, 78, 302, 74, 0, 0, 0, 297,
	0, 0, 306, 0, 0, 307, 308, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 270, 314, 272, 0, 0,
	0, 318, 319, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 333, 325, 326, 0, 0, 329,
	0, 0, 0, 332, 0, 0, 0, 0, 21, 22,
	28, 337, 0, 32, 14, 9, 15, 43, 0, 18,
	0, 0, 0, 0, 343, 344, 0, 38, 29, 30,
	31, 16, 19, 0, 0, 0, 0, 0, 0, 0,
	0, 12, 13, 0, 0, 0, 0, 0, 20, 316,
	0, 36, 0, 39, 40, 0, 37, 41, 0, 0,
	0, 0, 0, 0, 0, 23, 27, 0, 0, 328,
	34, 0, 6, 33, 0, 0, 24, 25, 26, 35,
	0, 0, 7, 60, 61, 63, 65, 75, 77, 0,
	0, 0, 0, 0, 0, 0, 0, 66, 67, 68,
	69, 70, 71, 0, 0, 72, 73, 57, 58, 59,
	0, 0, 0, 0, 0, 0, 80, 0, 0, 0,
	0, 50, 0, 323, 62, 64, 52, 53, 54, 55,
	56, 0, 0, 0, 0, 79, 0, 51, 0, 0,
	76, 78, 322, 74, 60, 61, 63, 65, 75, 77,
	0, 0, 0, 0, 0, 0, 0, 0, 66, 67,
	68, 69, 70, 71, 0, 0, 72, 73, 57, 58,
	59, 0, 0, 0, 0, 0, 0, 80, 0, 0,
	0, 0, 50, 214, 0, 62, 64, 52, 53, 54,
	55, 56, 0, 0, 0, 0, 79, 213, 51, 0,
	0, 76, 78, 0, 74, 60, 61, 63, 65, 75,
	77, 0, 0, 0, 0, 0, 0, 0, 0, 66,
	67, 68, 69, 70, 71, 0, 0, 72, 73, 57,
	58, 59, 0, 0, 0, 0, 0, 0, 80, 0,
	0, 0, 0, 50, 196, 0, 62, 64, 52, 53,
	54, 55, 56, 0, 0, 0, 0, 79, 195, 51,
	0, 0, 76, 78, 0, 74, 60, 61, 63, 65,
	75, 77, 0, 0, 0, 0, 0, 0, 0, 0,
	66, 67, 68, 69, 70, 71, 0, 0, 72, 73,
	57, 58, 59, 0, 0, 0, 0, 0, 0, 80,
	0, 0, 0, 0, 50, 0, 0, 62, 64, 52,
	53, 54, 55, 56, 0, 0, 0, 0, 79, 0,
	51, 0, 0, 76, 78, 339, 74, 60, 61, 63,
	65, 75, 77, 0, 0, 0, 0, 0, 0, 0,
	0, 66, 67, 68, 69, 70, 71, 0, 0, 72,
	73, 57, 58, 59, 0, 0, 0, 0, 0, 0,
	80, 0, 0, 0, 0, 50, 0, 0, 62, 64,
	52, 53, 54, 55, 56, 0, 0, 0, 0, 79,
	0, 51, 0, 0, 76, 78, 324, 74, 60, 61,
	63, 65, 75, 77, 0, 0, 0, 0, 0, 0,
	0, 0, 66, 67, 68, 69, 70, 71, 0, 0,
	72, 73, 57, 58, 59, 0, 0, 0, 0, 0,
	0, 80, 0, 0, 0, 0, 50, 0, 0, 62,
	64, 52, 53, 54, 55, 56, 0, 0, 0, 0,
	79, 0, 51, 0, 0, 76, 78, 321, 74, 60,
	61, 63, 65, 75, 77, 0, 0, 0, 0, 0,
	0, 0, 0, 66, 67, 68, 69, 70, 71, 0,
	0, 72, 73, 57, 58, 59, 0, 0, 0, 0,
	0, 0, 80, 0, 0, 0, 0, 50, 315, 0,
	62, 64, 52, 53, 54, 55, 56, 0, 0, 0,
	0, 79, 0, 51, 0, 0, 76, 78, 0, 74,
	60, 61, 63, 65, 75, 77, 0, 0, 0, 0,
	0, 0, 0, 0, 66, 67, 68, 69, 70, 71,
	0, 0, 72, 73, 57, 58, 59, 0, 0, 0,
	0, 0, 0, 80, 0, 0, 0, 0, 50, 0,
	0, 62, 64, 52, 53, 54, 55, 56, 0, 312,
	0, 0, 79, 0, 51, 0, 0, 76, 78, 0,
	74, 60, 61, 63, 65, 75, 77, 0, 0, 0,
	0, 0, 0, 0, 0, 66, 67, 68, 69, 70,
	71, 0, 0, 72, 73, 57, 58, 59, 0, 0,
	0, 0, 0, 0, 80, 0, 0, 0, 0, 50,
	0, 0, 62, 64, 52, 53, 54, 55, 56, 0,
	0, 0, 0, 79, 0, 51, 0, 0, 76, 78,
	309, 74, 60, 61, 63, 65, 75, 77, 0, 0,
	0, 0, 0, 0, 0, 0, 66, 67, 68, 69,
	70, 71, 0, 0, 72, 73, 57, 58, 59, 0,
	0, 0, 0, 0, 0, 80, 0, 0, 0, 0,
	50, 0, 0, 62, 64, 52, 53, 54, 55, 56,
	0, 0, 0, 0, 79, 296, 51, 0, 0, 76,
	78, 0, 74, 60, 61, 63, 65, 75, 77, 0,
	0, 0, 0, 0, 0, 0, 0, 66, 67, 68,
	69, 70, 71, 0, 0, 72, 73, 57, 58, 59,
	0, 0, 0, 0, 0, 0, 80, 0, 0, 0,
	0, 50, 0, 0, 62, 64, 52, 53, 54, 55,
	56, 0, 0, 0, 0, 79, 288, 51, 0, 0,
	76, 78, 0, 74, 60, 61, 63, 65, 75, 77,
	0, 0, 0, 0, 0, 0, 0, 0, 66, 67,
	68, 69, 70, 71, 0, 0, 72, 73, 57, 58,
	59, 0, 0, 0, 0, 0, 0, 80, 0, 0,
	0, 0, 50, 0, 0, 62, 64, 52, 53, 54,
	55, 56, 0, 0, 0, 0, 79, 265, 51, 0,
	0, 76, 78, 0, 74, 60, 61, 63, 65, 75,
	77, 0, 0, 0, 0, 0, 0, 0, 0, 66,
	67, 68, 69, 70, 71, 0, 0, 72, 73, 57,
	58, 59, 0, 0, 0, 0, 0, 0, 80, 0,
	0, 0, 0, 50, 0, 0, 62, 64, 52, 53,
	54, 55, 56, 0, 0, 0, 252, 79, 0, 51,
	0, 0, 76, 78, 0, 74, 60, 61, 63, 65,
	75, 77, 0, 0, 0, 0, 0, 0, 0, 0,
	66, 67, 68, 69, 70, 71, 0, 0, 72, 73,
	57, 58, 59, 0, 0, 0, 0, 0, 0, 80,
	0, 0, 0, 0, 50, 0, 0, 62, 64, 52,
	53, 54, 55, 56, 0, 251, 0, 0, 79, 0,
	51, 0, 0, 76, 78, 0, 74, 60, 61, 63,
	65, 75, 77, 0, 0, 0, 0, 0, 0, 0,
	0, 66, 67, 68, 69, 70, 71, 0, 0, 72,
	73, 57, 58, 59, 0, 0, 0, 0, 0, 0,
	80, 0, 0, 0, 0, 50, 0, 0, 62, 64,
	52, 53, 54, 55, 56, 0, 249, 0, 0, 79,
	0, 51, 0, 0, 76, 78, 0, 74, 60, 61,
	63, 65, 75, 77, 0, 0, 0, 0, 0, 0,
	0, 0, 66, 67, 68, 69, 70, 71, 0, 0,
	72, 73, 57, 58, 59, 0, 0, 0, 0, 0,
	0, 80, 0, 0, 0, 0, 50, 0, 0, 62,
	64, 52, 53, 54, 55, 56, 0, 0, 0, 0,
	79, 245, 51, 0, 0, 76, 78, 0, 74, 60,
	61, 63, 65, 75, 77, 0, 0, 0, 0, 0,
	0, 0, 0, 66, 67, 68, 69, 70, 71, 0,
	0, 72, 73, 57, 58, 59, 0, 0, 0, 0,
	0, 0, 80, 0, 0, 0, 0, 50, 0, 236,
	62, 64, 52, 53, 54, 55, 56, 0, 0, 0,
	0, 79, 0, 51, 0, 0, 76, 78, 0, 74,
	60, 61, 63, 65, 75, 77, 0, 0, 0, 0,
	0, 0, 0, 0, 66, 67, 68, 69, 70, 71,
	0, 0, 72, 73, 57, 58, 59, 0, 0, 0,
	0, 0, 0, 80, 0, 0, 0, 0, 50, 0,
	0, 62, 64, 52, 53, 54, 55, 56, 0, 0,
	0, 0, 79, 0, 51, 0, 0, 76, 78, 228,
	74, 60, 61, 63, 65, 75, 77, 0, 0, 0,
	0, 0, 0, 0, 0, 66, 67, 68, 69, 70,
	71, 0, 0, 72, 73, 57, 58, 59, 0, 0,
	0, 0, 0, 0, 80, 0, 0, 0, 0, 50,
	192, 0, 62, 64, 52, 53, 54, 55, 56, 0,
	0, 0, 0, 79, 0, 51, 0, 0, 76, 78,
	0, 74, 60, 61, 63, 65, 75, 77, 0, 0,
	0, 0, 0, 0, 0, 0, 66, 67, 68, 69,
	70, 71, 0, 0, 72, 73, 57, 58, 59, 0,
	0, 0, 0, 0, 0, 80, 0, 0, 0, 0,
	50, 0, 0, 62, 64, 52, 53, 54, 55, 56,
	0, 189, 0, 0, 79, 0, 51, 0, 0, 76,
	78, 0, 74, 60, 61, 63, 65, 75, 77, 0,
	0, 0, 0, 0, 0, 0, 0, 66, 67, 68,
	69, 70, 71, 0, 0, 72, 73, 57, 58, 59,
	0, 0, 0, 0, 0, 0, 80, 0, 0, 0,
	0, 50, 0, 0, 62, 64, 52, 53, 54, 55,
	56, 0, 0, 0, 0, 79, 0, 51, 0, 0,
	76, 78, 177, 74, 60, 61, 63, 65, 75, 77,
	0, 0, 0, 0, 0, 0, 0, 0, 66, 67,
	68, 69, 70, 71, 0, 0, 72, 73, 57, 58,
	59, 0, 0, 0, 0, 0, 0, 80, 0, 0,
	0, 0, 50, 0, 0, 62, 64, 52, 53, 54,
	55, 56, 0, 165, 0, 0, 79, 0, 51, 0,
	0, 76, 78, 0, 74, 60, 61, 63, 65, 75,
	77, 0, 0, 0, 0, 0, 0, 0, 0, 66,
	67, 68, 69, 70, 71, 0, 0, 72, 73, 57,
	58, 59, 0, 0, 0, 0, 0, 0, 80, 0,
	0, 0, 0, 50, 0, 0, 62, 64, 52, 53,
	54, 55, 56, 0, 162, 0, 0, 79, 0, 51,
	0, 0, 76, 78, 0, 74, 60, 61, 63, 65,
	75, 77, 0, 0, 0, 0, 0, 0, 0, 0,
	66, 67, 68, 69, 70, 71, 0, 0, 72, 73,
	57, 58, 59, 0, 0, 0, 0, 0, 0, 80,
	0, 0, 0, 49, 50, 0, 0, 62, 64, 52,
	53, 54, 55, 56, 0, 0, 0, 0, 79, 0,
	51, 0, 0, 76, 78, 0, 74, 60, 61, 63,
	65, 75, 77, 0, 0, 0, 0, 0, 0, 0,
	0, 66, 67, 68, 69, 70, 71, 0, 0, 72,
	73, 57, 58, 59, 0, 0, 0, 0, 0, 0,
	80, 0, 0, 0, 0, 50, 0, 0, 62, 64,
	52, 53, 54, 55, 56, 0, 0, 0, 0, 79,
	0, 51, 0, 0, 76, 78, 0, 74, 60, 61,
	63, 65, 75, 77, 0, 0, 0, 0, 0, 0,
	0, 0, 66, 67, 68, 69, 70, 71, 0, 0,
	72, 73, 57, 58, 59, 0, 0, 0, 0, 0,
	0, 80, 0, 0, 0, 0, 50, 0, 0, 62,
	64, 52, 53, 54, 55, 56, 0, 0, 0, 0,
	79, 0, 51, 0, 0, 76, 179, 0, 74, 60,
	61, 63, 65, 75, 77, 0, 0, 0, 0, 0,
	0, 0, 0, 66, 67, 68, 69, 70, 71, 0,
	0, 72, 73, 57, 58, 59, 0, 0, 0, 0,
	0, 0, 80, 0, 0, 0, 0, 50, 0, 0,
	62, 64, 52, 53, 54, 55, 56, 0, 0, 0,
	0, 79, 0, 170, 0, 0, 76, 78, 0, 74,
	60, 61, 63, 65, 75, 77, 0, 0, 0, 0,
	0, 0, 0, 0, 66, 67, 68, 69, 70, 71,
	0, 0, 72, 73, 57, 58, 59, 0, 0, 0,
	0, 0, 0, 80, 0, 0, 0, 0, 50, 0,
	0, 62, 64, 52, 53, 54, 55, 56, 0, 0,
	0, 0, 79, 0, 169, 0, 0, 76, 78, 0,
	74, 21, 22, 203, 0, 0, 32, 14, 9, 15,
	43, 0, 18, 0, 0, 0, 0, 0, 0, 0,
	38, 29, 30, 31, 16, 19, 0, 0, 0, 0,
	0, 0, 0, 0, 12, 13, 0, 0, 0, 0,
	0, 20, 0, 0, 36, 0, 39, 40, 0, 37,
	41, 0, 0, 0, 0, 0, 0, 0, 23, 27,
	0, 0, 0, 34, 0, 0, 33, 0, 0, 24,
	25, 26, 35, 60, 61, 63, 65, 0, 77, 0,
	0, 0, 0, 0, 0, 0, 0, 66, 67, 68,
	69, 70, 71, 0, 0, 72, 73, 57, 58, 59,
	0, 0, 0, 0, 0, 0, 80, 0, 0, 0,
	0, 0, 0, 0, 62, 64, 52, 53, 54, 55,
	56, 0, 0, 0, 0, 79, 0, 51, 0, 0,
	76, 78, 0, 74, 21, 22, 28, 0, 0, 32,
	14, 9, 15, 43, 0, 18, 0, 0, 0, 0,
	0, 0, 0, 38, 29, 30, 31, 16, 19, 0,
	0, 0, 0, 0, 0, 0, 0, 12, 13, 0,
	0, 0, 0, 0, 20, 0, 0, 36, 0, 39,
	40, 0, 37, 41, 0, 0, 0, 0, 0, 0,
	0, 23, 27, 0, 0, 0, 34, 0, 0, 33,
	0, 0, 24, 25, 26, 35, 60, 61, 63, 65,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	66, 67, 68, 69, 70, 71, 0, 0, 72, 73,
	57, 58, 59, 0, 0, 0, 0, 0, 0, 80,
	0, 0, 0, 0, 0, 0, 0, 62, 64, 52,
	53, 54, 55, 56, 0, 63, 65, 0, 79, 0,
	51, 0, 0, 76, 78, 0, 74, 66, 67, 68,
	69, 70, 71, 0, 0, 72, 73, 57, 58, 59,
	0, 0, 247, 22, 28, 0, 80, 32, 0, 0,
	0, 0, 0, 0, 62, 64, 52, 53, 54, 55,
	56, 38, 29, 30, 31, 79, 0, 51, 0, 0,
	76, 78, 0, 74, 21, 22, 28, 0, 0, 32,
	0, 0, 0, 0, 0, 36, 0, 39, 40, 0,
	37, 41, 0, 38, 29, 30, 31, 0, 0, 23,
	27, 0, 0, 0, 34, 0, 0, 33, 301, 0,
	24, 25, 26, 35, 0, 0, 0, 36, 0, 39,
	40, 0, 37, 41, 0, 0, 0, 0, 21, 22,
	28, 23, 27, 32, 0, 0, 34, 0, 0, 33,
	264, 0, 24, 25, 26, 35, 0, 38, 29, 30,
	31, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 36, 0, 39, 40, 0, 37, 41, 0, 0,
	0, 0, 0, 0, 0, 23, 27, 0, 0, 0,
	34, 0, 0, 33, 244, 0, 24, 25, 26, 35,
	66, 67, 68, 69, 70, 71, 0, 0, 72, 73,
	57, 0, 0, 0, 0, 21, 22, 28, 0, 80,
	32, 0, 0, 0, 0, 0, 0, 0, 0, 52,
	53, 54, 55, 56, 38, 29, 30, 31, 79, 0,
	51, 0, 0, 76, 78, 0, 74, 0, 0, 21,
	22, 28, 0, 0, 32, 0, 0, 0, 36, 0,
	39, 40, 0, 37, 41, 0, 0, 168, 38, 29,
	30, 31, 23, 27, 0, 0, 0, 34, 0, 0,
	33, 0, 0, 24, 25, 26, 35, 0, 0, 0,
	0, 0, 36, 0, 39, 40, 0, 37, 41, 0,
	0, 150, 0, 21, 22, 28, 23, 27, 32, 0,
	0, 34, 0, 0, 33, 0, 0, 24, 25, 26,
	35, 0, 38, 29, 30, 31, 0, 0, 0, 0,
	0, 0, 0, 0, 247, 22, 28, 0, 0, 32,
	0, 0, 0, 0, 0, 0, 36, 0, 39, 40,
	0, 37, 41, 38, 29, 30, 31, 0, 0, 0,
	23, 27, 0, 0, 0, 34, 0, 0, 33, 0,
	0, 24, 25, 26, 35, 0, 0, 36, 0, 39,
	40, 0, 37, 41, 0, 0, 0, 0, 238, 22,
	28, 23, 27, 32, 0, 0, 34, 0, 0, 33,
	0, 0, 24, 25, 26, 35, 0, 38, 29, 30,
	31, 0, 0, 0, 0, 0, 0, 0, 0, 109,
	22, 28, 0, 0, 32, 0, 0, 0, 0, 0,
	0, 36, 0, 39, 40, 0, 37, 41, 38, 29,
	30, 31, 0, 0, 0, 23, 27, 0, 0, 0,
	34, 0, 0, 33, 0, 0, 24, 25, 26, 35,
	0, 0, 36, 0, 39, 40, 0, 37, 41, 0,
	0, 0, 0, 102, 22, 28, 23, 27, 32, 0,
	0, 34, 0, 0, 33, 0, 0, 24, 25, 26,
	35, 0, 38, 29, 30, 31, 0, 0, 0, 0,
	0, 0, 0, 0, 100, 22, 28, 0, 0, 32,
	0, 0, 0, 0, 0, 0, 36, 0, 39, 40,
	0, 37, 41, 38, 29, 30, 31, 0, 0, 0,
	23, 27, 0, 0, 0, 34, 0, 0, 33, 0,
	0, 24, 25, 26, 35, 0, 0, 36, 0, 39,
	40, 0, 37, 41, 0, 0, 0, 0, 91, 22,
	28, 23, 27, 32, 0, 0, 34, 0, 0, 33,
	0, 0, 24, 25, 26, 35, 0, 38, 29, 30,
	31, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 36, 0, 39, 40, 0, 37, 41, 0, 0,
	0, 0, 0, 0, 0, 23, 27, 0, 0, 0,
	87, 0, 0, 33, 0, 0, 24, 25, 26, 35,
	66, 67, 68, 69, 70, 71, 0, 0, 0, 0,
	57, 0, 0, 0, 0, 0, 0, 0, 0, 80,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 54, 55, 56, 0, 0, 0, 0, 79, 0,
	51, 0, 0, 76, 78, 0, 74,
}
var yyPact = [...]int{

	-60, -1000, 2250, -60, -60, -1000, -1000, -1000, -1000, 215,
	1799, 141, -1000, -1000, 2659, 2659, 219, 196, 2914, 110,
	2659, 26, -1000, 2659, 2659, 2659, 2860, 2829, -1000, -1000,
	-1000, -1000, 35, -60, -60, 2659, 2775, 15, -27, -32,
	2659, -40, 126, 2659, -1000, 334, -1000, 124, -1000, 2659,
	2659, 218, 2659, 2659, 2659, 2659, 2659, 2659, 2659, 2659,
	2659, 2659, 2659, 2659, 2659, 2659, 2659, 2659, 2659, 2659,
	2659, 2659, -1000, -1000, 2659, 2659, 2659, 2659, 2659, 2605,
	2659, 2659, 125, 1860, 1860, 107, 130, -60, 122, 34,
	1738, 26, 138, -60, 1677, 2659, 2571, 198, 198, 198,
	26, 2043, 26, 1982, 215, -41, 2659, 205, 1616, 36,
	1921, 2659, 214, 73, 1860, 2659, -60, 1555, -1000, 2659,
	-60, 1860, 1494, -1000, 2959, 2959, 198, 198, 198, 1860,
	2529, 2529, 2356, 2356, 2529, 2529, 2529, 2529, 1860, 1860,
	1860, 1860, 1860, 1860, 1860, 2176, 1860, 2309, 76, 518,
	2659, 1860, -1000, 1860, -60, -60, 2659, -60, 89, 2117,
	2659, 2659, -60, 2659, 88, -60, 71, 457, 2659, 217,
	216, 65, 215, -26, -48, -1000, 132, -1000, 2659, 2659,
	1433, -38, -1000, 214, 94, 213, 49, 1372, 2744, -60,
	-1000, 210, 2659, -44, -1000, -1000, 2484, 1311, 2690, 87,
	1250, 86, -1000, 132, 1189, 1128, 83, -1000, 180, 81,
	160, -59, -1000, -1000, 2430, 1067, -1000, -1000, 106, -61,
	39, -60, 21, -60, 82, 2659, 37, 32, -1000, 208,
	-1000, 28, -21, 17, 123, -1000, 2659, 1860, 26, 79,
	-1000, 1860, -1000, 1006, -1000, -1000, 1860, 26, -1000, -60,
	-1000, -60, 2659, -1000, 128, -1000, -1000, -1000, 2659, 129,
	-1000, -1000, -1000, 945, -1000, -1000, -60, 101, 100, -62,
	2398, -1000, 117, -1000, 1860, -63, -1000, -65, -1000, -1000,
	-1000, 2659, -1000, -1000, 2659, 2659, 884, -1000, -1000, 78,
	77, 823, 99, -60, 762, -60, -1000, 72, -60, -60,
	93, -1000, -1000, -1000, -1000, -1000, 701, 396, 640, -1000,
	-1000, -1000, -60, -60, 70, -60, -60, -1000, 69, 68,
	-60, -1000, -1000, 2659, -1000, 67, 64, 178, -60, -1000,
	-1000, -1000, 62, 579, -1000, 177, 92, -1000, -1000, -1000,
	74, -60, -60, 58, 40, -1000, -1000,
}
var yyPgo = [...]int{

	0, 12, 241, 201, 240, 5, 2, 236, 0, 7,
	10, 235, 1, 227, 4, 6, 225, 83, 224, 198,
}
var yyR1 = [...]int{

	0, 1, 1, 2, 2, 2, 3, 3, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	3, 3, 3, 3, 3, 4, 4, 4, 7, 7,
	7, 7, 7, 6, 5, 16, 16, 16, 12, 13,
	13, 13, 14, 14, 14, 15, 15, 11, 10, 10,
	10, 9, 9, 9, 9, 8, 8, 8, 8, 8,
	8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
	8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
	8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
	8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
	8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
	8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
	8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
	8, 8, 17, 17, 18, 18, 19, 19,
}
var yyR2 = [...]int{

	0, 1, 2, 0, 2, 3, 4, 3, 3, 1,
	1, 2, 2, 5, 1, 4, 7, 9, 5, 13,
	12, 9, 8, 5, 1, 7, 5, 5, 0, 2,
	2, 2, 2, 5, 4, 0, 2, 3, 3, 0,
	1, 4, 0, 1, 4, 1, 3, 3, 1, 4,
	4, 0, 1, 4, 4, 1, 1, 2, 2, 2,
	2, 4, 2, 4, 1, 1, 1, 1, 5, 3,
	7, 8, 8, 9, 5, 6, 5, 6, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 2,
	2, 3, 3, 3, 3, 5, 4, 6, 5, 5,
	4, 6, 5, 4, 4, 6, 5, 5, 6, 5,
	5, 4, 4, 5, 7, 5, 7, 9, 7, 3,
	2, 6, 0, 1, 1, 2, 1, 1,
}
var yyChk = [...]int{

	-1000, -1, -17, -2, -18, -19, 68, 78, -3, 11,
	-8, -10, 37, 38, 10, 12, 27, -4, 15, 28,
	44, 4, 5, 61, 72, 73, 74, 62, 6, 24,
	25, 26, 9, 69, 66, 75, 47, 52, 23, 49,
	50, 53, -9, 13, -17, -18, -19, -14, 4, 54,
	55, 71, 60, 61, 62, 63, 64, 41, 42, 43,
	17, 18, 58, 19, 59, 20, 31, 32, 33, 34,
	35, 36, 39, 40, 77, 21, 74, 22, 75, 69,
	50, 54, -9, -8, -8, 4, 14, 66, -14, -11,
	-8, 4, -10, 66, -8, 75, 69, -8, -8, -8,
	4, -8, 4, -8, 75, 4, -17, -17, -8, 4,
	-8, 75, 75, 75, -8, 75, 57, -8, -3, 54,
	57, -8, -8, 4, -8, -8, -8, -8, -8, -8,
	-8, -8, -8, -8, -8, -8, -8, -8, -8, -8,
	-8, -8, -8, -8, -8, -8, -8, -8, -9, -8,
	56, -8, -10, -8, 57, 66, 13, 66, -1, -17,
	16, 68, 66, 54, -1, 66, -9, -8, 56, 71,
	71, -14, 75, -9, -13, -12, 6, 76, 75, 75,
	-8, -15, 4, 48, -16, 51, 69, -8, -17, 66,
	-10, -17, 56, 8, 76, 70, 56, -8, -17, -1,
	-8, -1, 67, 6, -8, -8, -1, -10, 67, -7,
	-17, 8, 76, 70, 56, -8, 4, 4, 76, 8,
	-14, 57, -17, 57, -17, 56, -9, -9, 76, 71,
	76, -15, 69, -15, 4, 70, 57, -8, 4, -1,
	4, -8, 76, -8, 70, 70, -8, 4, 67, 66,
	67, 66, 68, 67, 29, 67, -6, -5, 45, 46,
	-6, -5, 76, -8, 70, 70, 66, 76, 76, 8,
	-17, 70, -17, 67, -8, 8, 76, 8, 76, 4,
	76, 57, 70, 76, 57, 57, -8, 67, 70, -1,
	-1, -8, 4, 66, -8, 56, 70, -1, 66, 66,
	76, 70, -12, 67, 76, 76, -8, -8, -8, 76,
	67, 67, 66, 66, -1, 56, -17, 67, -1, -1,
	66, 76, 76, 57, 76, -1, -1, 67, -17, -1,
	67, 67, -1, -8, 67, 67, 30, -1, 67, 76,
	30, 66, 66, -1, -1, 67, 67,
}
var yyDef = [...]int{

	-2, -2, -2, 132, 133, 134, 136, 137, 4, 42,
	-2, 0, 9, 10, 51, 0, 0, 14, 42, 0,
	0, 55, 56, 0, 0, 0, 0, 0, 64, 65,
	66, 67, 0, 132, 132, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 2, -2, 135, 0, 43, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 99, 100, 0, 0, 0, 0, 51, 0,
	0, 51, 11, 52, 12, 0, 0, -2, 0, 0,
	-2, -2, 0, -2, 0, 51, 0, 57, 58, 59,
	-2, 0, -2, 0, 42, 0, 51, 39, 0, 55,
	0, 0, 0, 35, 130, 0, 132, 0, 5, 51,
	132, 7, 0, 69, 79, 80, 81, 82, 83, 84,
	85, 86, -2, -2, 89, 90, 91, 92, 93, 94,
	95, 96, 97, 98, 101, 102, 103, 104, 0, 0,
	0, 129, 8, -2, 132, -2, 0, -2, 0, -2,
	0, 0, -2, 51, 0, 28, 0, 0, 0, 0,
	0, 0, 42, 132, 132, 40, 0, 78, 51, 51,
	0, 0, 45, 0, 0, 0, 0, 0, 0, -2,
	6, 0, 0, 0, 110, 114, 0, 0, 0, 0,
	0, 0, 15, 64, 0, 0, 0, 47, 0, 0,
	0, 0, 106, 113, 0, 0, 61, 63, 0, 0,
	0, 132, 0, 132, 0, 0, 0, 0, 121, 0,
	122, 0, 0, 0, 0, 36, 0, -2, -2, 0,
	44, 68, 109, 0, 119, 120, 53, -2, 13, -2,
	26, -2, 0, 18, 0, 23, 31, 32, 0, 0,
	29, 30, 105, 0, 116, 117, -2, 0, 0, 0,
	0, 74, 0, 76, 38, 0, -2, 0, -2, 46,
	123, 0, 37, 125, 0, 0, 0, 27, 118, 0,
	0, 0, 0, -2, 0, 132, 115, 0, -2, -2,
	0, 75, 41, 77, -2, -2, 0, 0, 0, 131,
	25, 16, -2, -2, 0, 132, -2, 70, 0, 0,
	-2, 124, 126, 0, 128, 0, 0, 22, -2, 34,
	71, 72, 0, 0, 17, 21, 0, 33, 73, 127,
	0, -2, -2, 0, 0, 20, 19,
}
var yyTok1 = [...]int{

	1, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	78, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	3, 3, 3, 72, 3, 3, 3, 64, 74, 3,
	75, 76, 62, 60, 57, 61, 71, 63, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 56, 68,
	59, 54, 58, 55, 3, 3, 3, 3, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	3, 69, 3, 70, 73, 3, 3, 3, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	3, 3, 3, 66, 77, 67,
}
var yyTok2 = [...]int{

	2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
	12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
	22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
	32, 33, 34, 35, 36, 37, 38, 39, 40, 41,
	42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
	52, 53, 65,
}
var yyTok3 = [...]int{
	0,
}

var yyErrorMessages = [...]struct {
	state int
	token int
	msg   string
}{}

//line yaccpar:1

/*	parser for yacc output	*/

var (
	yyDebug        = 0
	yyErrorVerbose = false
)

type yyLexer interface {
	Lex(lval *yySymType) int
	Error(s string)
}

type yyParser interface {
	Parse(yyLexer) int
	Lookahead() int
}

type yyParserImpl struct {
	lval  yySymType
	stack [yyInitialStackSize]yySymType
	char  int
}

func (p *yyParserImpl) Lookahead() int {
	return p.char
}

func yyNewParser() yyParser {
	return &yyParserImpl{}
}

const yyFlag = -1000

func yyTokname(c int) string {
	if c >= 1 && c-1 < len(yyToknames) {
		if yyToknames[c-1] != "" {
			return yyToknames[c-1]
		}
	}
	return __yyfmt__.Sprintf("tok-%v", c)
}

func yyStatname(s int) string {
	if s >= 0 && s < len(yyStatenames) {
		if yyStatenames[s] != "" {
			return yyStatenames[s]
		}
	}
	return __yyfmt__.Sprintf("state-%v", s)
}

func yyErrorMessage(state, lookAhead int) string {
	const TOKSTART = 4

	if !yyErrorVerbose {
		return "syntax error"
	}

	for _, e := range yyErrorMessages {
		if e.state == state && e.token == lookAhead {
			return "syntax error: " + e.msg
		}
	}

	res := "syntax error: unexpected " + yyTokname(lookAhead)

	// To match Bison, suggest at most four expected tokens.
	expected := make([]int, 0, 4)

	// Look for shiftable tokens.
	base := yyPact[state]
	for tok := TOKSTART; tok-1 < len(yyToknames); tok++ {
		if n := base + tok; n >= 0 && n < yyLast && yyChk[yyAct[n]] == tok {
			if len(expected) == cap(expected) {
				return res
			}
			expected = append(expected, tok)
		}
	}

	if yyDef[state] == -2 {
		i := 0
		for yyExca[i] != -1 || yyExca[i+1] != state {
			i += 2
		}

		// Look for tokens that we accept or reduce.
		for i += 2; yyExca[i] >= 0; i += 2 {
			tok := yyExca[i]
			if tok < TOKSTART || yyExca[i+1] == 0 {
				continue
			}
			if len(expected) == cap(expected) {
				return res
			}
			expected = append(expected, tok)
		}

		// If the default action is to accept or reduce, give up.
		if yyExca[i+1] != 0 {
			return res
		}
	}

	for i, tok := range expected {
		if i == 0 {
			res += ", expecting "
		} else {
			res += " or "
		}
		res += yyTokname(tok)
	}
	return res
}

func yylex1(lex yyLexer, lval *yySymType) (char, token int) {
	token = 0
	char = lex.Lex(lval)
	if char <= 0 {
		token = yyTok1[0]
		goto out
	}
	if char < len(yyTok1) {
		token = yyTok1[char]
		goto out
	}
	if char >= yyPrivate {
		if char < yyPrivate+len(yyTok2) {
			token = yyTok2[char-yyPrivate]
			goto out
		}
	}
	for i := 0; i < len(yyTok3); i += 2 {
		token = yyTok3[i+0]
		if token == char {
			token = yyTok3[i+1]
			goto out
		}
	}

out:
	if token == 0 {
		token = yyTok2[1] /* unknown char */
	}
	if yyDebug >= 3 {
		__yyfmt__.Printf("lex %s(%d)\n", yyTokname(token), uint(char))
	}
	return char, token
}

func yyParse(yylex yyLexer) int {
	return yyNewParser().Parse(yylex)
}

func (yyrcvr *yyParserImpl) Parse(yylex yyLexer) int {
	var yyn int
	var yyVAL yySymType
	var yyDollar []yySymType
	_ = yyDollar // silence set and not used
	yyS := yyrcvr.stack[:]

	Nerrs := 0   /* number of errors */
	Errflag := 0 /* error recovery flag */
	yystate := 0
	yyrcvr.char = -1
	yytoken := -1 // yyrcvr.char translated into internal numbering
	defer func() {
		// Make sure we report no lookahead when not parsing.
		yystate = -1
		yyrcvr.char = -1
		yytoken = -1
	}()
	yyp := -1
	goto yystack

ret0:
	return 0

ret1:
	return 1

yystack:
	/* put a state and value onto the stack */
	if yyDebug >= 4 {
		__yyfmt__.Printf("char %v in %v\n", yyTokname(yytoken), yyStatname(yystate))
	}

	yyp++
	if yyp >= len(yyS) {
		nyys := make([]yySymType, len(yyS)*2)
		copy(nyys, yyS)
		yyS = nyys
	}
	yyS[yyp] = yyVAL
	yyS[yyp].yys = yystate

yynewstate:
	yyn = yyPact[yystate]
	if yyn <= yyFlag {
		goto yydefault /* simple state */
	}
	if yyrcvr.char < 0 {
		yyrcvr.char, yytoken = yylex1(yylex, &yyrcvr.lval)
	}
	yyn += yytoken
	if yyn < 0 || yyn >= yyLast {
		goto yydefault
	}
	yyn = yyAct[yyn]
	if yyChk[yyn] == yytoken { /* valid shift */
		yyrcvr.char = -1
		yytoken = -1
		yyVAL = yyrcvr.lval
		yystate = yyn
		if Errflag > 0 {
			Errflag--
		}
		goto yystack
	}

yydefault:
	/* default state action */
	yyn = yyDef[yystate]
	if yyn == -2 {
		if yyrcvr.char < 0 {
			yyrcvr.char, yytoken = yylex1(yylex, &yyrcvr.lval)
		}

		/* look through exception table */
		xi := 0
		for {
			if yyExca[xi+0] == -1 && yyExca[xi+1] == yystate {
				break
			}
			xi += 2
		}
		for xi += 2; ; xi += 2 {
			yyn = yyExca[xi+0]
			if yyn < 0 || yyn == yytoken {
				break
			}
		}
		yyn = yyExca[xi+1]
		if yyn < 0 {
			goto ret0
		}
	}
	if yyn == 0 {
		/* error ... attempt to resume parsing */
		switch Errflag {
		case 0: /* brand new error */
			yylex.Error(yyErrorMessage(yystate, yytoken))
			Nerrs++
			if yyDebug >= 1 {
				__yyfmt__.Printf("%s", yyStatname(yystate))
				__yyfmt__.Printf(" saw %s\n", yyTokname(yytoken))
			}
			fallthrough

		case 1, 2: /* incompletely recovered error ... try again */
			Errflag = 3

			/* find a state where "error" is a legal shift action */
			for yyp >= 0 {
				yyn = yyPact[yyS[yyp].yys] + yyErrCode
				if yyn >= 0 && yyn < yyLast {
					yystate = yyAct[yyn] /* simulate a shift of "error" */
					if yyChk[yystate] == yyErrCode {
						goto yystack
					}
				}

				/* the current p has no shift on "error", pop stack */
				if yyDebug >= 2 {
					__yyfmt__.Printf("error recovery pops state %d\n", yyS[yyp].yys)
				}
				yyp--
			}
			/* there is no state on the stack with an error shift ... abort */
			goto ret1

		case 3: /* no shift yet; clobber input char */
			if yyDebug >= 2 {
				__yyfmt__.Printf("error recovery discards %s\n", yyTokname(yytoken))
			}
			if yytoken == yyEofCode {
				goto ret1
			}
			yyrcvr.char = -1
			yytoken = -1
			goto yynewstate /* try again in the same state */
		}
	}

	/* reduction by production yyn */
	if yyDebug >= 2 {
		__yyfmt__.Printf("reduce %v in:\n\t%v\n", yyn, yyStatname(yystate))
	}

	yynt := yyn
	yypt := yyp
	_ = yypt // guard against "declared and not used"

	yyp -= yyR2[yyn]
	// yyp is now the index of $0. Perform the default action. Iff the
	// reduced production is ε, $1 is possibly out of range.
	if yyp+1 >= len(yyS) {
		nyys := make([]yySymType, len(yyS)*2)
		copy(nyys, yyS)
		yyS = nyys
	}
	yyVAL = yyS[yyp+1]

	/* consult goto table to find next state */
	yyn = yyR1[yyn]
	yyg := yyPgo[yyn]
	yyj := yyg + yyS[yyp].yys + 1

	if yyj >= yyLast {
		yystate = yyAct[yyg]
	} else {
		yystate = yyAct[yyj]
		if yyChk[yystate] != -yyn {
			yystate = yyAct[yyg]
		}
	}
	// dummy call; replaced with literal code
	switch yynt {

	case 1:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:68
		{
			yyVAL.compstmt = nil
		}
	case 2:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:72
		{
			yyVAL.compstmt = yyDollar[1].stmts
		}
	case 3:
		yyDollar = yyS[yypt-0 : yypt+1]
		//line parser.go.y:77
		{
			yyVAL.stmts = nil
			if l, ok := yylex.(*Lexer); ok {
				l.stmts = yyVAL.stmts
			}
		}
	case 4:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:84
		{
			yyVAL.stmts = []ast.Stmt{yyDollar[2].stmt}
			if l, ok := yylex.(*Lexer); ok {
				l.stmts = yyVAL.stmts
			}
		}
	case 5:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:91
		{
			if yyDollar[3].stmt != nil {
				yyVAL.stmts = append(yyDollar[1].stmts, yyDollar[3].stmt)
				if l, ok := yylex.(*Lexer); ok {
					l.stmts = yyVAL.stmts
				}
			}
		}
	case 6:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:102
		{
			yyVAL.stmt = &ast.VarStmt{Names: yyDollar[2].expr_idents, Exprs: yyDollar[4].expr_many}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 7:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:107
		{
			yyVAL.stmt = &ast.LetsStmt{Lhss: []ast.Expr{yyDollar[1].expr}, Operator: "=", Rhss: []ast.Expr{yyDollar[3].expr}}
		}
	case 8:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:111
		{
			yyVAL.stmt = &ast.LetsStmt{Lhss: yyDollar[1].expr_many, Operator: "=", Rhss: yyDollar[3].expr_many}
		}
	case 9:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:115
		{
			yyVAL.stmt = &ast.BreakStmt{}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 10:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:120
		{
			yyVAL.stmt = &ast.ContinueStmt{}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 11:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:125
		{
			yyVAL.stmt = &ast.ReturnStmt{Exprs: yyDollar[2].exprs}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 12:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:130
		{
			yyVAL.stmt = &ast.ThrowStmt{Expr: yyDollar[2].expr}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 13:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:135
		{
			yyVAL.stmt = &ast.ModuleStmt{Name: yyDollar[2].tok.Lit, Stmts: yyDollar[4].compstmt}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 14:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:140
		{
			yyVAL.stmt = yyDollar[1].stmt_if
			yyVAL.stmt.SetPosition(yyDollar[1].stmt_if.Position())
		}
	case 15:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:145
		{
			yyVAL.stmt = &ast.LoopStmt{Stmts: yyDollar[3].compstmt}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 16:
		yyDollar = yyS[yypt-7 : yypt+1]
		//line parser.go.y:150
		{
			yyVAL.stmt = &ast.ForStmt{Vars: yyDollar[2].expr_idents, Value: yyDollar[4].expr, Stmts: yyDollar[6].compstmt}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 17:
		yyDollar = yyS[yypt-9 : yypt+1]
		//line parser.go.y:155
		{
			yyVAL.stmt = &ast.CForStmt{Expr1: yyDollar[2].expr_lets, Expr2: yyDollar[4].expr, Expr3: yyDollar[6].expr, Stmts: yyDollar[8].compstmt}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 18:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:160
		{
			yyVAL.stmt = &ast.LoopStmt{Expr: yyDollar[2].expr, Stmts: yyDollar[4].compstmt}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 19:
		yyDollar = yyS[yypt-13 : yypt+1]
		//line parser.go.y:165
		{
			yyVAL.stmt = &ast.TryStmt{Try: yyDollar[3].compstmt, Var: yyDollar[6].tok.Lit, Catch: yyDollar[8].compstmt, Finally: yyDollar[12].compstmt}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 20:
		yyDollar = yyS[yypt-12 : yypt+1]
		//line parser.go.y:170
		{
			yyVAL.stmt = &ast.TryStmt{Try: yyDollar[3].compstmt, Catch: yyDollar[7].compstmt, Finally: yyDollar[11].compstmt}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 21:
		yyDollar = yyS[yypt-9 : yypt+1]
		//line parser.go.y:175
		{
			yyVAL.stmt = &ast.TryStmt{Try: yyDollar[3].compstmt, Var: yyDollar[6].tok.Lit, Catch: yyDollar[8].compstmt}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 22:
		yyDollar = yyS[yypt-8 : yypt+1]
		//line parser.go.y:180
		{
			yyVAL.stmt = &ast.TryStmt{Try: yyDollar[3].compstmt, Catch: yyDollar[7].compstmt}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 23:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:185
		{
			yyVAL.stmt = &ast.SwitchStmt{Expr: yyDollar[2].expr, Cases: yyDollar[4].stmt_cases}
			yyVAL.stmt.SetPosition(yyDollar[1].tok.Position())
		}
	case 24:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:190
		{
			yyVAL.stmt = &ast.ExprStmt{Expr: yyDollar[1].expr}
			yyVAL.stmt.SetPosition(yyDollar[1].expr.Position())
		}
	case 25:
		yyDollar = yyS[yypt-7 : yypt+1]
		//line parser.go.y:198
		{
			yyDollar[1].stmt_if.(*ast.IfStmt).ElseIf = append(yyDollar[1].stmt_if.(*ast.IfStmt).ElseIf, &ast.IfStmt{If: yyDollar[4].expr, Then: yyDollar[6].compstmt})
			yyVAL.stmt_if.SetPosition(yyDollar[1].stmt_if.Position())
		}
	case 26:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:203
		{
			if yyVAL.stmt_if.(*ast.IfStmt).Else != nil {
				yylex.Error("multiple else statement")
			} else {
				yyVAL.stmt_if.(*ast.IfStmt).Else = append(yyVAL.stmt_if.(*ast.IfStmt).Else, yyDollar[4].compstmt...)
			}
			yyVAL.stmt_if.SetPosition(yyDollar[1].stmt_if.Position())
		}
	case 27:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:212
		{
			yyVAL.stmt_if = &ast.IfStmt{If: yyDollar[2].expr, Then: yyDollar[4].compstmt, Else: nil}
			yyVAL.stmt_if.SetPosition(yyDollar[1].tok.Position())
		}
	case 28:
		yyDollar = yyS[yypt-0 : yypt+1]
		//line parser.go.y:218
		{
			yyVAL.stmt_cases = []ast.Stmt{}
		}
	case 29:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:222
		{
			yyVAL.stmt_cases = []ast.Stmt{yyDollar[2].stmt_case}
		}
	case 30:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:226
		{
			yyVAL.stmt_cases = []ast.Stmt{yyDollar[2].stmt_default}
		}
	case 31:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:230
		{
			yyVAL.stmt_cases = append(yyDollar[1].stmt_cases, yyDollar[2].stmt_case)
		}
	case 32:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:234
		{
			for _, stmt := range yyDollar[1].stmt_cases {
				if _, ok := stmt.(*ast.DefaultStmt); ok {
					yylex.Error("multiple default statement")
				}
			}
			yyVAL.stmt_cases = append(yyDollar[1].stmt_cases, yyDollar[2].stmt_default)
		}
	case 33:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:245
		{
			yyVAL.stmt_case = &ast.CaseStmt{Expr: yyDollar[2].expr, Stmts: yyDollar[5].compstmt}
		}
	case 34:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:251
		{
			yyVAL.stmt_default = &ast.DefaultStmt{Stmts: yyDollar[4].compstmt}
		}
	case 35:
		yyDollar = yyS[yypt-0 : yypt+1]
		//line parser.go.y:256
		{
			yyVAL.array_count = ast.ArrayCount{Count: 0}
		}
	case 36:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:260
		{
			yyVAL.array_count = ast.ArrayCount{Count: 1}
		}
	case 37:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:264
		{
			yyVAL.array_count.Count = yyVAL.array_count.Count + 1
		}
	case 38:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:270
		{
			yyVAL.expr_pair = &ast.PairExpr{Key: yyDollar[1].tok.Lit, Value: yyDollar[3].expr}
		}
	case 39:
		yyDollar = yyS[yypt-0 : yypt+1]
		//line parser.go.y:275
		{
			yyVAL.expr_pairs = []ast.Expr{}
		}
	case 40:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:279
		{
			yyVAL.expr_pairs = []ast.Expr{yyDollar[1].expr_pair}
		}
	case 41:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:283
		{
			yyVAL.expr_pairs = append(yyDollar[1].expr_pairs, yyDollar[4].expr_pair)
		}
	case 42:
		yyDollar = yyS[yypt-0 : yypt+1]
		//line parser.go.y:288
		{
			yyVAL.expr_idents = []string{}
		}
	case 43:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:292
		{
			yyVAL.expr_idents = []string{yyDollar[1].tok.Lit}
		}
	case 44:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:296
		{
			yyVAL.expr_idents = append(yyDollar[1].expr_idents, yyDollar[4].tok.Lit)
		}
	case 45:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:302
		{
			yyVAL.expr_type = yyDollar[1].tok.Lit
		}
	case 46:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:306
		{
			yyVAL.expr_type = yyVAL.expr_type + "." + yyDollar[3].tok.Lit
		}
	case 47:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:311
		{
			yyVAL.expr_lets = &ast.LetsExpr{Lhss: yyDollar[1].expr_many, Operator: "=", Rhss: yyDollar[3].expr_many}
		}
	case 48:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:317
		{
			yyVAL.expr_many = []ast.Expr{yyDollar[1].expr}
		}
	case 49:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:321
		{
			yyVAL.expr_many = append(yyDollar[1].exprs, yyDollar[4].expr)
		}
	case 50:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:325
		{
			yyVAL.expr_many = append(yyDollar[1].exprs, &ast.IdentExpr{Lit: yyDollar[4].tok.Lit})
		}
	case 51:
		yyDollar = yyS[yypt-0 : yypt+1]
		//line parser.go.y:330
		{
			yyVAL.exprs = nil
		}
	case 52:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:334
		{
			yyVAL.exprs = []ast.Expr{yyDollar[1].expr}
		}
	case 53:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:338
		{
			yyVAL.exprs = append(yyDollar[1].exprs, yyDollar[4].expr)
		}
	case 54:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:342
		{
			yyVAL.exprs = append(yyDollar[1].exprs, &ast.IdentExpr{Lit: yyDollar[4].tok.Lit})
		}
	case 55:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:348
		{
			yyVAL.expr = &ast.IdentExpr{Lit: yyDollar[1].tok.Lit}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 56:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:353
		{
			yyVAL.expr = &ast.NumberExpr{Lit: yyDollar[1].tok.Lit}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 57:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:358
		{
			yyVAL.expr = &ast.UnaryExpr{Operator: "-", Expr: yyDollar[2].expr}
			yyVAL.expr.SetPosition(yyDollar[2].expr.Position())
		}
	case 58:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:363
		{
			yyVAL.expr = &ast.UnaryExpr{Operator: "!", Expr: yyDollar[2].expr}
			yyVAL.expr.SetPosition(yyDollar[2].expr.Position())
		}
	case 59:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:368
		{
			yyVAL.expr = &ast.UnaryExpr{Operator: "^", Expr: yyDollar[2].expr}
			yyVAL.expr.SetPosition(yyDollar[2].expr.Position())
		}
	case 60:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:373
		{
			yyVAL.expr = &ast.AddrExpr{Expr: &ast.IdentExpr{Lit: yyDollar[2].tok.Lit}}
			yyVAL.expr.SetPosition(yyDollar[2].tok.Position())
		}
	case 61:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:378
		{
			yyVAL.expr = &ast.AddrExpr{Expr: &ast.MemberExpr{Expr: yyDollar[2].expr, Name: yyDollar[4].tok.Lit}}
			yyVAL.expr.SetPosition(yyDollar[2].expr.Position())
		}
	case 62:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:383
		{
			yyVAL.expr = &ast.DerefExpr{Expr: &ast.IdentExpr{Lit: yyDollar[2].tok.Lit}}
			yyVAL.expr.SetPosition(yyDollar[2].tok.Position())
		}
	case 63:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:388
		{
			yyVAL.expr = &ast.DerefExpr{Expr: &ast.MemberExpr{Expr: yyDollar[2].expr, Name: yyDollar[4].tok.Lit}}
			yyVAL.expr.SetPosition(yyDollar[2].expr.Position())
		}
	case 64:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:393
		{
			yyVAL.expr = &ast.StringExpr{Lit: yyDollar[1].tok.Lit}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 65:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:398
		{
			yyVAL.expr = &ast.ConstExpr{Value: yyDollar[1].tok.Lit}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 66:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:403
		{
			yyVAL.expr = &ast.ConstExpr{Value: yyDollar[1].tok.Lit}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 67:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:408
		{
			yyVAL.expr = &ast.ConstExpr{Value: yyDollar[1].tok.Lit}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 68:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:413
		{
			yyVAL.expr = &ast.TernaryOpExpr{Expr: yyDollar[1].expr, Lhs: yyDollar[3].expr, Rhs: yyDollar[5].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 69:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:418
		{
			yyVAL.expr = &ast.MemberExpr{Expr: yyDollar[1].expr, Name: yyDollar[3].tok.Lit}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 70:
		yyDollar = yyS[yypt-7 : yypt+1]
		//line parser.go.y:423
		{
			yyVAL.expr = &ast.FuncExpr{Params: yyDollar[3].expr_idents, Stmts: yyDollar[6].compstmt}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 71:
		yyDollar = yyS[yypt-8 : yypt+1]
		//line parser.go.y:428
		{
			yyVAL.expr = &ast.FuncExpr{Params: yyDollar[3].expr_idents, Stmts: yyDollar[7].compstmt, VarArg: true}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 72:
		yyDollar = yyS[yypt-8 : yypt+1]
		//line parser.go.y:433
		{
			yyVAL.expr = &ast.FuncExpr{Name: yyDollar[2].tok.Lit, Params: yyDollar[4].expr_idents, Stmts: yyDollar[7].compstmt}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 73:
		yyDollar = yyS[yypt-9 : yypt+1]
		//line parser.go.y:438
		{
			yyVAL.expr = &ast.FuncExpr{Name: yyDollar[2].tok.Lit, Params: yyDollar[4].expr_idents, Stmts: yyDollar[8].compstmt, VarArg: true}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 74:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:443
		{
			yyVAL.expr = &ast.ArrayExpr{Exprs: yyDollar[3].exprs}
			if l, ok := yylex.(*Lexer); ok {
				yyVAL.expr.SetPosition(l.pos)
			}
		}
	case 75:
		yyDollar = yyS[yypt-6 : yypt+1]
		//line parser.go.y:448
		{
			yyVAL.expr = &ast.ArrayExpr{Exprs: yyDollar[3].exprs}
			if l, ok := yylex.(*Lexer); ok {
				yyVAL.expr.SetPosition(l.pos)
			}
		}
	case 76:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:453
		{
			mapExpr := make(map[string]ast.Expr)
			for _, v := range yyDollar[3].expr_pairs {
				mapExpr[v.(*ast.PairExpr).Key] = v.(*ast.PairExpr).Value
			}
			yyVAL.expr = &ast.MapExpr{MapExpr: mapExpr}
			if l, ok := yylex.(*Lexer); ok {
				yyVAL.expr.SetPosition(l.pos)
			}
		}
	case 77:
		yyDollar = yyS[yypt-6 : yypt+1]
		//line parser.go.y:462
		{
			mapExpr := make(map[string]ast.Expr)
			for _, v := range yyDollar[3].expr_pairs {
				mapExpr[v.(*ast.PairExpr).Key] = v.(*ast.PairExpr).Value
			}
			yyVAL.expr = &ast.MapExpr{MapExpr: mapExpr}
			if l, ok := yylex.(*Lexer); ok {
				yyVAL.expr.SetPosition(l.pos)
			}
		}
	case 78:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:471
		{
			yyVAL.expr = &ast.ParenExpr{SubExpr: yyDollar[2].expr}
			if l, ok := yylex.(*Lexer); ok {
				yyVAL.expr.SetPosition(l.pos)
			}
		}
	case 79:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:476
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "+", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 80:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:481
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "-", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 81:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:486
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "*", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 82:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:491
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "/", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 83:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:496
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "%", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 84:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:501
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "**", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 85:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:506
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "<<", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 86:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:511
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: ">>", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 87:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:516
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "==", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 88:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:521
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "!=", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 89:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:526
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: ">", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 90:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:531
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: ">=", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 91:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:536
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "<", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 92:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:541
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "<=", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 93:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:546
		{
			yyVAL.expr = &ast.AssocExpr{Lhs: yyDollar[1].expr, Operator: "+=", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 94:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:551
		{
			yyVAL.expr = &ast.AssocExpr{Lhs: yyDollar[1].expr, Operator: "-=", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 95:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:556
		{
			yyVAL.expr = &ast.AssocExpr{Lhs: yyDollar[1].expr, Operator: "*=", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 96:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:561
		{
			yyVAL.expr = &ast.AssocExpr{Lhs: yyDollar[1].expr, Operator: "/=", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 97:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:566
		{
			yyVAL.expr = &ast.AssocExpr{Lhs: yyDollar[1].expr, Operator: "&=", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 98:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:571
		{
			yyVAL.expr = &ast.AssocExpr{Lhs: yyDollar[1].expr, Operator: "|=", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 99:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:576
		{
			yyVAL.expr = &ast.AssocExpr{Lhs: yyDollar[1].expr, Operator: "++"}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 100:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:581
		{
			yyVAL.expr = &ast.AssocExpr{Lhs: yyDollar[1].expr, Operator: "--"}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 101:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:586
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "|", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 102:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:591
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "||", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 103:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:596
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "&", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 104:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:601
		{
			yyVAL.expr = &ast.BinOpExpr{Lhs: yyDollar[1].expr, Operator: "&&", Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 105:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:606
		{
			yyVAL.expr = &ast.CallExpr{Name: yyDollar[1].tok.Lit, SubExprs: yyDollar[3].exprs, VarArg: true}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 106:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:611
		{
			yyVAL.expr = &ast.CallExpr{Name: yyDollar[1].tok.Lit, SubExprs: yyDollar[3].exprs}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 107:
		yyDollar = yyS[yypt-6 : yypt+1]
		//line parser.go.y:616
		{
			yyVAL.expr = &ast.CallExpr{Name: yyDollar[2].tok.Lit, SubExprs: yyDollar[4].exprs, VarArg: true, Go: true}
			yyVAL.expr.SetPosition(yyDollar[2].tok.Position())
		}
	case 108:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:621
		{
			yyVAL.expr = &ast.CallExpr{Name: yyDollar[2].tok.Lit, SubExprs: yyDollar[4].exprs, Go: true}
			yyVAL.expr.SetPosition(yyDollar[2].tok.Position())
		}
	case 109:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:626
		{
			yyVAL.expr = &ast.AnonCallExpr{Expr: yyDollar[1].expr, SubExprs: yyDollar[3].exprs, VarArg: true}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 110:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:631
		{
			yyVAL.expr = &ast.AnonCallExpr{Expr: yyDollar[1].expr, SubExprs: yyDollar[3].exprs}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 111:
		yyDollar = yyS[yypt-6 : yypt+1]
		//line parser.go.y:636
		{
			yyVAL.expr = &ast.AnonCallExpr{Expr: yyDollar[2].expr, SubExprs: yyDollar[4].exprs, VarArg: true, Go: true}
			yyVAL.expr.SetPosition(yyDollar[2].expr.Position())
		}
	case 112:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:641
		{
			yyVAL.expr = &ast.AnonCallExpr{Expr: yyDollar[2].expr, SubExprs: yyDollar[4].exprs, Go: true}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 113:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:646
		{
			yyVAL.expr = &ast.ItemExpr{Value: &ast.IdentExpr{Lit: yyDollar[1].tok.Lit}, Index: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 114:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:651
		{
			yyVAL.expr = &ast.ItemExpr{Value: yyDollar[1].expr, Index: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 115:
		yyDollar = yyS[yypt-6 : yypt+1]
		//line parser.go.y:656
		{
			yyVAL.expr = &ast.SliceExpr{Value: &ast.IdentExpr{Lit: yyDollar[1].tok.Lit}, Begin: yyDollar[3].expr, End: yyDollar[5].expr}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 116:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:661
		{
			yyVAL.expr = &ast.SliceExpr{Value: &ast.IdentExpr{Lit: yyDollar[1].tok.Lit}, Begin: yyDollar[3].expr, End: nil}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 117:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:666
		{
			yyVAL.expr = &ast.SliceExpr{Value: &ast.IdentExpr{Lit: yyDollar[1].tok.Lit}, Begin: nil, End: yyDollar[4].expr}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 118:
		yyDollar = yyS[yypt-6 : yypt+1]
		//line parser.go.y:671
		{
			yyVAL.expr = &ast.SliceExpr{Value: yyDollar[1].expr, Begin: yyDollar[3].expr, End: yyDollar[5].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 119:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:676
		{
			yyVAL.expr = &ast.SliceExpr{Value: yyDollar[1].expr, Begin: yyDollar[3].expr, End: nil}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 120:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:681
		{
			yyVAL.expr = &ast.SliceExpr{Value: yyDollar[1].expr, Begin: nil, End: yyDollar[4].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 121:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:686
		{
			yyVAL.expr = &ast.LenExpr{Expr: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 122:
		yyDollar = yyS[yypt-4 : yypt+1]
		//line parser.go.y:691
		{
			yyVAL.expr = &ast.NewExpr{Type: yyDollar[3].expr_type}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 123:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:696
		{
			yyVAL.expr = &ast.MakeChanExpr{Type: yyDollar[4].expr_type, SizeExpr: nil}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 124:
		yyDollar = yyS[yypt-7 : yypt+1]
		//line parser.go.y:701
		{
			yyVAL.expr = &ast.MakeChanExpr{Type: yyDollar[4].expr_type, SizeExpr: yyDollar[6].expr}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 125:
		yyDollar = yyS[yypt-5 : yypt+1]
		//line parser.go.y:706
		{
			yyVAL.expr = &ast.MakeExpr{Dimensions: yyDollar[3].array_count.Count, Type: yyDollar[4].expr_type}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 126:
		yyDollar = yyS[yypt-7 : yypt+1]
		//line parser.go.y:711
		{
			yyVAL.expr = &ast.MakeExpr{Dimensions: yyDollar[3].array_count.Count, Type: yyDollar[4].expr_type, LenExpr: yyDollar[6].expr}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 127:
		yyDollar = yyS[yypt-9 : yypt+1]
		//line parser.go.y:716
		{
			yyVAL.expr = &ast.MakeExpr{Dimensions: yyDollar[3].array_count.Count, Type: yyDollar[4].expr_type, LenExpr: yyDollar[6].expr, CapExpr: yyDollar[8].expr}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 128:
		yyDollar = yyS[yypt-7 : yypt+1]
		//line parser.go.y:721
		{
			yyVAL.expr = &ast.MakeTypeExpr{Name: yyDollar[4].tok.Lit, Type: yyDollar[6].expr}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 129:
		yyDollar = yyS[yypt-3 : yypt+1]
		//line parser.go.y:726
		{
			yyVAL.expr = &ast.ChanExpr{Lhs: yyDollar[1].expr, Rhs: yyDollar[3].expr}
			yyVAL.expr.SetPosition(yyDollar[1].expr.Position())
		}
	case 130:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:731
		{
			yyVAL.expr = &ast.ChanExpr{Rhs: yyDollar[2].expr}
			yyVAL.expr.SetPosition(yyDollar[2].expr.Position())
		}
	case 131:
		yyDollar = yyS[yypt-6 : yypt+1]
		//line parser.go.y:736
		{
			yyVAL.expr = &ast.DeleteExpr{MapExpr: yyDollar[3].expr, KeyExpr: yyDollar[5].expr}
			yyVAL.expr.SetPosition(yyDollar[1].tok.Position())
		}
	case 134:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:747
		{
		}
	case 135:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:750
		{
		}
	case 136:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:755
		{
		}
	case 137:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:758
		{
		}
	}
	goto yystack /* stack new state and value */
}
