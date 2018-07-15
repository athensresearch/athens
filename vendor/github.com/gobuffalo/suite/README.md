# suite

Suite is a package meant to make testing [gobuffalo.io](http://gobuffalo.io) applications easier.

## Setup

This is the entry point into your unit testing suite. The `Test_ActionSuite(t *testing.T)` function is
compatible with the `go test` command, and it should:

- Create and configure your new test suite instance (`ActionSuite` in this case)
- Call `suite.Run` with the `*testing.T` passed by the Go testing system, and your new `ActionSuite` instance

```go
package actions_test

import (
	"testing"

	"github.com/gobuffalo/suite"
	"github.com/gobuffalo/toodo/actions"
)

type ActionSuite struct {
	*suite.Action
}

func Test_ActionSuite(t *testing.T) {
	as := &ActionSuite{suite.NewAction(actions.App())}
	suite.Run(t, as)
}
```

## Usage

This is where you write your actual test logic. The rules for test names are similar, but not the same, as with `go test`:

- Each test is a method on your `*ActionSuite`
- Test method names should start with `Test` (note the upper case `T`)
- Test methods should have no arguments

A few additional notes:

- To avoid race conditions on the testing database, always use the `ActionSuite` variable called `DB` to access the database (not your production app's database)
- You can access the raw `*testing.T` value if needed with `as.T()`
- `ActionSuite` has support for [`testify`](https://github.com/stretchr/testify)'s [`require` package](https://godoc.org/github.com/stretchr/testify/require) and [`assert` package](https://godoc.org/github.com/stretchr/testify/assert)
- ... So try to use one of those instead packages of using the raw methods on the `*testing.T`
- The default database that `suite` will connect to is called `testing` in your [database.yml](https://github.com/markbates/pop#connecting-to-databases)

```go
package actions_test

import (
	"fmt"

	"github.com/gobuffalo/toodo/models"
)

func (as *ActionSuite) Test_TodosResource_List() {
	todos := models.Todos{
		{Title: "buy milk"},
		{Title: "read a good book"},
	}
	for _, t := range todos {
		err := as.DB.Create(&t)
		as.NoError(err)
	}

	res := as.HTML("/todos").Get()
	body := res.Body.String()
	for _, t := range todos {
		as.Contains(body, fmt.Sprintf("<h2>%s</h2>", t.Title))
	}
}

func (as *ActionSuite) Test_TodosResource_New() {
	res := as.HTML("/todos/new").Get()
	as.Contains(res.Body.String(), "<h1>New Todo</h1>")
}

func (as *ActionSuite) Test_TodosResource_Create() {
	todo := &models.Todo{Title: "Learn Go"}
	res := as.HTML("/todos").Post(todo)
	as.Equal(301, res.Code)
	as.Equal("/todos", res.Location())

	err := as.DB.First(todo)
	as.NoError(err)
	as.NotZero(todo.ID)
	as.NotZero(todo.CreatedAt)
	as.Equal("Learn Go", todo.Title)
}

func (as *ActionSuite) Test_TodosResource_Create_Errors() {
	todo := &models.Todo{}
	res := as.HTML("/todos").Post(todo)
	as.Equal(422, res.Code)
	as.Contains(res.Body.String(), "Title can not be blank.")

	c, err := as.DB.Count(todo)
	as.NoError(err)
	as.Equal(0, c)
}

func (as *ActionSuite) Test_TodosResource_Update() {
	todo := &models.Todo{Title: "Lern Go"}
	verrs, err := as.DB.ValidateAndCreate(todo)
	as.NoError(err)
	as.False(verrs.HasAny())

	res := as.HTML("/todos/%s", todo.ID).Put(&models.Todo{ID: todo.ID, Title: "Learn Go"})
	as.Equal(200, res.Code)

	err = as.DB.Reload(todo)
	as.NoError(err)
	as.Equal("Learn Go", todo.Title)
}
```

## Fixtures (Test Data)

Often it is useful to load a series of data into the database at the start of the test to make testing easier. For example, you need to have a user in the database to log a person into the application, or you need some data in the database to test destroying that data. Fixtures let us solve these problems easily.

### Usage

First you need to setup your test suite to use fixtures. You can do this by using `suite.NewActionWithFixtures` or `suite.NewModelWithFixtures` methods to create new test suites that take a `packr.Box` pointing to where the files for this suite live.

```go
package actions

import (
	"testing"

	"github.com/gobuffalo/packr"
	"github.com/gobuffalo/suite"
)

type ActionSuite struct {
	*suite.Action
}

func Test_ActionSuite(t *testing.T) {
	action, err := suite.NewActionWithFixtures(App(), packr.NewBox("../fixtures"))
	if err != nil {
		t.Fatal(err)
	}

	as := &ActionSuite{
		Action: action,
	}
	suite.Run(t, as)
}
```

Once your suite is set up, you can create `N` numbers of `*.toml` files in the directory you've chosen for your fixtures, in this example, `../fixtures`.

### Example Fixture File

```toml
[[scenario]]
name = "lots of widgets"

  [[scenario.table]]
    name = "widgets"

    [[scenario.table.row]]
      id = "<%= uuidNamed("widget") %>"
      name = "This is widget #1"
      body = "some widget body"
      created_at = "<%= now() %>"
      updated_at = "<%= now() %>"

    [[scenario.table.row]]
      id = "<%= uuid() %>"
      name = "This is widget #2"
      body = "some widget body"
      created_at = "<%= now() %>"
      updated_at = "<%= now() %>"

  [[scenario.table]]
    name = "users"

    [[scenario.table.row]]
      id = "<%= uuid() %>"
      name = "Mark Bates"
      admin = true
      price = 19.99
      widget_id = "<%= uuidNamed("widget") %>"
      created_at = "<%= now() %>"
      updated_at = "<%= now() %>"
```

#### Helper Methods

The `*.toml` files all get run through [https://github.com/gobuffalo/plush](https://github.com/gobuffalo/plush) before they're decoded, so you can make use of the helpful helper methods that ship with Plush.

We've also add a couple of useful helpers for you as well:

* `uuid()` - returns a new `github.com/gobuffalo/uuid.UUID`
* `now()` - returns `time.Now()`
* `uuidNamed(name)` - will attempt to return a previously declared UUID with that name, useful, for relations/associations. If there was one that wasn't defined with that name, a new one will be created.

### Using in Tests

In your suite tests you need to call the `LoadFixture` method giving it the name of the fixtures you would like to use for this test.

```go
func (as *ActionSuite) Test_WidgetsResource_List() {
	as.LoadFixture("lots of widgets")
	res := as.HTML("/widgets").Get()

	body := res.Body.String()
	as.Contains(body, "widget #1")
	as.Contains(body, "widget #2")
}
```

### FAQs

* _Can I call `LoadFixture` more than once in a test?_ - Absolutely! Call it as many times as you want!
* _Can I load multiple rows into a table in one scenario?_ - Absolutely!
* _Can I load data into multiple tables in one scenario?_ - Absolutely!
* _Will it load all my fixtures?_ - No, you have to load specific scenarios, so don't be afraid to create lots of scenarios and only call the ones you need per test.
* _Will this polute my database, and how do I clear data between tests?_ - No need to worry, the suite will truncate any data in your database between test runs, so you never have to worry about it.
