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
const UNARY = 57395

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

//line parser.go.y:757

//line yacctab:1
var yyExca = [...]int{
	-1, 0,
	1, 3,
	-2, 131,
	-1, 1,
	1, -1,
	-2, 0,
	-1, 2,
	56, 51,
	-2, 1,
	-1, 10,
	56, 52,
	-2, 24,
	-1, 44,
	56, 51,
	-2, 132,
	-1, 86,
	66, 3,
	-2, 131,
	-1, 89,
	56, 52,
	-2, 48,
	-1, 90,
	16, 43,
	56, 43,
	-2, 55,
	-1, 92,
	66, 3,
	-2, 131,
	-1, 99,
	1, 60,
	8, 60,
	45, 60,
	46, 60,
	53, 60,
	55, 60,
	56, 60,
	65, 60,
	66, 60,
	67, 60,
	69, 60,
	75, 60,
	77, 60,
	-2, 55,
	-1, 101,
	1, 62,
	8, 62,
	45, 62,
	46, 62,
	53, 62,
	55, 62,
	56, 62,
	65, 62,
	66, 62,
	67, 62,
	69, 62,
	75, 62,
	77, 62,
	-2, 55,
	-1, 130,
	17, 0,
	18, 0,
	-2, 87,
	-1, 131,
	17, 0,
	18, 0,
	-2, 88,
	-1, 151,
	56, 52,
	-2, 48,
	-1, 153,
	66, 3,
	-2, 131,
	-1, 155,
	66, 3,
	-2, 131,
	-1, 157,
	66, 1,
	-2, 39,
	-1, 160,
	66, 3,
	-2, 131,
	-1, 186,
	66, 3,
	-2, 131,
	-1, 233,
	56, 53,
	-2, 49,
	-1, 234,
	1, 50,
	45, 50,
	46, 50,
	53, 50,
	56, 54,
	66, 50,
	67, 50,
	77, 50,
	-2, 55,
	-1, 243,
	1, 54,
	8, 54,
	45, 54,
	46, 54,
	56, 54,
	66, 54,
	67, 54,
	69, 54,
	75, 54,
	77, 54,
	-2, 55,
	-1, 245,
	66, 3,
	-2, 131,
	-1, 247,
	66, 3,
	-2, 131,
	-1, 262,
	66, 3,
	-2, 131,
	-1, 272,
	1, 108,
	8, 108,
	45, 108,
	46, 108,
	53, 108,
	55, 108,
	56, 108,
	65, 108,
	66, 108,
	67, 108,
	69, 108,
	75, 108,
	77, 108,
	-2, 106,
	-1, 274,
	1, 112,
	8, 112,
	45, 112,
	46, 112,
	53, 112,
	55, 112,
	56, 112,
	65, 112,
	66, 112,
	67, 112,
	69, 112,
	75, 112,
	77, 112,
	-2, 110,
	-1, 288,
	66, 3,
	-2, 131,
	-1, 293,
	66, 3,
	-2, 131,
	-1, 294,
	66, 3,
	-2, 131,
	-1, 299,
	1, 107,
	8, 107,
	45, 107,
	46, 107,
	53, 107,
	55, 107,
	56, 107,
	65, 107,
	66, 107,
	67, 107,
	69, 107,
	75, 107,
	77, 107,
	-2, 105,
	-1, 300,
	1, 111,
	8, 111,
	45, 111,
	46, 111,
	53, 111,
	55, 111,
	56, 111,
	65, 111,
	66, 111,
	67, 111,
	69, 111,
	75, 111,
	77, 111,
	-2, 109,
	-1, 306,
	66, 3,
	-2, 131,
	-1, 307,
	66, 3,
	-2, 131,
	-1, 310,
	45, 3,
	46, 3,
	66, 3,
	-2, 131,
	-1, 314,
	66, 3,
	-2, 131,
	-1, 322,
	45, 3,
	46, 3,
	66, 3,
	-2, 131,
	-1, 335,
	66, 3,
	-2, 131,
	-1, 336,
	66, 3,
	-2, 131,
}

const yyPrivate = 57344

const yyLast = 2789

var yyAct = [...]int{

	82, 173, 252, 10, 46, 253, 179, 6, 226, 300,
	299, 220, 1, 227, 41, 170, 83, 7, 295, 89,
	11, 93, 6, 87, 96, 97, 98, 100, 102, 81,
	104, 263, 7, 258, 95, 238, 107, 109, 95, 91,
	94, 113, 112, 115, 176, 10, 111, 110, 278, 119,
	120, 267, 122, 123, 124, 125, 126, 127, 128, 129,
	130, 131, 132, 133, 134, 135, 136, 137, 138, 139,
	140, 141, 218, 232, 142, 143, 144, 145, 2, 147,
	149, 151, 43, 6, 65, 66, 67, 68, 69, 70,
	180, 174, 146, 7, 56, 159, 165, 340, 339, 156,
	103, 150, 273, 79, 181, 162, 332, 183, 169, 164,
	329, 178, 105, 106, 53, 54, 55, 280, 151, 287,
	171, 78, 336, 50, 184, 328, 75, 77, 271, 73,
	325, 226, 324, 321, 277, 265, 279, 311, 187, 305,
	304, 282, 65, 66, 67, 68, 69, 70, 226, 194,
	152, 298, 56, 276, 229, 197, 269, 216, 10, 201,
	202, 79, 151, 208, 249, 157, 196, 212, 198, 274,
	246, 244, 190, 203, 205, 217, 152, 254, 255, 78,
	288, 50, 204, 118, 75, 77, 233, 73, 228, 230,
	237, 223, 224, 185, 239, 272, 242, 188, 251, 235,
	199, 154, 264, 335, 314, 118, 307, 294, 293, 262,
	256, 152, 259, 257, 158, 153, 21, 22, 28, 92,
	152, 32, 117, 270, 215, 118, 281, 152, 114, 290,
	209, 195, 222, 161, 80, 38, 29, 30, 31, 191,
	254, 255, 207, 5, 8, 334, 330, 250, 45, 286,
	219, 221, 85, 155, 118, 289, 174, 275, 284, 36,
	285, 39, 40, 236, 37, 231, 180, 242, 47, 214,
	297, 213, 23, 27, 121, 292, 84, 34, 301, 182,
	33, 302, 303, 24, 25, 26, 35, 4, 45, 116,
	172, 44, 88, 206, 17, 3, 0, 266, 0, 268,
	0, 308, 0, 0, 0, 0, 312, 313, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 327, 319,
	320, 0, 0, 323, 0, 0, 0, 326, 0, 0,
	0, 0, 0, 0, 0, 331, 0, 0, 59, 60,
	62, 64, 74, 76, 0, 0, 0, 0, 337, 338,
	0, 0, 65, 66, 67, 68, 69, 70, 0, 0,
	71, 72, 56, 57, 58, 0, 0, 0, 0, 310,
	0, 79, 0, 0, 0, 49, 0, 317, 61, 63,
	51, 52, 53, 54, 55, 0, 0, 0, 322, 78,
	0, 50, 0, 0, 75, 77, 316, 73, 59, 60,
	62, 64, 74, 76, 0, 0, 0, 0, 0, 0,
	0, 0, 65, 66, 67, 68, 69, 70, 0, 0,
	71, 72, 56, 57, 58, 0, 0, 0, 0, 0,
	0, 79, 0, 0, 0, 49, 211, 0, 61, 63,
	51, 52, 53, 54, 55, 0, 0, 0, 0, 78,
	210, 50, 0, 0, 75, 77, 0, 73, 59, 60,
	62, 64, 74, 76, 0, 0, 0, 0, 0, 0,
	0, 0, 65, 66, 67, 68, 69, 70, 0, 0,
	71, 72, 56, 57, 58, 0, 0, 0, 0, 0,
	0, 79, 0, 0, 0, 49, 193, 0, 61, 63,
	51, 52, 53, 54, 55, 0, 0, 0, 0, 78,
	192, 50, 0, 0, 75, 77, 0, 73, 21, 22,
	28, 0, 0, 32, 14, 9, 15, 42, 0, 18,
	0, 0, 0, 0, 0, 0, 0, 38, 29, 30,
	31, 16, 19, 0, 0, 0, 0, 0, 0, 0,
	0, 12, 13, 0, 0, 0, 0, 0, 20, 0,
	0, 36, 0, 39, 40, 0, 37, 0, 0, 0,
	0, 0, 0, 0, 23, 27, 0, 0, 0, 34,
	0, 6, 33, 0, 0, 24, 25, 26, 35, 0,
	0, 7, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	333, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	318, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	315, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	309, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	306, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 291, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 283, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 261, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 248, 78, 0, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	247, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	245, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 241, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	225, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	189, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	186, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	175, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	163, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	160, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 48, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 0, 50, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 0, 50, 0, 0, 75, 177,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 0,
	0, 0, 0, 78, 0, 168, 0, 0, 75, 77,
	0, 73, 59, 60, 62, 64, 74, 76, 0, 0,
	0, 0, 0, 0, 0, 0, 65, 66, 67, 68,
	69, 70, 0, 0, 71, 72, 56, 57, 58, 0,
	0, 0, 0, 0, 0, 79, 0, 0, 0, 49,
	0, 0, 61, 63, 51, 52, 53, 54, 55, 59,
	60, 62, 64, 78, 76, 167, 0, 0, 75, 77,
	0, 73, 0, 65, 66, 67, 68, 69, 70, 0,
	0, 71, 72, 56, 57, 58, 0, 0, 0, 0,
	0, 0, 79, 0, 0, 0, 0, 0, 0, 61,
	63, 51, 52, 53, 54, 55, 0, 0, 0, 0,
	78, 0, 50, 0, 0, 75, 77, 0, 73, 21,
	22, 200, 0, 0, 32, 14, 9, 15, 42, 0,
	18, 0, 0, 0, 0, 0, 0, 0, 38, 29,
	30, 31, 16, 19, 0, 0, 0, 0, 0, 0,
	0, 0, 12, 13, 0, 0, 0, 0, 0, 20,
	0, 0, 36, 0, 39, 40, 0, 37, 0, 0,
	0, 0, 0, 0, 0, 23, 27, 0, 0, 0,
	34, 0, 0, 33, 0, 0, 24, 25, 26, 35,
	59, 60, 62, 64, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 65, 66, 67, 68, 69, 70,
	0, 0, 71, 72, 56, 57, 58, 0, 0, 0,
	0, 0, 0, 79, 0, 0, 0, 0, 0, 0,
	61, 63, 51, 52, 53, 54, 55, 0, 0, 0,
	0, 78, 0, 50, 0, 0, 75, 77, 0, 73,
	21, 22, 28, 0, 0, 32, 14, 9, 15, 42,
	0, 18, 0, 0, 0, 0, 0, 0, 0, 38,
	29, 30, 31, 16, 19, 0, 0, 0, 0, 0,
	0, 0, 0, 12, 13, 0, 0, 0, 0, 0,
	20, 0, 0, 36, 0, 39, 40, 0, 37, 0,
	0, 0, 0, 0, 0, 0, 23, 27, 0, 62,
	64, 34, 0, 0, 33, 0, 0, 24, 25, 26,
	35, 65, 66, 67, 68, 69, 70, 0, 0, 71,
	72, 56, 57, 58, 0, 0, 0, 0, 0, 0,
	79, 0, 0, 0, 0, 0, 0, 61, 63, 51,
	52, 53, 54, 55, 0, 0, 0, 0, 78, 0,
	50, 0, 0, 75, 77, 0, 73, 65, 66, 67,
	68, 69, 70, 0, 0, 71, 72, 56, 0, 0,
	0, 0, 0, 0, 0, 0, 79, 0, 0, 0,
	0, 0, 0, 0, 0, 51, 52, 53, 54, 55,
	243, 22, 28, 0, 78, 32, 50, 0, 0, 75,
	77, 0, 73, 0, 0, 0, 0, 0, 0, 38,
	29, 30, 31, 0, 0, 0, 0, 0, 0, 0,
	21, 22, 28, 0, 0, 32, 0, 0, 0, 0,
	0, 0, 0, 36, 0, 39, 40, 0, 37, 38,
	29, 30, 31, 0, 0, 0, 23, 27, 0, 0,
	0, 34, 0, 0, 33, 296, 0, 24, 25, 26,
	35, 0, 0, 36, 0, 39, 40, 0, 37, 0,
	0, 0, 0, 21, 22, 28, 23, 27, 32, 0,
	0, 34, 0, 0, 33, 260, 0, 24, 25, 26,
	35, 0, 38, 29, 30, 31, 0, 0, 0, 0,
	0, 0, 0, 21, 22, 28, 0, 0, 32, 0,
	0, 0, 0, 0, 0, 0, 36, 0, 39, 40,
	0, 37, 38, 29, 30, 31, 0, 0, 0, 23,
	27, 0, 0, 0, 34, 0, 0, 33, 240, 0,
	24, 25, 26, 35, 0, 0, 36, 0, 39, 40,
	0, 37, 0, 0, 166, 0, 21, 22, 28, 23,
	27, 32, 0, 0, 34, 0, 0, 33, 0, 0,
	24, 25, 26, 35, 0, 38, 29, 30, 31, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 243,
	22, 28, 0, 0, 32, 0, 0, 0, 0, 36,
	0, 39, 40, 0, 37, 0, 0, 148, 38, 29,
	30, 31, 23, 27, 0, 0, 0, 34, 0, 0,
	33, 0, 0, 24, 25, 26, 35, 0, 0, 0,
	0, 0, 36, 0, 39, 40, 0, 37, 0, 0,
	0, 0, 234, 22, 28, 23, 27, 32, 0, 0,
	34, 0, 0, 33, 0, 0, 24, 25, 26, 35,
	0, 38, 29, 30, 31, 0, 0, 0, 0, 0,
	0, 0, 108, 22, 28, 0, 0, 32, 0, 0,
	0, 0, 0, 0, 0, 36, 0, 39, 40, 0,
	37, 38, 29, 30, 31, 0, 0, 0, 23, 27,
	0, 0, 0, 34, 0, 0, 33, 0, 0, 24,
	25, 26, 35, 0, 0, 36, 0, 39, 40, 0,
	37, 0, 0, 0, 0, 101, 22, 28, 23, 27,
	32, 0, 0, 34, 0, 0, 33, 0, 0, 24,
	25, 26, 35, 0, 38, 29, 30, 31, 0, 0,
	0, 0, 0, 0, 0, 99, 22, 28, 0, 0,
	32, 0, 0, 0, 0, 0, 0, 0, 36, 0,
	39, 40, 0, 37, 38, 29, 30, 31, 0, 0,
	0, 23, 27, 0, 0, 0, 34, 0, 0, 33,
	0, 0, 24, 25, 26, 35, 0, 0, 36, 0,
	39, 40, 0, 37, 0, 0, 0, 0, 90, 22,
	28, 23, 27, 32, 0, 0, 34, 0, 0, 33,
	0, 0, 24, 25, 26, 35, 0, 38, 29, 30,
	31, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 36, 0, 39, 40, 0, 37, 0, 0, 0,
	0, 0, 0, 0, 23, 27, 0, 0, 0, 86,
	0, 0, 33, 0, 0, 24, 25, 26, 35,
}
var yyPact = [...]int{

	-60, -1000, 2146, -60, -60, -1000, -1000, -1000, -1000, 264,
	1655, 181, -1000, -1000, 212, 212, 272, 238, 2714, 154,
	212, -34, -1000, 212, 212, 212, 2661, 2631, -1000, -1000,
	-1000, -1000, 26, -60, -60, 212, 2578, -27, -28, -32,
	212, 172, 212, -1000, 514, -1000, 169, -1000, 212, 212,
	270, 212, 212, 212, 212, 212, 212, 212, 212, 212,
	212, 212, 212, 212, 212, 212, 212, 212, 212, 212,
	212, -1000, -1000, 212, 212, 212, 212, 212, 2462, 212,
	212, 171, 1715, 1715, 150, 188, -60, 198, 28, 1595,
	-34, 180, -60, 1535, 212, 2409, 111, 111, 111, -34,
	1895, -34, 1835, 264, -59, 212, 250, 1475, -30, 1775,
	212, 262, 56, 1715, -60, 1415, -1000, 212, -60, 1715,
	1355, -1000, 53, 53, 111, 111, 111, 1715, 2236, 2236,
	2190, 2190, 2236, 2236, 2236, 2236, 1715, 1715, 1715, 1715,
	1715, 1715, 1715, 1942, 1715, 2073, 164, 441, 212, 1715,
	-1000, 1715, -60, -60, 212, -60, 134, 2015, 212, 212,
	-60, 212, 108, -60, 155, 381, 212, 267, 265, 149,
	264, 16, -45, -1000, 177, -1000, 212, 212, 1295, -62,
	-1000, 262, 86, 261, 4, 2548, -60, -1000, 259, 212,
	-40, -1000, -1000, 2379, 1235, 2495, 105, 1175, 104, -1000,
	177, 1115, 1055, 98, -1000, 218, 132, 195, -42, -1000,
	-1000, 2326, 995, -1000, -1000, 144, -44, 127, -60, -18,
	-60, 90, 212, 120, 94, -1000, 253, -1000, 78, -21,
	61, 170, -1000, 1715, -34, 75, -1000, 1715, -1000, 935,
	-1000, -1000, 1715, -34, -1000, -60, -1000, -60, 212, -1000,
	115, -1000, -1000, -1000, 212, 174, -1000, -1000, -1000, 875,
	-1000, -1000, -60, 143, 142, -57, 2296, -1000, 85, -1000,
	1715, -65, -1000, -66, -1000, -1000, -1000, 212, -1000, -1000,
	212, 212, -1000, -1000, 74, 73, 815, 141, -60, 755,
	-60, -1000, 71, -60, -60, 139, -1000, -1000, -1000, -1000,
	-1000, 695, 321, 635, -1000, -1000, -60, -60, 67, -60,
	-60, -1000, 66, 64, -60, -1000, -1000, 212, -1000, 59,
	44, 216, -60, -1000, -1000, -1000, 40, 575, -1000, 215,
	138, -1000, -1000, -1000, 57, -60, -60, 32, 31, -1000,
	-1000,
}
var yyPgo = [...]int{

	0, 12, 295, 244, 294, 5, 2, 293, 0, 14,
	20, 292, 1, 290, 4, 6, 279, 78, 287, 243,
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
	8, 17, 17, 18, 18, 19, 19,
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
	2, 0, 1, 1, 2, 1, 1,
}
var yyChk = [...]int{

	-1000, -1, -17, -2, -18, -19, 67, 77, -3, 11,
	-8, -10, 37, 38, 10, 12, 27, -4, 15, 28,
	44, 4, 5, 60, 71, 72, 73, 61, 6, 24,
	25, 26, 9, 68, 65, 74, 47, 52, 23, 49,
	50, -9, 13, -17, -18, -19, -14, 4, 53, 54,
	70, 59, 60, 61, 62, 63, 41, 42, 43, 17,
	18, 57, 19, 58, 20, 31, 32, 33, 34, 35,
	36, 39, 40, 76, 21, 73, 22, 74, 68, 50,
	53, -9, -8, -8, 4, 14, 65, -14, -11, -8,
	4, -10, 65, -8, 74, 68, -8, -8, -8, 4,
	-8, 4, -8, 74, 4, -17, -17, -8, 4, -8,
	74, 74, 74, -8, 56, -8, -3, 53, 56, -8,
	-8, 4, -8, -8, -8, -8, -8, -8, -8, -8,
	-8, -8, -8, -8, -8, -8, -8, -8, -8, -8,
	-8, -8, -8, -8, -8, -8, -9, -8, 55, -8,
	-10, -8, 56, 65, 13, 65, -1, -17, 16, 67,
	65, 53, -1, 65, -9, -8, 55, 70, 70, -14,
	74, -9, -13, -12, 6, 75, 74, 74, -8, -15,
	4, 48, -16, 51, 68, -17, 65, -10, -17, 55,
	8, 75, 69, 55, -8, -17, -1, -8, -1, 66,
	6, -8, -8, -1, -10, 66, -7, -17, 8, 75,
	69, 55, -8, 4, 4, 75, 8, -14, 56, -17,
	56, -17, 55, -9, -9, 75, 70, 75, -15, 68,
	-15, 4, 69, -8, 4, -1, 4, -8, 75, -8,
	69, 69, -8, 4, 66, 65, 66, 65, 67, 66,
	29, 66, -6, -5, 45, 46, -6, -5, 75, -8,
	69, 69, 65, 75, 75, 8, -17, 69, -17, 66,
	-8, 8, 75, 8, 75, 4, 75, 56, 69, 75,
	56, 56, 66, 69, -1, -1, -8, 4, 65, -8,
	55, 69, -1, 65, 65, 75, 69, -12, 66, 75,
	75, -8, -8, -8, 66, 66, 65, 65, -1, 55,
	-17, 66, -1, -1, 65, 75, 75, 56, 75, -1,
	-1, 66, -17, -1, 66, 66, -1, -8, 66, 66,
	30, -1, 66, 75, 30, 65, 65, -1, -1, 66,
	66,
}
var yyDef = [...]int{

	-2, -2, -2, 131, 132, 133, 135, 136, 4, 42,
	-2, 0, 9, 10, 51, 0, 0, 14, 42, 0,
	0, 55, 56, 0, 0, 0, 0, 0, 64, 65,
	66, 67, 0, 131, 131, 0, 0, 0, 0, 0,
	0, 0, 0, 2, -2, 134, 0, 43, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 99, 100, 0, 0, 0, 0, 51, 0, 0,
	51, 11, 52, 12, 0, 0, -2, 0, 0, -2,
	-2, 0, -2, 0, 51, 0, 57, 58, 59, -2,
	0, -2, 0, 42, 0, 51, 39, 0, 55, 0,
	0, 0, 35, 130, 131, 0, 5, 51, 131, 7,
	0, 69, 79, 80, 81, 82, 83, 84, 85, 86,
	-2, -2, 89, 90, 91, 92, 93, 94, 95, 96,
	97, 98, 101, 102, 103, 104, 0, 0, 0, 129,
	8, -2, 131, -2, 0, -2, 0, -2, 0, 0,
	-2, 51, 0, 28, 0, 0, 0, 0, 0, 0,
	42, 131, 131, 40, 0, 78, 51, 51, 0, 0,
	45, 0, 0, 0, 0, 0, -2, 6, 0, 0,
	0, 110, 114, 0, 0, 0, 0, 0, 0, 15,
	64, 0, 0, 0, 47, 0, 0, 0, 0, 106,
	113, 0, 0, 61, 63, 0, 0, 0, 131, 0,
	131, 0, 0, 0, 0, 121, 0, 122, 0, 0,
	0, 0, 36, -2, -2, 0, 44, 68, 109, 0,
	119, 120, 53, -2, 13, -2, 26, -2, 0, 18,
	0, 23, 31, 32, 0, 0, 29, 30, 105, 0,
	116, 117, -2, 0, 0, 0, 0, 74, 0, 76,
	38, 0, -2, 0, -2, 46, 123, 0, 37, 125,
	0, 0, 27, 118, 0, 0, 0, 0, -2, 0,
	131, 115, 0, -2, -2, 0, 75, 41, 77, -2,
	-2, 0, 0, 0, 25, 16, -2, -2, 0, 131,
	-2, 70, 0, 0, -2, 124, 126, 0, 128, 0,
	0, 22, -2, 34, 71, 72, 0, 0, 17, 21,
	0, 33, 73, 127, 0, -2, -2, 0, 0, 20,
	19,
}
var yyTok1 = [...]int{

	1, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	77, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	3, 3, 3, 71, 3, 3, 3, 63, 73, 3,
	74, 75, 61, 59, 56, 60, 70, 62, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 55, 67,
	58, 53, 57, 54, 3, 3, 3, 3, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	3, 68, 3, 69, 72, 3, 3, 3, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	3, 3, 3, 65, 76, 66,
}
var yyTok2 = [...]int{

	2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
	12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
	22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
	32, 33, 34, 35, 36, 37, 38, 39, 40, 41,
	42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
	52, 64,
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
	case 133:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:742
		{
		}
	case 134:
		yyDollar = yyS[yypt-2 : yypt+1]
		//line parser.go.y:745
		{
		}
	case 135:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:750
		{
		}
	case 136:
		yyDollar = yyS[yypt-1 : yypt+1]
		//line parser.go.y:753
		{
		}
	}
	goto yystack /* stack new state and value */
}
