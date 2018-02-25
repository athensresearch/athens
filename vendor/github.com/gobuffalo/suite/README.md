# suite

Suite is a package meant to make testing [gobuffalo.io](http://gobuffalo.io) applications easier.

## Setup

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
