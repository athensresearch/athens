# Engineering Glossary

* DB / Database
  * rfdb - refers to re-frame's database. re-frame docs typically call re-frame db `[app-db](https://day8.github.io/re-frame/FAQs/Inspecting-app-db/)`. app-db should be used for non-persistent UI state. Examples: whether the left sidebar, right sidebar, or Athena are open or not.
  * dsdb - refers to datascript's database. datascript docs typically call datascript db `[conn](https://github.com/tonsky/datascript#usage-examples)`.
  * `:fs/` namespace - refers to all the operations for saving datascript to the filesystem. This is currently how local-only Athens is persisted. Athens reads and writes to filesystem via Electron, which exposes node.js libraries.
  * local-storage
    * `db/remote`
    * `db/remote-graph-conf` - supported in first attempt at backend. No longer needed to support
    * `db/filepath`
    * `db-picker/all-dbs`

* [State](https://github.com/athensresearch/athens/issues/997)
  * Use datascript for blocks, pages, and graphs.
  * Use re-frame for UI state.
  * Use localStorage for persistent application state. Also write re-frame bindings so views can more easily subscribe and dipatch updates to these values.
    * appearance
      * dark/light mode
      * screen width
    * settings
      * backup timer
      * usage/diagnostics
    * user
      * OpenCollective email
      * username
    * Recently opened databases
      * Most recently opened
      * Entire List
