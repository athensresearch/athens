# x. Local Storage

Date: 2021-07-01

## Status

Pending

## Context

Problems with local-storage state:
- We previously used a re-frame event to get local-storage values, `:local-storage/get-db-filepath` during boot. Events should not be used this way.
- We use redundant values, e.g. `db/filepath` and `db-picker/all-items`. `db/filepath` can be derived from `db-picker-all-items`. Having multiple key-values increases the amount of state replication.
- Up until this point, we used plain strings for keys. This made mapping local-storage keys to re-frame keys inconvenient, e.g. `"db/filepath"`  in local-storage, `:db/filepath` in re-frame. Managing both keys manually is tedious and error-prone.
- Up until this point, we used plain strings for values. `:db-picker/all-items` is the first value we want to persist in local-storage that is a nested data structure, therefore we need to serialize and deserialize more consistently.
- We should be using the same local-storage API for getting and setting, but we have specific local-storage events and effects, e.g. `:local-storage/`
- Sometimes we get and set data to localStorage API, bypassing re-frame entirely. This happens mainly in `settings`, where we read and write to localStorage for username, email, monitoring, and backup time.

### Examples

* settings
    * backup timer
    * usage/diagnostics
    * user
      * OpenCollective email
      * username (used by RTC)
* Recently opened databases
    * Entire List
    * Most recently opened (can now be derived from Entire List)
* appearance
  * dark/light mode
  * screen width (not merged yet)
    
### Existing Solutions

- [blulegenes](shttps://sourcegraph.com/github.com/intermine/bluegenes@dev/-/blob/src/cljs/bluegenes/effects.cljs?L15-29&subtree=true)
  - Our implementation could be as simple as a pair of cofx/fx. No library actually needed.
  - Doesn't let you persist or get multiple keys at once.
- https://github.com/akiroz/re-frame-storage
  - Automatically persists the values you want. Don't have to create duplicate `:db` and `:fx`
  - Persists multiple keys easily.
  - There is a single `:persistent` key
- https://github.com/deg/re-frame-storage-fx 
  - Handles both local-storage and session-storage (not used yet).
  - `get`s multiple keys easily.



## Decision

After first chat with Sid and Alex, the current approach is to create a single nested map for all values that need to be persisted to localStorage.

Pros:

- We have one data structure to work with. This means we can stick to `.edn` as much as we want to until the final serialized state (probably transit-json). One monolithic data structure makes portability easier in the future, for instance, if we stored all these settings in `settings.json` (like VS Code), `config.edn`, or in a SQL/NoSQL table.
- We can easily read in multiple values via cofx at once. The bluegenes approach only gives the coeffects one `:local-storage` key, which means multiple `get`s would overwrite this key.

Cons:

- It is harder to manipulate local-storage values directly from the Dev Console. Probably mainly only Athens devs would have to work around this nested, serialized data structure, but overall probably not a big deal.

### Implementation

- Use init-rfdb with `nil` values or empty values if a collection. Try to make sure all key value pairs are present. [Bluegenes example](https://sourcegraph.com/github.com/intermine/bluegenes@dev/-/blob/src/cljs/bluegenes/db.cljc?subtree=true)
- Read in local-storage on `boot`. Go through db logic, based on whether a db exists or not in settings or on filesystem.
- Apply additional settings, such as appearance (screen width, light/dark mode)
- Write interceptor with :after key that looks at app-db. If :athens/persist key in app-db is updated, update local-storage. See https://day8.github.io/re-frame/Interceptors/
- Make sure user can `get` and `set` multiple values at once.


## Consequences

How do we upgrade/migrate from `db/filepath` to `db-picker/all-dbs` ?
