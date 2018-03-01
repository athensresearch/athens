package columns

import (
	"reflect"
)

// ColumnsForStruct returns a Columns instance for
// the struct passed in.

func ColumnsForStruct(s interface{}, tableName string) (columns Columns) {
	return ColumnsForStructWithAlias(s, tableName, "")
}

func ColumnsForStructWithAlias(s interface{}, tableName string, tableAlias string) (columns Columns) {
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

			//add writable or readable.
			tag := popTags.Find("rw")
			if !tag.Empty() {
				col = col + "," + tag.Value
			}

			cs := columns.Add(col)

			//add select clause.
			tag = popTags.Find("select")
			if !tag.Empty() {
				c := cs[0]
				c.SetSelectSQL(tag.Value)
			}
		}
	}

	return columns
}
