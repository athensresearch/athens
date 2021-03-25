# Development

## Running Athens Locally

[Video version of this for Mac](https://www.loom.com/share/63618f2a2b2249e3923577fb88fabfdc).

These dependencies are needed to get Athens up and running. To install them, follow the instructions in the links.

1. [Java 11 and Leiningen](https://purelyfunctional.tv/guide/how-to-install-clojure/) \(Leiningen installs Clojure\)
2. [Node 12](https://nodejs.org/en/download/) and [Yarn](https://classic.yarnpkg.com/en/docs/install/#mac-stable)

_If you want to use Windows Subsystem for Linux \(WSL\),_ [_try this tutorial_](https://www.notion.so/Beginner-Clojure-Environment-Setup-Windows-36f70c16b9a7420da3cd797a3eb712fa#6a53854de58d4f07ba6319d868fba29c)_._

After you've got these dependencies, clone the Git repository to your hard drive:

```text
git clone https://github.com/athensresearch/athens.git
```

Then `cd athens/` and run the following commands.

Pull JavaScript dependencies:

```text
yarn
```

Pull Java dependencies and build, then start a local HTTP server for Athens:

```text
lein dev
```

In another terminal, run:

```text
yarn run electron .
```

Another window should open automatically. That's your Athens!

Now make sure you can run code in a REPL and that you know how to use re-frame-10x.

### Running in Docker

Docker doesn't work perfectly well anymore, because we are using Electron. Electron requires access to local resources such as `resources/index.html`.

If you run `yarn run electron .` from your local system, but are running Athens from within Docker, it won't work. Furthermore, if you run `yarn run electron .` from within your Docker system, the GUI won't popup on your local system. The workaround would be to sync the `resources/` files from Docker to a local folder.

The following command runs Athens in a docker container, but does not provide a workaround to actually run Electron.

```text
docker build -t athens .
docker run -it -p 3000:3000 -p 8777:8777 -p 9630:9630 athens
```

## Deploying Athens and Devcards

You should deploy your version of Athens and [Devcards](https://github.com/bhauman/devcards) if you are making UI-releated pull requests to Athens. This will allow developers and designers to interact with your code, which is essential for reviewing UI changes.

Athens Devcards can be found at [https://athensresearch.github.io/athens/cards.html](https://athensresearch.github.io/athens/cards.html).

### Automated Deploys

We've setup GitHub Actions so that each time you commit to your fork on GitHub, GitHub Actions automatically lints, tests, and styles your code.

If these scripts pass, GitHub builds your code and then deploys it to [https://YOUR\_GITHUB.github.io/athens/](https://YOUR_GITHUB.github.io/athens/) and [https://YOUR\_GITHUB.github.io/athens/cards.html](https://YOUR_GITHUB.github.io/athens/cards.html).

To begin doing automated deploys, just make sure your Actions are enabled at [https://github.com/YOUR\_GITHUB/athens/actions](https://github.com/YOUR_GITHUB/athens/actions). Then start pushing code!

### Manual Deploys

To build and deploy Athens and Devcards from your local development environment:

1. Build your JavaScript bundle\(s\) with either `lein dev`, `lein devcards`, or `lein compile`.
2. Run `lein gh-pages`.
3. Open http:///github.io/athens/ and http:///github.io/athens/cards.html. Sometimes this takes a minute to be updated.

Notes:

* If you want to compile Athens and Devcards one time without hot-reloading, run `lein compile`.
* If you are actively developing Athens and not Devcards, run `lein dev` to hot-reload the Athens application.
* If you are actively developing DevCards and not Athens, run `lein devcards` to hot-reload Devcards.
* If you want to build Athens and Devcards, because you are testing a component on DevCards and Athens at the same time, you should run `lein dev` and `lein devcards` in two terminals.
* If both builds are running, it doesn't matter which port you go to \(i.e. `3000` or `3001`\), because both HTTP servers can serve assets.
* More docs should be written in the future on how to connect a REPL to either build, depending on your text editor.

## Connecting your REPL

The REPL is one of the core features of Clojure. REPL-driven programming can make you code faster, with less tests and bugs. This [video](https://vvvvalvalval.github.io/posts/what-makes-a-good-repl.html#what_does_a_good_repl_give_you?:~:text=What%20does%20a%20good%20REPL%20give%20you%3F,-The) demonstrates this.

* Make sure you can run Athens locally before proceeding with this section.
* Refer to shadow-cljs [editor integration docs](https://shadow-cljs.github.io/docs/UsersGuide.html#_editor_integration) for more details.
* nREPL port is 8777, as defined in [shadow-cljs.edn](./shadow-cljs.edn).

### Cursive

[https://www.loom.com/share/a2cc5f36f8814704948a57e8277c04e9](https://www.loom.com/share/45d7c61703324089a425a9c91b14445b)

### CIDER

[Video tutorial](https://www.loom.com/share/a2cc5f36f8814704948a57e8277c04e9)

### Calva

```text
Editor - Visual Studio Code
Calva plugin: v2.0.126 Built on: 2020-07-09
OS - Windows 10, MacOS Catalina v10.15.6
```

1. In VS Code, run `ctrl+shift+c` and `ctrl+shift+j` \(`ctrl+alt+c ctrl+alt+j` in Windows 10\) to jack into a repl session.
2. Pick shadow-cljs.
3. Select `:main` and `:renderer` profile for shadow-cljs to watch.
4. Select the `:renderer` build to connect to.
5. In another terminal tab, run `npx electron .`

   ![load the namespace](doc/vscode-calva-repl-config.PNG)

### Vim Plugins

* [ ] TODO vim-iced
* [ ] TODO conjure
* \[X\] TODO fireplace

#### Fireplace

[Fireplace](https://github.com/tpope/vim-fireplace) is a popular Clojure\(script\) development plugin for Vim \(and Neovim\) text editor. It's main dependency is the [cider-nrepl](https://github.com/clojure-emacs/cider-nrepl) which already included as a development dependency.

Assume you already executed the commands described above in different terminal sessions and have the Athens instance running. And of course assume you installed vim-fireplace plugin too.

```text
lein dev # in one terminal, running nrepl server on port 8777
yarn run electron . # another terminal running the Athens app itself
```

Now open any Clojure file in Vim. This will load vim-fireplace plugin and necessary commands. First, we need to connect Clojure \(not Clojurescript yet\) runtime;

```text
:FireplaceConnect 8777
```

Clojure part is done. Now to connect Clojurescript runtime with vim-fireplace;

```text
:Piggieback :renderer
```

To test your development environment you can try to evaluate some Clojurescript and see the results on Athens running in electron;

```text
:CljsEval (js/alert "hello!")
```

You supposed to see an alert on electron app saying "hello!" and your Vim instance would be blocked until you acknowledge the alert message.

If all goes well, now you can see documentation of symbols \(binding: K\), go to definition \(binding: \[ C-d\) and so fort. See `:help fireplace` for more information.

## Using re-frame-10x

The right sidebar has [`re-frame-10x`](https://github.com/day8/re-frame-10x/tree/master/src/day8) developer tools. You can toggle it open and close with `ctrl-h`, but you must not be focused on a block \(ctrl-h has a specific action in some operating systems\).

Once you have 10x open, you can hover over blocks' bullets to see some of their datascript data.

By default, 10x is closed everytime Athens starts. Sometimes you want 10x to be open immediately on start. To do, comment out the two lines of JavaScript code in `index.html`, where localStorage sets 10x to be closed by default.

## Running CI Scripts Locally

After each submitted PR to Athens, GitHub Actions runs the continuous integration workflow declared in `.github/workflows/build.yml`. This workflow runs scripts from [`script/`](script) to test, lint, and build Athens. You can see these workflows in practice in the [Actions tab](https://github.com/athensresearch/athens/actions/).

However, it's a lot faster if you run these tests locally, so you don't have to submit a PR each time to make sure the workflow succeeds. You may need to install additional dependencies, though.

### Testing

No additional installation is needed. Just run this:

```text
lein test
```

The output will look something like this:

```text
$ lein test

Testing athens.block-test

Testing athens.parser-test

Testing athens.patterns-test

Ran 4 tests containing 16 assertions.
0 failures, 0 errors.
```

### Linting

We are linting Clojure code using [clj-kondo](https://github.com/borkdude/clj-kondo). Our clj-kondo configuration is in [`.clj-kondo/config.edn`](.clj-kondo/config.edn).

For this linting to work, you will need to install `clj-kondo`. Instructions are in [`clj-kondo`’s installation guide](https://github.com/borkdude/clj-kondo/blob/master/doc/install.md).

To see the problems reported by clj-kondo, run `script/lint`. Example run:

```text
$ script/lint
linting took 257ms, errors: 0, warnings: 0
```

Your editor may also be able to integrate with clj-kondo’s output. For example, if you use [Calva](https://marketplace.visualstudio.com/items?itemName=betterthantomorrow.calva) for VS Code, then clj-kondo’s messages are reported in the Problems panel.

### Clojure Styling

To format your code or check that your code is formatted correctly, you will need to use `cljstyle`. Instructions for installing it are [in `cljstyle`’s README](https://github.com/greglook/cljstyle/tree/master#installation).

To check if your Clojure code is formatted correctly, run `cljstyle check`. If there is no output and the return code is zero, you’re good. You can also run `script/style`, but currently it only works if you’re running Linux.

To reformat all your Clojure files in place, run `cljstyle fix`.

### Unused Variable Checking

To set this up, first make sure that a global `clojure` binary is installed. You won’t necessarily have a `clojure` binary installed just because you installed Leiningen.

Next, just run `script/carve`. The first time you run it it will download [Carve](https://github.com/borkdude/carve) as a dependency, which takes about a minute and outputs lots of messages. On subsequent runs `script/carve` won’t output anything unless an unused variable was found.

## Git and GitHub Style Guide

### Commits

Follow guidelines from [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/). Specifically, begin each commit with one of the following types:

```text
build:
ci:
chore:
docs:
feat:
fix:
perf:
refactor:
revert:
style:
test:
```

See some real examples in our [commit history](https://github.com/athensresearch/athens/commits/master).

### Issues

Please create issues using [our templates](https://github.com/athensresearch/athens/issues/new/choose). However, you will almost certainly get feedback and help faster in our [Discord](https://discord.gg/GCJaV3V)!

### Pull Requests

If your PR is related to other issue\(s\), reference it by issue number. You can close issues smoothly with [GitHub keywords](https://help.github.com/en/enterprise/2.16/user/github/managing-your-work-on-github/closing-issues-using-keywords):

```text
close #1
fix #2
resolve #2
```

This repo only allows those with merge permissions to "Squash and Merge" PRs. This makes reverts easier if they are needed.

