package module

import (
	"testing"

	"github.com/stretchr/testify/suite"
)

type FilterTests struct {
	suite.Suite
}

func Test_Filter(t *testing.T) {
	suite.Run(t, new(FilterTests))
}

func (t *FilterTests) Test_IgnoreSimple() {
	r := t.Require()

	f := NewFilter()
	f.AddRule("github.com/a/b", Exclude)

	r.Equal(true, f.ShouldProcess("github.com/a"))
	r.Equal(false, f.ShouldProcess("github.com/a/b"))
	r.Equal(false, f.ShouldProcess("github.com/a/b/c"))
	r.Equal(true, f.ShouldProcess("github.com/d"))
	r.Equal(true, f.ShouldProcess("bitbucket.com/a/b"))
}

func (t *FilterTests) Test_IgnoreParentAllowChildren() {
	r := t.Require()

	f := NewFilter()
	f.AddRule("github.com/a/b", Exclude)
	f.AddRule("github.com/a/b/c", Include)

	r.Equal(true, f.ShouldProcess("github.com/a"))
	r.Equal(false, f.ShouldProcess("github.com/a/b"))
	r.Equal(true, f.ShouldProcess("github.com/a/b/c"))
	r.Equal(true, f.ShouldProcess("github.com/d"))
	r.Equal(true, f.ShouldProcess("bitbucket.com/a/b"))
}

func (t *FilterTests) Test_OnlyAllowed() {
	r := t.Require()

	f := NewFilter()
	f.AddRule("github.com/a/b", Include)
	f.AddRule("", Exclude)

	r.Equal(false, f.ShouldProcess("github.com/a"))
	r.Equal(true, f.ShouldProcess("github.com/a/b"))
	r.Equal(true, f.ShouldProcess("github.com/a/b/c"))
	r.Equal(false, f.ShouldProcess("github.com/d"))
	r.Equal(false, f.ShouldProcess("bitbucket.com/a/b"))
}
