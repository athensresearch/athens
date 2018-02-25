formam
======

A package for decode form's values into struct in Go. 
The only requirement is [Go 1.2](http://golang.org/doc/go1.2) or later.

[![Build Status](https://travis-ci.org/monoculum/formam.png?branch=master)](https://travis-ci.org/monoculum/formam)
[![GoDoc](https://godoc.org/github.com/monoculum/formam?status.png)](https://godoc.org/github.com/monoculum/formam)

Features
--------

* Nesting ad infinitum in `maps`, `structs` and `slices`.
* `UnmarshalText` in values and keys of maps.
* the `map`'s key supported are: `string`, `int` and variants, `uint` and variants, `uintptr`, `float32`, `float64`, `bool`, `struct`, `custom types` to one of the above types registered by function or `UnmarshalText` method, a `pointer` to one of the above types
* A field with `interface{}` that has a `map`, `struct` or `slice` as value is perfectly possible access to them! (see example below)
* decode `time.Time` with format "2006-01-02" by its UnmarshalText method.
* decode `url.URL`
* The `slice` and `array` is possible to access without to indicate a index (If it is the last field, of course)`
* You can to register a `func` for a `custom type` for all fields that include it or one in particular! (see example below)

Performance
-----------

You can see the performance in [formam-benchmark](https://github.com/monoculum/formam-benchmark) compared with [ajg/form](https://github.com/ajg/form), [gorilla/schema](https://github.com/gorilla/schema), [go-playground/form](https://github.com/go-playground/form) and [built-in/json](http://golang.org/pkg/encoding/json/).

Types
-----

The supported field types in the destination struct are:

* `string`
* `bool`
* `int`, `int8`, `int16`, `int32`, `int64`
* `uint`, `uint8`, `uint16`, `uint32`, `uint64`
* `float32`, `float64`
* `slice`, `array`
* `struct` and `struct anonymous`
* `map`
* `interface{}`
* `time.Time`
* `url.URL`
* `custom types` to one of the above types
* a `pointer` to one of the above types

**NOTE**: the nesting in `maps`, `structs` and `slices` can be ad infinitum.

Custom Marshaling
-----------------

Is possible unmarshaling data and the key of a map by the `encoding.TextUnmarshaler` interface.

Custom Type
-----------

Is possible to register a function for a custom type for all fields that include it or one in particular. Types registered has preference over UnmarshalText method.
For example:

##### All fields

```go
decoder.RegisterCustomType(func(vals []string) (interface{}, error) {
        return time.Parse("2006-01-02", vals[0])
}, []interface{}{time.Time{}}, nil)
```

##### Specific fields

```go
package main

type Times struct {
    Timestamp   time.Time
    Time        time.Time
    TimeDefault time.Time
}

func main() {
    var t Timestamp
    
    dec := NewDecoder(nil)
    // for Timestamp field
    dec.RegisterCustomType(func(vals []string) (interface{}, error) {
            return time.Parse("2006-01-02T15:04:05Z07:00", vals[0])
    }, []interface{}{time.Time{}}, []interface{}{&t.Timestamp{}}) 
    // for Time field
    dec.RegisterCustomType(func(vals []string) (interface{}, error) {
                return time.Parse("Mon, 02 Jan 2006 15:04:05 MST", vals[0])
    }, []interface{}{time.Time{}}, []interface{}{&t.Time{}}) 
    // for field that not be Time or Timestamp, i.e, in this example, TimeDefault.
    dec.RegisterCustomType(func(vals []string) (interface{}, error) {
                return time.Parse("2006-01-02", vals[0])
    }, []interface{}{time.Time{}}, nil)
    
    dec.Decode(url.Values{}, &t)
}
```

Notes
-----

The version 2 is compatible with old syntax for to access to maps, i.e., by point.
If you is using this package for first time in your project, please, use the brackets for to access to maps.


Usage
-----

### In form html

- Use symbol `.` for access a field of a struct. (i.e, `struct.field1`)
- Use `[<index>]` for access to index of a slice/array. (i.e, `struct.array[0]`). If the array/slice is the last field of the path, it is not necessary to indicate the index
- Use `[<key>]` for access to key of a map. (i.e, `struct.map[es-ES]`).

```html
<form method="POST">
  <input type="text" name="Name" value="Sony"/>
  <input type="text" name="Location.Country" value="Japan"/>
  <input type="text" name="Location.City" value="Tokyo"/>
  <input type="text" name="Products[0].Name" value="Playstation 4"/>
  <input type="text" name="Products[0].Type" value="Video games"/>
  <input type="text" name="Products[1].Name" value="TV Bravia 32"/>
  <input type="text" name="Products[1].Type" value="TVs"/>
  <input type="text" name="Founders[0]" value="Masaru Ibuka"/>
  <input type="text" name="Founders[0]" value="Akio Morita"/>
  <input type="text" name="Employees" value="90000"/>
  <input type="text" name="public" value="true"/>
  <input type="url" name="website" value="http://www.sony.net"/>
  <input type="date" name="foundation" value="1946-05-07"/>
  <input type="text" name="Interface.ID" value="12"/>
  <input type="text" name="Interface.Name" value="Go Programming Language"/>
  <input type="submit"/>
</form>
```

### In golang

You can use the tag `formam` if the name of a input of form starts lowercase.

```go
type InterfaceStruct struct {
    ID   int
    Name string
}

type Company struct {
  Public     bool      `formam:"public"`
  Website    url.URL   `formam:"website"`
  Foundation time.Time `formam:"foundation"`
  Name       string
  Location   struct {
    Country  string
    City     string
  }
  Products   []struct {
    Name string
    Type string
  }
  Founders   []string
  Employees  int64
  
  Interface interface{}
}

func MyHandler(w http.ResponseWriter, r *http.Request) error {
  m := Company{
      Interface: &InterfaceStruct{}, // its is possible to access to the fields although it's an interface field!
  }
  r.ParseForm()
  dec := formam.NewDecoder(&formam.DecoderOptions{TagName: "formam"})
  if err := dec.Decode(r.Form, &m); err != nil {
  		return err
  }
  return nil
}
```
