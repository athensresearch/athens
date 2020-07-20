# Athens Components Documentation

Components are special syntaxes in blocks that allow complex interactions and data display from an end-user perspective. Currently we only support a couple of components (see below), but new components would be gradually added.

This documentation provides a technical overview regarding how Athens components are processed in the frontend. If you have any ideas or suggestions, feel free to open an issue!

## How Components are Parsed

The Athens [parser](./parser.md) considers everything in double curly brackets (`{{}}`) a component. You can have all kinds of syntaxes in a component, as the everything in the component is matched using regular expressions first and could be further parsed using instaparse if it has a more complex syntax.

After the parsing process, the `:component` list would be a variadic list in which the first element is the unparsed string for more efficient pattern matching while rest of the elements are parsed tree for things like dynamic references & auto page creation. Will add documentation.

Relevant code:

* <../src/cljc/athens/parser.cljc>
* <../src/cljs/athens/components>

## Currently Supported Components

* Todo Component
* Embed Component (YouTube/Arbitrary Embed)

## Upcoming Components

* Kanban
* Table
* Query
* Charts
* Block Embed
* ... and more!

If you would like to contribute a component, feel free to talk to us in Discord & submit a PR!