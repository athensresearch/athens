# x. Record architecture decisions

Date: 2021-04-04

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

### Local Storage State

* [State](https://github.com/athensresearch/athens/issues/997)


* settings
    * backup timer
    * usage/diagnostics
    * user
      * OpenCollective email
      * username (used by RTC)
* Recently opened databases
    * Most recently opened (can now be derived from Entire List)
    * Entire List
* appearance
  * dark/light mode
  * screen width (not merged yet)


## Decision

- Would it be better to have one big `athens/settings` map or to have separate key/val pairs in local-storage?
- How do we upgrade/migrate from `db/filepath` to `db-picker/all-dbs` ?
- What if a user needs to pull multiple values from local-storage? The current cofx adds a `:local-storage` key, but this would be overwritten if multiple values were `get`


## Consequences


