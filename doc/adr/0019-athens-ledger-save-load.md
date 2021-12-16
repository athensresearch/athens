# 19. Athens ledger save-load 

Date: 2021-12-01

## Status

Accepted.

## Context 

In case of data-loss we want a mechanism that can be used to restore the data.

## Decision

1. MVP - 0 : Use cron to save the ledger on disk, use the cli to load a previous ledger.

    - Have a cli using which we can 
      - Save: Take the current state of ledger and save it in the specified folder.
      - Load: Load events from some other ledger to a new server instance.
        - Delete the current ledger
        - Restart the fluree ledger
        - Load the create a new ledger and load the events from the other ledger.

    - Create a cron job that will save the ledger every X amount of time.
