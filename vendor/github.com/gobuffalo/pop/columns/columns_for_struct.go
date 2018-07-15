package columns

import (
	"fmt"
	"reflect"
)

// ColumnsForStruct returns a Columns instance for
// the struct passed in.
//
// Deprecated: use ForStruct instead.
func ColumnsForStruct(s interface{}, tableName string) (columns Columns) {
	fmt.Println(`Warning: ColumnsForStruct is deprecated, and will be removed in a future version. Please use ForStruct instead.`)
	return ForStruct(s, tableName)
}

// ColumnsForStructWithAlias returns a Columns instance for the struct passed in.
// If the tableAlias is not empty, it will be used.
//
// Deprecated: use ForStructWithAlias instead.
func ColumnsForStructWithAlias(s interface{}, tableName string, tableAlias string) (columns Columns) {
	fmt.Println(`Warning: ColumnsForStructWithAlias is deprecated, and will be removed in a future version. Please use ForStructWithAlias instead.`)
	return ForStructWithAlias(s, tableName, tableAlias)
}

// ForStruct returns a Columns instance for
// the struct passed in.
func ForStruct(s interface{}, tableName string) (columns Columns) {
	return ForStructWithAlias(s, tableName, "")
}

// ForStructWithAlias returns a Columns instance for the struct passed in.
// If the tableAlias is not empty, it will be used.
func ForStructWithAlias(s interface{}, tableName string, tableAlias string) (columns Columns) {
	columns = NewColumnsWithAlias(tableName, tableAlias)
	defer func() {
		if r := recover(); r != nil {
			columns = NewColumnsWithAlias(tableName, tableAlias)
			columns.Add("*")
		}
	}()
	st := reflect.TypeOf(s)
	if st.Kind() == reflect.Ptr {
		st = st.Elem()
	}
	if st.Kind() == reflect.Slice {
		st = st.Elem()
		if st.Kind() == reflect.Ptr {
			st = st.Elem()
		}
	}

	fieldCount := st.NumField()

	for i := 0; i < fieldCount; i++ {
		field := st.Field(i)

		popTags := TagsFor(field)
		tag := popTags.Find("db")

		if !tag.Ignored() && !tag.Empty() {
			col := tag.Value

			// add writable or readable.
			tag := popTags.Find("rw")
			if !tag.Empty() {
				col = col + "," + tag.Value
			}

			cs := columns.Add(col)

			// add select clause.
			tag = popTags.Find("select")
			if !tag.Empty() {
				c := cs[0]
				c.SetSelectSQL(tag.Value)
			}
		}
	}

	return columns
}
