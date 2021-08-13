# Athens Lan Party Mode

## Client

Use DB Picker -> join with `localhost:3010` as URL, no password.

## Server

### Building `uberjar`

To create uberjar:
``` shell
lein uberjar
```

This will create `target/athens-lan-party-standalone.jar`.

### Running `uberjar`

Once you've built `uberjar` you can run it as simply as:

``` shell
java -jar target/athens-lan-party-standalone.jar
```

In the output you can notice `Starting WebServer with config:  {:port 3010}`,  
this **3010** is the port number that Athens Lan-Party runs on,  
notice it might be different number if you've changed configuration.

### Running Athens Self-Hosted Server (using `lein`)

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
