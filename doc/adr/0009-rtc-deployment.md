# 5. RTC deployment

Date: 2021-08-24


## Status

Proposed.


## Context

Now that we have a RTC server build we need to define how it should be deployed for usage.

The primary requirements are a method of installation, update, and guarantee of data resilience.


On installation:
- we should allow configuring hostname
    - port should already be configurable
    - might be preferrable to ship it with nginx
        - don't need hostname config then
- ideally all bundled in a docker image
    - whatever the services we have in that docker compose are, we always need to spit out backups/etc to a volume, and then the data is only as safe as that volume is
    - even if it's not a volume, backups are an output that goes somewhere
- we can use the server for our PKM usecases as well on electron
    - so whatever we come up with here for storage is applicable there as well
- there's several levels of persistence here
    - what the athens client saves to the athens server
    - how the athens server db commits changes
    - how that db commits to it's own persistence layer
    - how that persistence layer is made durable
- ideally we should be able to provide a 1-click deploy on aws/do/gcp
    - the docker strategy supports this
    - 


On update:
- the server update needs to run a boot sequence where migrations can be ran
- very likely that we need a shutdown sequence
    - because of alex has seen datahike corruption on filesystem twice, filipe has seen it once
- we should be able to use `component` to run both boot and shutdown seqs, via signal handling
- can we ensure the output of the server is always consistent?
    - if so, even if boot or shutdown seqs are interrupted, a new runtime can be provided
    - this is the holy grail of recovery
    - even if the server is booted/shutdown correctly, there can still be messages in flight to other services that we can't stop


On data resilience:
- for simple consistency we can store resources in the same place as db
    - if the db storage supports consistent backups we're good
    - what about fluree if we want to use it?
- every storage mechanism has limits
- whatever limits we have on storage will affect what we allow to upload
    - we have no data on what these limits should be right now
    - we should add limits according to our usecases and UX of the app
- storage for identity, permissions, etc also needs to be kept
- alex has seen datahike corruption on filesystem twice, filipe has seen it once
    - `No implementation of method: :-affects-key of protocol: #'hitchhiker.tree.op/IOperation found for class: incognito.base.IncognitoTaggedLiteral`
    - only solution was to delete the fs data
    - we need to repro to inform our decisions about data loss and server update
    - we should ask the datahike folks about this
- the frequency of output of the athens setup determines how safe your data is
    - e.g. snapshots of fs are infrequent
    - e.g. postgres writes are frequent
    - e.g. fluree writes are frequent
- we shouldn't java ports directly if we care abut data loss
    - otherwise we are exposed to a lot of java related security issues


## Decision

We will create create a docker compose definition for the athens server and a nginx proxy.

The docker compose definition will contain volume mapping for the outputs of the athens deployment.


## Consequences

We're defining docker compose as the input mechanism for an Athens server, and volumes as the output.
Data resilience is only as high as a user can make the volume be.

If we discover data corruption problems stemming for disk usage of Datahike we might need to take an approach that's different than volume as output.
