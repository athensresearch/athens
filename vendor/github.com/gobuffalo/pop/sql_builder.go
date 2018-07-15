package pop

import (
	"fmt"
	"log"
	"regexp"
	"strings"
	"sync"

	"github.com/gobuffalo/pop/columns"
	"github.com/jmoiron/sqlx"
)

type sqlBuilder struct {
	Query      Query
	Model      *Model
	AddColumns []string
	sql        string
	args       []interface{}
}

func newSQLBuilder(q Query, m *Model, addColumns ...string) *sqlBuilder {
	return &sqlBuilder{
		Query:      q,
		Model:      m,
		AddColumns: addColumns,
		args:       []interface{}{},
	}
}

var (
	regexpMatchLimit    = regexp.MustCompile(`(?i).*\s+limit\s+[0-9]*(\s?,\s?[0-9]*)?$`)
	regexpMatchOffset   = regexp.MustCompile(`(?i).*\s+offset\s+[0-9]*$`)
	regexpMatchRowsOnly = regexp.MustCompile(`(?i).*\s+rows only`)
	regexpMatchNames    = regexp.MustCompile("(?i).*;+.*") // https://play.golang.org/p/FAmre5Sjin5
)

func hasLimitOrOffset(sqlString string) bool {
	trimmedSQL := strings.TrimSpace(sqlString)
	if regexpMatchLimit.MatchString(trimmedSQL) {
		return true
	}

	if regexpMatchOffset.MatchString(trimmedSQL) {
		return true
	}

	if regexpMatchRowsOnly.MatchString(trimmedSQL) {
		return true
	}

	return false
}

func (sq *sqlBuilder) String() string {
	if sq.sql == "" {
		sq.compile()
	}
	return sq.sql
}

func (sq *sqlBuilder) Args() []interface{} {
	if len(sq.args) == 0 {
		if len(sq.Query.RawSQL.Arguments) > 0 {
			sq.args = sq.Query.RawSQL.Arguments
		} else {
			sq.compile()
		}
	}
	return sq.args
}

var inRegex = regexp.MustCompile(`(?i)in\s*\(\s*\?\s*\)`)

func (sq *sqlBuilder) compile() {
	if sq.sql == "" {
		if sq.Query.RawSQL.Fragment != "" {
			if sq.Query.Paginator != nil && !hasLimitOrOffset(sq.Query.RawSQL.Fragment) {
				sq.sql = sq.buildPaginationClauses(sq.Query.RawSQL.Fragment)
			} else {
				if sq.Query.Paginator != nil {
					log.Println("Warning: Query already contains pagination")
				}
				sq.sql = sq.Query.RawSQL.Fragment
			}
		} else {
			sq.sql = sq.buildSelectSQL()
		}

		if inRegex.MatchString(sq.sql) {
			s, _, err := sqlx.In(sq.sql, sq.Args())
			if err == nil {
				sq.sql = s
			}
		}
		sq.sql = sq.Query.Connection.Dialect.TranslateSQL(sq.sql)
	}
}

func (sq *sqlBuilder) buildSelectSQL() string {
	cols := sq.buildColumns()

	fc := sq.buildfromClauses()

	sql := fmt.Sprintf("SELECT %s FROM %s", cols.Readable().SelectString(), fc)

	sql = sq.buildJoinClauses(sql)
	sql = sq.buildWhereClauses(sql)
	sql = sq.buildGroupClauses(sql)
	sql = sq.buildOrderClauses(sql)
	sql = sq.buildPaginationClauses(sql)

	return sql
}

func (sq *sqlBuilder) buildfromClauses() fromClauses {
	models := []*Model{
		sq.Model,
	}
	for _, mc := range sq.Query.belongsToThroughClauses {
		models = append(models, mc.Through)
	}

	fc := sq.Query.fromClauses
	for _, m := range models {
		tableName := m.TableName()
		asName := m.As
		if asName == "" {
			asName = strings.Replace(tableName, ".", "_", -1)
		}
		fc = append(fc, fromClause{
			From: tableName,
			As:   asName,
		})
	}

	return fc
}

func (sq *sqlBuilder) buildWhereClauses(sql string) string {
	mcs := sq.Query.belongsToThroughClauses
	for _, mc := range mcs {
		sq.Query.Where(fmt.Sprintf("%s.%s = ?", mc.Through.TableName(), mc.BelongsTo.associationName()), mc.BelongsTo.ID())
		sq.Query.Where(fmt.Sprintf("%s.id = %s.%s", sq.Model.TableName(), mc.Through.TableName(), sq.Model.associationName()))
	}

	wc := sq.Query.whereClauses
	if len(wc) > 0 {
		sql = fmt.Sprintf("%s WHERE %s", sql, wc.Join(" AND "))
		sq.args = append(sq.args, wc.Args()...)
	}
	return sql
}

func (sq *sqlBuilder) buildJoinClauses(sql string) string {
	oc := sq.Query.joinClauses
	if len(oc) > 0 {
		sql += " " + oc.String()
		for i := range oc {
			sq.args = append(sq.args, oc[i].Arguments...)
		}
	}

	return sql
}

func (sq *sqlBuilder) buildGroupClauses(sql string) string {
	gc := sq.Query.groupClauses
	if len(gc) > 0 {
		sql = fmt.Sprintf("%s GROUP BY %s", sql, gc.String())

		hc := sq.Query.havingClauses
		if len(hc) > 0 {
			sql = fmt.Sprintf("%s HAVING %s", sql, hc.String())
		}

		for i := range hc {
			sq.args = append(sq.args, hc[i].Arguments...)
		}
	}

	return sql
}

func (sq *sqlBuilder) buildOrderClauses(sql string) string {
	oc := sq.Query.orderClauses
	if len(oc) > 0 {
		orderSQL := oc.Join(", ")
		if regexpMatchNames.MatchString(orderSQL) {
			warningMsg := fmt.Sprintf("Warning: Order clause(s) contains invalid characters: %s", orderSQL)
			log.Println(warningMsg)
			return sql
		}

		sql = fmt.Sprintf("%s ORDER BY %s", sql, orderSQL)
		sq.args = append(sq.args, oc.Args()...)
	}
	return sql
}

func (sq *sqlBuilder) buildPaginationClauses(sql string) string {
	if sq.Query.limitResults > 0 && sq.Query.Paginator == nil {
		sql = fmt.Sprintf("%s LIMIT %d", sql, sq.Query.limitResults)
	}
	if sq.Query.Paginator != nil {
		sql = fmt.Sprintf("%s LIMIT %d", sql, sq.Query.Paginator.PerPage)
		sql = fmt.Sprintf("%s OFFSET %d", sql, sq.Query.Paginator.Offset)
	}
	return sql
}

var columnCache = map[string]columns.Columns{}
var columnCacheMutex = sync.Mutex{}

func (sq *sqlBuilder) buildColumns() columns.Columns {
	tableName := sq.Model.TableName()
	asName := sq.Model.As
	if asName == "" {
		asName = strings.Replace(tableName, ".", "_", -1)
	}
	acl := len(sq.AddColumns)
	if acl <= 0 {
		columnCacheMutex.Lock()
		cols, ok := columnCache[tableName]
		columnCacheMutex.Unlock()
		//if alias is different, remake columns
		if ok && cols.TableAlias == asName {
			return cols
		}
		cols = columns.ForStructWithAlias(sq.Model.Value, tableName, asName)
		columnCacheMutex.Lock()
		columnCache[tableName] = cols
		columnCacheMutex.Unlock()
		return cols
	}

	// acl > 0
	cols := columns.NewColumns("")
	cols.Add(sq.AddColumns...)
	return cols
}
