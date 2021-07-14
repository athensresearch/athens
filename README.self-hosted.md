# Athens Lan Party Mode

## Client

Standard procedure for running dev client.

Open DevConsole.

**Join LanParty**

``` javascript
athens.core.lan_on();
```

**Leave LanParty**

``` javascript
athens.core.lan_off();
```

## Server

### Running Athens Self-Hosted Server

``` shell
lein run
```

This will start HTTP server on port 3010, unless you've modified `src/clj/config.edn`.

Also nREPL server is started on port 8877, unless you've modified `src/clj/config.edn`.

### Developing Athens Self-Hosted Server

Start REPL:

``` shell
lein repl
```

Start the system:

``` clojure
(dev)
(start)
```

Same way you can start the system after `cider-jack-in`.

After starting HTTP & nREPL servers are running on default ports or changes in `config.edn`.

**Resetting the system**

``` clojure
(reset)
```

**Clean the Datahike DB**

Stop the Self-Hosted server. [ctrl+c] if using `lein run` or [ctrl+d] if repl.
By default Datahike DB is stored in `/tmp/exmaple`, remove this forlder
start the srever and Bob's your unkle.
