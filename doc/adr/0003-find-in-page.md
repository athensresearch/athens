# Find in page

Date: 2021-05-31

## Status

Proposed

## Context

Feat: Implementing a find in page functionality similar to chrome

Functionality:

- ctrl/cmd + f to start search, escape/click 'x' to stop
- Navigate between highlights, enter/shift+enter or next/previous

## Decision

There are 3 possible approaches to it
1. Using an external lib like
    - https://github.com/glafche/electron-find-in-page
    - https://github.com/rhysd/electron-in-page-search
2. Using highlight.js and dom modal
3. Separate window and using webContents API(cljs)

### Prerequisites

1. WebContents.findInPage is used to find matches in page. It is used by chrome and all highlights are made by chrome.
2. WebContents.findInPage does not have an "exclude this element" option i.e it highlights all matches in page
3. Highlight.js manipulates dom to change an element matched to include css which shows highlight background color.
4. Electron provides an option to create windows which are completely separated from main window(whole new dom) and can be communicated using ipc.

### Using an external lib

These libs are very shallow modules(wrappers) and should be avoided because such APIs result in increasing complexity(benefit they hide negated by cost of learning it)
Nonetheless here's an analysis of their approach.

#### https://github.com/glafche/electron-find-in-page

This lib doesn't use a separate window. 
Look at 2 from Prerequisites, as a consequence the input element that is taking input to find in page will also be highlighted.
This lib uses a hack https://github.com/glafche/electron-find-in-page/blob/master/src/findInPage.js#L418 to temporarily make the input's visibility hidden and un-hide it after timeout.
It should be causing some flicker and focus issues. Mainly if I type a common letter in large document(lot of matches) the timeout could lead to weird state where input will be highlighted or input field will vanish for a notable period.  
Avoids tiling manager issue.

#### https://github.com/rhysd/electron-in-page-search

This uses a separate window.
For styles, you'd be relying on files and if custom html is needed(a vertical bar like chrome between a/b and left/right/cross icons) you ought to create your own html.
Not tested in newer versions of OS(readme)
If for some reason you need to add one extra button, and want to hear for a callback when it's clicked in main window, it's not possible with this lib.
All this derives that it is a constrained API and extension beyond bare minimum is not possible or involves workarounds.

### Using highlight.js and dom modal

This approach uses a dom modal and highlight.js/an equivalent lib to start highlighting.
Using this we will remove the need for second div as we have option to highlight only a section or exclude sections of app
Avoiding this due to:
1. highlight js re renders dom elements and matches are many, it takes a notable amount of time
2. Could have unintended consequences on our currently rendered elements.  
3. Alternative to 1 is to highlight only one match when searched
    - Then it's not find in page like chrome
    - Will avoid new-window and new target in cljs
4. Avoids tiling manager issue
    
### Separate window and using webContents API

1. Create a new window from cljs code without relying on any of the above mentioned libs.
2. Manage communication between the two windows using ipc.
3. Uses a new compile target like `find-in-page` and html
    - Avoids above deps and all cljs code(Easy to debug) 
    - Easy to extend 
    - Con: Anyone working with this code will need to understand ipc data flow
4. Con: Tiling issue.
5. WebContents is faster and ensured to work without affecting dom.


## Consequences

Choosing "Separate window and using webContents API"

1. We'd have readable cljs code all across
2. Avoids external deps. 
    - Full functionality available
    - Easy to extend
    - Easy to debug
3. Desirably faster while being closest to "find in page like chrome"      
4. Con: Anyone working with this code will need to understand ipc data flow
