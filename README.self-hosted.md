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

Pick a [release](https://github.com/athensresearch/athens/releases) you'd like to use, download the `docker-compose.yml` file in the release to a folder, and then run `docker-compose up --detach` to run the services in the background.

For example, for `v1.0.0-alpha.rtc.27`:

```sh
curl -L -o docker-compose.yml https://github.com/athensresearch/athens/releases/download/v1.0.0-alpha.rtc.27/docker-compose.yml
docker-compose up --detach
```

The server will be acessible at `localhost:80`, and all data will be saved at `./athens-data`.

If any of the services fails to launch, you can use `docker-compose logs SERVICE_NAME` to inspect what the problem is. You can also run `docker-compose ps`  to see all running services. You should see that services `fluree`, `athens`, and `nginx` are up and `healthy`.

The `fluree` service can fail to launch if it does not have enough permissions for the `./athens-data` folder.
You can work around this particular failure more by manually creating the data folder via 

```
mkdir -p ./athens-data/fluree
chmod -R 777 athens-data/fluree
```

You can override the app configuration via an environment variable:

```sh
CONFIG_EDN="{:password \"YourServerPassword\"}" docker-compose up
```

or via an `.env` file located in the same directory as the downloaded `docker-compose.yml`:

```sh
# .env
CONFIG_EDN="{:password \"YourServerPassword\"}"
```

To update your deployment, curl the new `docker-compose.yml` file and restart docker-compose entirely:

```
# curl a new version of Athens described by docker-compose
curl -L -o docker-compose.yml https://github.com/athensresearch/athens/releases/download/v1.0.0-alpha.rtc.33/docker-compose.yml

# restart docker-compose
docker-compose down
docker-compose pull
docker-compose up --detach
```


## DigitalOcean

Athens the team has tested the backend beta server on DigitalOcean the most, and not as much yet on other cloud providers like AWS or GCP.

A minimum of 4gb of memory are needed.

![image](https://user-images.githubusercontent.com/8952138/141150237-dce5f183-f25c-4a9b-9526-dcc310c09a44.png)

Use marketplace docker image: `docker 19.03.12 on Ubuntu 20.04` which has docker and docker-compose pre-installed.

![digital-ocean-docker-image](https://user-images.githubusercontent.com/8952138/141150155-7be8bce5-1804-431c-9cd0-4cf11e9c8d87.jpg)

If you resize your droplet image, if you have trouble opening the console, just wait a few minutes and refresh the Digital Ocean dashboard.

Once your droplet has been created, open the console, run the instructions in [docker](docker), add the IP address to your Athens client to connect to the server!

![Athens Vision _ Ecosystem Visualization _ Product - Frame 13](https://user-images.githubusercontent.com/8952138/141150925-9f8df004-faa0-4fbe-9875-c276d60c5118.jpg)

