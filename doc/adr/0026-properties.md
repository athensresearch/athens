# Properties

Properties are a new type of parent-child relationship for blocks.

Currently, blocks can have children:

```md
- parent block
  - first child
  - second child
```

Block children behave as an ordered list of blocks.
Order numbers are not visible in the outline, and are implicit in the listing of children.

You can think of `first child` as having order number 1, and `second child` as having order number 2.
You cannot have two children share the same order number - if one appears after the other, then the latter has a higher order number.

Properties are similar to children in that they are blocks with a parent, but instead of an order number they possess a key (also known as a name).

```md
- parent block
  : a key - a prop
  : another key - another prop
    - child of a another prop
  - first child
  - second child
```

The block `a prop` is a property of `parent block` under the key `a key`.
Property keys are page titles, and a block cannot have duplicate keys.

Property blocks are shown in the outline as prefixed with `:` followed by the key.
Properties differ from children in that they are ordered alphabetically by key name, cannot be reordered, and appear before children.

Property blocks are otherwise the same as any other block.
You can edit and reference them, add children and properties under them, move them to other places.

You can convert any given block into a property by typing `::` and searching or creating a new property key.
Pressing backspace at the start of a property block will remove the key, turning it into a child block.

Clicking on the key will take you to the page for that key.
Pages that are used as properties have a `Linked properties` section at the end.


## Uses

### Labelling

You can label well-known pieces of information on your graph using properties. 

```md
[[Deep Work]]
  : Author - [[Cal Newport]]
  : Highlights - 
    - Clarity about what matters provides clarity about what does not.
    - As Nietzsche said: “It is only ideas gained from walking that have any worth.
  - Stopped at page 120 on 2022-05-20
  - Stopped at page 189 on 2022-05-24
```

Using properties instead of children for labelling has the following advantages:
- they show up at the top
- they show up always in the same order
- you can look up all blocks with the property on the property page

### Named relationships

Using refs you can establish a relationship between a block and a page or a block:

```md
- Deep Work [[Cal Newport]]
```

Let's call this block `Deep Work`.
You can say that `Deep Work` is associated with `Cal Newport`, but you cannot say much more than that.
You can think of this as a `['Deep Work' 'Cal Newport']` tuple.

Using properties you can name the relationship:

```md
- Deep Work
  : Author - [[Cal Newport]]
```

This example matches the `['Deep Work' 'Author' 'Cal Newport']` triple.

Named associations are a powerful and expressive way to think about data.
Triples are the atomic data of subject-predicate-object databases.

### On-graph data storage

Athens' data model is rich enough to store collection-based application data:
- parent-children relationships model trees
- children model vectors
- properties model maps

This Athens document can be mapped to the following JSON data:

```md
- 
  : value - a string
  : vector - 
    - 1
    - 2
    - 3
  : map -
    : key - value
    : another key - another value
  : nested data -
    : level 1 - 
      : level 2 - 
        :level 3 -
```
```json
{
   "value": "a string",
   "vector": [
      "1",
      "2",
      "3"
   ],
   "map": {
      "key": "value",
      "another key": "another value"
   },
   "nested data": {
      "level 1": {
         "level 2": {
            "level 3": ""
         }
      }
   }
}
```

The only primitive data type currently available is `string`, but since other data types can be serialised to string it is expressive enough.

We plan to add support to more primitive data types like `number`, `ref`, `datetime`, `user`.

Our own first-party features persist data on-graph data.
It has enabled us to prototype much faster.