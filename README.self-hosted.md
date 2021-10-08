# Athens Lan Party Mode

## Client

Use DB Picker -> join with `localhost:3010` as URL, no password.

## Server

### Building `uberjar`

To create uberjar:
``` shell
yarn server
```

This will create `target/athens-lan-party-standalone.jar`.

### Custom Config

The default configuration can be found in `config.edn`.
You can customize it through the `config_edn` environment variable.

Here's an example on how to overwrite the default Datahike store using `config_edn`,
and configure Server password.
```
config_edn: "{:password "YourServerPassword" :datahike {:store {:path \"/srv/athens/db\"}}}"
```
The `config_edn` will be merged to other configs via deep merging.

### Running `uberjar`

Once you've built `uberjar` you can run it as simply as:

``` shell
java -jar target/athens-lan-party-standalone.jar
```

In the output you can notice `Starting WebServer with config:  {:port 3010}`,  
this **3010** is the port number that Athens Lan-Party runs on,  
notice it might be different number if you've changed configuration.

### Running Athens Self-Hosted Server

``` shell
yarn server:run
```

This will start HTTP server on port 3010, unless you've modified `src/clj/config.edn`.

Also nREPL server is started on port 8877, unless you've modified `src/clj/config.edn`.

### Developing Athens Self-Hosted Server

Start REPL:

``` shell
yarn server:repl
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

Stop the Self-Hosted server. [ctrl+c] if using `yarn server:run` or [ctrl+d] if repl.
By default Datahike DB is stored in `/tmp/exmaple`, remove this forlder
start the srever and Bob's your unkle.


## Docker

You can create an Athens server without installing anything else via docker compose.

Pick a [release](https://github.com/athensresearch/athens/releases you'd like to use, download the `docker-compose.yml` file in the release to a folder, and then run `docker compose up --no-build`.

For example, for `v1.0.0-alpha.rtc.12`:

```sh
curl https://github.com/athensresearch/athens/releases/download/v1.0.0-alpha.rtc.12/docker-compose.yml --output docker-compose.yml
docker compose up
```

The server will be acessible at `localhost:80`, and all data will be saved at `./athens-data`.

You can override the app configuration via an environment variable:

```sh
CONFIG_EDN="{:password \"YourServerPassword\" :datahike {:store {:path \"/srv/athens/db\"}}}" docker compose up
```

or via an `.env` file located in the same directory as the downloaded `docker-compose.yml`:

```sh
# .env
CONFIG_EDN="{:password \"YourServerPassword\" :datahike {:store {:path \"/srv/athens/db\"}}}"
```

