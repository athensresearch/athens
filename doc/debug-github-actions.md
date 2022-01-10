# How to debug github actions

Sometimes things fail on github actions but not locally.
The best and fastest way to debug this is to try and setup a local environment that's similar to the github runner.

This command will start a docker container in the background running ubuntu and with `~/work/athens` (change to yours) folder mounted on `/sandbox`:
```
docker run -d -it -v ~/work/athens:/sandbox/ --name sandbox-container clojure:tools-deps /bin/sh
```

Now you can connect to it:
```
docker attach sandbox-container
```

This will start `sh` inside the container.

The `clojure:tools-deps` docker image has clojure, but doesn't have node, xfvb, or electron system deps, so we'll need to install it:
```
apt-get update
apt-get install -y xvfb
apt-get install -y libgconf-2-4 libnss3 libatk-bridge2.0-0 libgdk-pixbuf2.0-0 libgtk-3-0 libgbm-dev libasound2
apt-get install -y curl
curl -fsSL https://deb.nodesource.com/setup_16.x | bash -
apt-get install -y nodejs
npm install -g yarn
```

Change the code in `test/e2e/electron-test.ts` that says `if (process.env.CI)` to `if (true)` to force the CI only logic. 

Now you should be able to run project commands:
```
cd /sandbox
yarn
yarn client:e2e
```

The `yarn client:e2e:only:verbose <test-name>` script is especially useful to see the verbose logs and figure out where e2e tests are stuck.

Be aware that filesystem operations over docker mapped volumes are much slower, and `yarn` in particular will be very slow.

When you're done you can remove this container:
```
docker container rm sandbox-container
```


