package fizz

import (
	"strings"
)

func (f fizzer) RawSql(sql string) {
	if !strings.HasSuffix(sql, ";") {
		sql += ";"
	}
	f.add(sql, nil)
}
