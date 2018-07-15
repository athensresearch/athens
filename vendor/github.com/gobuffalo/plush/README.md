# Plush  [![Build Status](https://travis-ci.org/gobuffalo/plush.svg?branch=master)](https://travis-ci.org/gobuffalo/plush) [![GoDoc](https://godoc.org/github.com/gobuffalo/plush?status.svg)](https://godoc.org/github.com/gobuffalo/plush)

Plush is the templating system that [Go](http://golang.org) both needs _and_ deserves. Powerful, flexible, and extendable, Plush is there to make writing your templates that much easier.

**[Introduction Video](https://blog.gobuffalo.io/introduction-to-plush-82a8a12cf98a#.y9t0g4xq2)**

## Installation

```text
$ go get -u github.com/gobuffalo/plush
```

## Usage

Plush allows for the embedding of dynamic code inside of your templates. Take the following example:

```erb
<!-- input -->
<p><%= "plush is great" %></p>

<!-- output -->
<p>plush is great</p>
```

### Controlling Output

By using the `<%= %>` tags we tell Plush to dynamically render the inner content, in this case the string `plush is great`, into the template between the `<p></p>` tags.

If we were to change the example to use `<% %>` tags instead the inner content will be evaluated and executed, but not injected into the template:

```erb
<!-- input -->
<p><% "plush is great" %></p>

<!-- output -->
<p></p>
```

By using the `<% %>` tags we can create variables (and functions!) inside of templates to use later:

```erb
<!-- does not print output -->
<%
let h = {name: "mark"}
let greet = fn(n) {
  return "hi " + n
}
%>
<!-- prints output -->
<h1><%= greet(h["name"]) %></h1>
```

#### Full Example:

```go
html := `<html>
<%= if (names && len(names) > 0) { %>
	<ul>
		<%= for (n) in names { %>
			<li><%= capitalize(n) %></li>
		<% } %>
	</ul>
<% } else { %>
	<h1>Sorry, no names. :(</h1>
<% } %>
</html>`

ctx := plush.NewContext()
ctx.Set("names", []string{"john", "paul", "george", "ringo"})

s, err := plush.Render(html, ctx)
if err != nil {
  log.Fatal(err)
}

fmt.Print(s)
// output: <html>
// <ul>
// 		<li>John</li>
// 		<li>Paul</li>
// 		<li>George</li>
// 		<li>Ringo</li>
// 		</ul>
// </html>
```
## Comments

You can add comments like this:

```erb
<%# This is a comment %>
```

## If/Else Statements

The basic syntax of `if/else if/else` statements is as follows:

```erb
<%
if (true) {
  # do something
} else if (false) {
  # do something
} else {
  # do something else
}
%>
```

When using `if/else` statements to control output, remember to use the `<%= %>` tag to output the result of the statement:

```erb
<%= if (true) { %>
  <!-- some html here -->
<% } else { %>
  <!-- some other html here -->
<% } %>
```

### Operators

Complex `if` statements can be built in Plush using "common" operators:

* `==` - checks equality of two expressions
* `!=` - checks that the two expressions are not equal
* `~=` - checks a string against a regular expression (`foo ~= "^fo"`)
* `<` - checks the left expression is less than the right expression
* `<=` - checks the left expression is less than or equal to the right expression
* `>` - checks the left expression is greater than the right expression
* `>=` - checks the left expression is greater than or equal to the right expression
* `&&` - requires both the left **and** right expression to be true
* `||` - requires either the left **or** right expression to be true

### Grouped Expressions

```erb
<%= if ((1 < 2) && (someFunc() == "hi")) { %>
  <!-- some html here -->
<% } else { %>
  <!-- some other html here -->
<% } %>
```

## Maps

Maps in Plush will get translated to the Go type `map[string]interface{}` when used. Creating, and using maps in Plush is not too different than in JSON:

```erb
<% let h = {key: "value", "a number": 1, bool: true} %>
```

Would become the following in Go:

```go
map[string]interface{}{
  "key": "value",
  "a number": 1,
  "bool": true,
}
```

Accessing maps is just like access a JSON object:

```erb
<%= h["key"] %>
```

Using maps as options to functions in Plush is incredibly powerful. See the sections on Functions and Helpers to see more examples.

## Arrays

Arrays in Plush will get translated to the Go type `[]interface{}` when used.

```erb
<% let a = [1, 2, "three", "four", h] %>
```

```go
[]interface{}{ 1, 2, "three", "four", h }
```

## For Loops

There are three different types that can be looped over: maps, arrays/slices, and iterators. The format for them all looks the same:

```erb
<%= for (key, value) in expression { %>
  <%= key %> <%= value %>
<% } %>
```

The values inside the `()` part of the statement are the names you wish to give to the key (or index) and the value of the expression. The `expression` can be an array, map, or iterator type.

### Arrays

#### Using Index and Value

```erb
<%= for (i, x) in someArray { %>
  <%= i %> <%= x %>
<% } %>
```

#### Using Just the Value

```erb
<%= for (val) in someArray { %>
  <%= val %>
<% } %>
```

### Maps

#### Using Index and Value

```erb
<%= for (k, v) in someMap { %>
  <%= k %> <%= v %>
<% } %>
```

#### Using Just the Value

```erb
<%= for (v) in someMap { %>
  <%= v %>
<% } %>
```

### Iterators

```go
type ranger struct {
	pos int
	end int
}

func (r *ranger) Next() interface{} {
	if r.pos < r.end {
		r.pos++
		return r.pos
	}
	return nil
}

func betweenHelper(a, b int) Iterator {
	return &ranger{pos: a, end: b - 1}
}
```

```go
html := `<%= for (v) in between(3,6) { return v } %>`

ctx := plush.NewContext()
ctx.Set("between", betweenHelper)

s, err := plush.Render(html, ctx)
if err != nil {
  log.Fatal(err)
}
fmt.Print(s)
// output: 45
```

## Helpers

### Builtin Helpers

* `json` - converts the interface to a JSON object
* `jsEscape` - escapes the interface to be JavaScript safe
* `htmlEscape` - escapes the interface to be HTML safe
* `upcase` - converts the string to upper case
* `downcase` - converts the string to lower case
* `contentFor` - stores a block of HTML to be used later
* `contentOf` - retrieves a block of HTML previously stored with `contentFor`
* `markdown` - converts the string from Markdown into HTML
* `len` - returns the length of the interface
* `debug` - returns the `%+v` of the interface wrapped in `<pre>` tags.
* `inspect` - returns the `%+v` of the interface
* `range` - interate between, and including two numbers
* `between` - iterate between, but not including, two numbers
* `until` - iterate until a number is reached
* `groupBy` - splits a slice or array into `n` groups
* `env` - returns the ENV variable for the specified key
* `truncate` - truncates a string to a specified length
* `form` - support for the [github.com/gobuffalo/tags/form](https://github.com/gobuffalo/tags/tree/master/form) package (Bootstrap version)
* `form_for` - support for the [github.com/gobuffalo/tags/form](https://github.com/gobuffalo/tags/tree/master/form) package (Bootstrap version) to build a form for a model

#### contentFor and contentOf

Use the `contentFor` and `contentOf` helpers to dry up your templates with reusable components.

For example, we can define a snippet that generates a fancy title using `contentFor`:

```
<% contentFor("fancy-title") { %>
  <h1 class='fancy'><%= title %></h1>
<% } %>
```

The `fancy-title` name is how we will invoke this with `contentOf` elsewhere 
in our template:

```
<%= contentOf("fancy-title", {"title":"Welcome to Plush"}) %>
```

* The second map argument is optional, for static content just use `<%= contentOf("fancy-title") %>`

Rendering this would generate this output:

```
<h1 class='fancy'>Welcome to Plush</h1>
```

As you can see, the `<%= title %>` has been replaced with the `Welcome to Plush` string.

#### truncate

`truncate` takes two optional parameters:
* `size` - the maximum length of the returned string
* `trail` - the string to append at the end of a truncated string, defaults to `...`

```html
<p><%= truncate("a long string", {"size": 10, "trail": "[more]"})%></p>
```

### From github.com/markbates/inflect

* `asciffy`
* `camelize`
* `camelize_down_first`
* `capitalize`
* `dasherize`
* `humanize`
* `ordinalize`
* `parameterize`
* `pluralize`
* `pluralize_with_size`
* `singularize`
* `tableize`
* `typeify`
* `underscore`

### Custom Helpers

```go
html := `<p><%= one() %></p>
<p><%= greet("mark")%></p>
<%= can("update") { %>
<p>i can update</p>
<% } %>
<%= can("destroy") { %>
<p>i can destroy</p>
<% } %>
`

ctx := NewContext()

// one() #=> 1
ctx.Set("one", func() int {
  return 1
})

// greet("mark") #=> "Hi mark"
ctx.Set("greet", func(s string) string {
  return fmt.Sprintf("Hi %s", s)
})

// can("update") #=> returns the block associated with it
// can("adsf") #=> ""
ctx.Set("can", func(s string, help HelperContext) (template.HTML, error) {
  if s == "update" {
    h, err := help.Block()
    return template.HTML(h), err
  }
  return "", nil
})

s, err := Render(html, ctx)
if err != nil {
  log.Fatal(err)
}
fmt.Print(s)
// output: <p>1</p>
// <p>Hi mark</p>
// <p>i can update</p>
```

### Special Thanks

This package absolutely 100% could not have been written without the help of Thorsten Ball's incredible book, [Writing an Interpeter in Go](https://interpreterbook.com).

Not only did the book make understanding the process of writing lexers, parsers, and asts, but it also provided the basis for the syntax of Plush itself.

If you have yet to read Thorsten's book, I can't recommend it enough. Please go and buy it!

