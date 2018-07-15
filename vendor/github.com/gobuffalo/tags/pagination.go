package tags

import (
	"net/url"
	"reflect"
	"strconv"
	"strings"

	"github.com/fatih/structs"
	"github.com/pkg/errors"
)

// Paginator describes a pagination meta data
type Paginator struct {
	// Current page you're on
	Page int `json:"page"`
	// Number of results you want per page
	PerPage int `json:"per_page"`
	// Page * PerPage (ex: 2 * 20, Offset == 40)
	Offset int `json:"offset"`
	// Total potential records matching the query
	TotalEntriesSize int `json:"total_entries_size"`
	// Total records returns, will be <= PerPage
	CurrentEntriesSize int `json:"current_entries_size"`
	// Total pages
	TotalPages int `json:"total_pages"`
}

//TagFromPagination receives a pagination interface{} and attempts to get
//Paginator properties from it before generating the tag.
func (p Paginator) TagFromPagination(pagination interface{}, opts Options) (*Tag, error) {
	rv := reflect.ValueOf(pagination)
	if rv.Kind() == reflect.Ptr {
		rv = rv.Elem()
	}

	if rv.Kind() != reflect.Struct {
		return nil, errors.Errorf("can't build a Paginator from %T", pagination)
	}

	s := structs.New(rv.Interface())

	if f, ok := s.FieldOk("Page"); ok {
		p.Page = f.Value().(int)
	}

	if f, ok := s.FieldOk("PerPage"); ok {
		p.PerPage = f.Value().(int)
	}

	if f, ok := s.FieldOk("Offset"); ok {
		p.Offset = f.Value().(int)
	}

	if f, ok := s.FieldOk("TotalEntriesSize"); ok {
		p.TotalEntriesSize = f.Value().(int)
	}

	if f, ok := s.FieldOk("TotalEntriesSize"); ok {
		p.TotalEntriesSize = f.Value().(int)
	}

	if f, ok := s.FieldOk("CurrentEntriesSize"); ok {
		p.CurrentEntriesSize = f.Value().(int)
	}

	if f, ok := s.FieldOk("TotalPages"); ok {
		p.TotalPages = f.Value().(int)
	}

	return p.Tag(opts)
}

//Tag generates the pagination html Tag
func (p Paginator) Tag(opts Options) (*Tag, error) {
	// return an empty div if there is only 1 page
	if p.TotalPages <= 1 {
		return New("div", Options{}), nil
	}

	path, class, wing := extractBaseOptions(opts)
	opts["class"] = strings.Join([]string{class, "pagination"}, " ")

	t := New("ul", opts)

	barLength := wing*2 + 1
	center := wing + 1
	loopStart := 1
	loopEnd := p.TotalPages

	li, err := p.addPrev(opts, path)
	if err != nil {
		return t, errors.WithStack(err)
	}
	t.Append(li)

	if p.TotalPages > barLength {
		loopEnd = barLength - 2 // range 1 ~ center
		if p.Page > center {    /// range center
			loopStart = p.Page - wing + 2
			loopEnd = loopStart + barLength - 5
			li, err := pageLI("1", 1, path, p)
			if err != nil {
				return t, errors.WithStack(err)
			}
			t.Append(li)
			t.Append(pageLIDummy())
		}
		if p.Page > (p.TotalPages - wing - 1) {
			loopEnd = p.TotalPages
			loopStart = p.TotalPages - barLength + 3
		}
	}

	for i := loopStart; i <= loopEnd; i++ {
		li, err := pageLI(strconv.Itoa(i), i, path, p)
		if err != nil {
			return t, errors.WithStack(err)
		}
		t.Append(li)
	}

	if p.TotalPages > loopEnd {
		t.Append(pageLIDummy())
		label := strconv.Itoa(p.TotalPages)
		li, err := pageLI(label, p.TotalPages, path, p)
		if err != nil {
			return t, errors.WithStack(err)
		}
		t.Append(li)
	}

	li, err = p.addNext(opts, path)
	if err != nil {
		return t, errors.WithStack(err)
	}
	t.Append(li)

	return t, nil
}

//Pagination builds pagination Tag based on a passed pagintation interface
func Pagination(pagination interface{}, opts Options) (*Tag, error) {
	if p, ok := pagination.(Paginator); ok {
		return p.Tag(opts)
	}

	p := Paginator{
		Page:    1,
		PerPage: 20,
	}

	return p.TagFromPagination(pagination, opts)
}

func (p Paginator) addPrev(opts Options, path string) (*Tag, error) {
	showPrev := true

	if b, ok := opts["showPrev"].(bool); ok {
		showPrev = b
		delete(opts, "showPrev")
	}

	if !showPrev {
		return nil, nil
	}

	page := p.Page - 1
	prevContent := "&laquo;"

	if opts["previousContent"] != nil {
		prevContent = opts["previousContent"].(string)
	}

	return pageLI(prevContent, page, path, p)
}

func (p Paginator) addNext(opts Options, path string) (*Tag, error) {
	showNext := true

	if b, ok := opts["showNext"].(bool); ok {
		showNext = b
		delete(opts, "showNext")
	}

	if !showNext {
		return nil, nil
	}

	page := p.Page + 1
	nextContent := "&raquo;"

	if opts["nextContent"] != nil {
		nextContent = opts["nextContent"].(string)
	}

	return pageLI(nextContent, page, path, p)
}

func extractBaseOptions(opts Options) (string, string, int) {
	var path string
	if p, ok := opts["path"]; ok {
		path = p.(string)
		delete(opts, "path")
	}

	var class string
	if cl, ok := opts["class"]; ok {
		class = cl.(string)
		delete(opts, "path")
	}

	wing := 5
	if w, ok := opts["wingLength"]; ok {
		wing = w.(int)
		delete(opts, "wingLength")
	}

	return path, class, wing
}

func pageLI(text string, page int, path string, pagination Paginator) (*Tag, error) {

	classes := []string{"page-item"}

	if page == pagination.Page {
		classes = append(classes, "active")
	}

	li := New("li", Options{})
	defer func() {
		li.Options["class"] = strings.Join(classes, " ")
	}()

	if page == 0 || page > pagination.TotalPages {
		classes = append(classes, "disabled")
		li.Append(New("span", Options{
			"body":  text,
			"class": "page-link",
		}))
		return li, nil
	}

	url, err := urlFor(path, page)
	if err != nil {
		return li, errors.WithStack(err)
	}

	li.Append(New("a", Options{
		"href":  url,
		"class": "page-link",
		"body":  text,
	}))

	return li, nil
}

func urlFor(path string, page int) (string, error) {
	u, err := url.Parse(path)
	if err != nil {
		return "", err
	}

	q := u.Query()
	q.Set("page", strconv.Itoa(page))
	u.RawQuery = q.Encode()

	return u.String(), err
}

func pageLIDummy() *Tag {
	li := New("li", Options{"class": "page-item disabled"})
	a := New("a", Options{"body": "..."})
	li.Append(a)
	return li
}
