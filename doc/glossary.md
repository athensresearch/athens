# Engineering Glossary

* DB / Database
  * rfdb - refers to re-frame's database. re-frame docs typically call re-frame db `[app-db](https://day8.github.io/re-frame/FAQs/Inspecting-app-db/)`. app-db should be used for non-persistent UI state. Examples: whether the left sidebar, right sidebar, or Athena are open or not.
  * dsdb - refers to datascript's database. datascript docs typically call datascript db `[conn](https://github.com/tonsky/datascript#usage-examples)`.
  * `:fs/` namespace - refers to all the operations for saving datascript to the filesystem. This is currently how local-only Athens is persisted. Athens reads and writes to filesystem via Electron, which exposes node.js libraries.
  * local-storage
    * `db-picker/all-dbs`        - all dbs known to athens
    * `db-picker/selected-db-id` - the id of the currently active db

