# vgoprox

A proxy server for [vgo](https://github.com/golang/go/wiki/vgo).

This project is a proof-of-concept server that implements the download protocol
detailed [here](https://research.swtch.com/vgo-module) (under "Download Protocol").

The server serves Go modules -- 
defined by the ["Go and Versioning"](https://research.swtch.com/vgo) papers -- from a few sources:

- Memory
- An on-disk directory (not yet implemented)
- Cloud blog stores (not implemented yet)

There are a few more features planned:

- An admin API that accepts new modules and stores them
- A CLI that packages up modules and sends them to the admin API
