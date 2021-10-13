# Athens Lan Party Mode

## Client

Use DB Picker -> join with `localhost:3010` as URL, no password.

## Server

### Custom Config

The default configuration can be found in `config.edn`.
You can customize it through the `config_edn` environment variable.

Here's an example on how to configure the server password using `config_edn`.
```
config_edn: "{:password "YourServerPassword"}"
```
The `config_edn` will be merged to other configs via deep merging.


### Running Athens Self-Hosted Server

Run the following commands in two different terminals:

``` shell
docker compose up fluree
yarn server
```

The first command starts the Fluree database using docker to persist data.
If you set `:in-memory? false` in `dev/clj/config.edn` no data is persisted and this command is not needed.

The second command will start HTTP server on port 3010, unless you've modified `dev/clj/config.edn`.

Also nREPL server is started on port 8877, unless you've modified `dev/clj/config.edn`.


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

**Clean the Fluree DB**

Stop the Self-Hosted server. [ctrl+c] if using `yarn server` or [ctrl+d] if repl.
By default Fluree DB is stored in `./athens-data/fluree`, remove this folder,
start the server and Bob's your unkle.


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
CONFIG_EDN="{:password \"YourServerPassword\"}" docker compose up
```

or via an `.env` file located in the same directory as the downloaded `docker-compose.yml`:

```sh
# .env
CONFIG_EDN="{:password \"YourServerPassword\"}"
```

