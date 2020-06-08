# Contributing to Athens

Whether you are a designer, developer, or none of the above, please see our [v1 Project Board](https://github.com/athensresearch/athens/projects/2) to get started!

# Development Environment

## Getting Athens to run locally

These dependencies are needed to get Athens up and running. To install them, follow the instructions in the links.

1. [Java 11 and Leiningen](https://purelyfunctional.tv/guide/how-to-install-clojure/) (Leiningen installs Clojure)
1. [Node 12](https://nodejs.org/en/download/) and [Yarn](https://classic.yarnpkg.com/en/docs/install/#mac-stable)

After you've got these dependencies, clone the Git repository to your hard drive:

```
git clone https://github.com/athensresearch/athens.git
```

Then `cd athens/` and run the following commands.

Pull JavaScript dependencies:

```
yarn
```

Pull Java dependencies and build, then start a local HTTP server for Athens:

```
lein dev
```

When these scripts are done, your terminal will read `build complete`. Athens can then be accessed by pointing a browser to http://localhost:3000/ on UNIX or http://127.0.0.1:3000/ on Windows.

## Viewing devcards

[Devcards](https://github.com/bhauman/devcards) are pages that show just one component of the web app, for the purpose of demonstrating or testing how that component looks when rendered with certain data.

To open this project’s devcards:

1.  Run `lein dev` like in the previous step.

2.  Open http://localhost:3000/cards.html.

3.  (optional) Using your fork, run `lein gh-pages` to see your own branch, e.g.
    https://tangjeff0.github.io/athens/cards.html

## Running tests locally

When you submit a pull request, the tests listed in `.github/workflows/build.yml` are run automatically and may report problems with your suggested changes.

If you want to run these tests without having to create a pull request, you’ll need to install a few more dependencies. When installed locally, some of these testing tools have modes that help you fix problems, not just identify them.

### Unit tests: `lein test`

No additional installation is needed. Just run this:

```
lein test
```

The output will look something like this:

```
$ lein test

Testing athens.block-test

Testing athens.parser-test

Testing athens.patterns-test

Ran 4 tests containing 16 assertions.
0 failures, 0 errors.
```

### Code style and lint checks: `script/lint`, `clj-kondo`

We are linting Clojure code using [clj-kondo](https://github.com/borkdude/clj-kondo). Our clj-kondo configuration is in [`.clj-kondo/config.edn`](.clj-kondo/config.edn).

For this linting to work, you will need to install `clj-kondo`. Instructions are in [`clj-kondo`’s installation guide](https://github.com/borkdude/clj-kondo/blob/master/doc/install.md) ([permalink](https://github.com/borkdude/clj-kondo/blob/7e7190b0bf673a6778c3b2cbf7c61f42cd57ee03/doc/install.md)).

To see the problems reported by clj-kondo, run `script/lint`. Example run:

```
$ script/lint
linting took 257ms, errors: 0, warnings: 0
```

Your editor may also be able to integrate with clj-kondo’s output. For example, if you use [Calva](https://marketplace.visualstudio.com/items?itemName=betterthantomorrow.calva) for VS Code, then clj-kondo’s messages are reported in the Problems panel.

### Clojure code formatting: `script/style`, `cljstyle`

To format your code or check that your code is formatted correctly, you will need to use `cljstyle`. Instructions for installing it are [in `cljstyle`’s README](https://github.com/greglook/cljstyle/tree/master#installation) ([permalink](https://github.com/greglook/cljstyle/tree/b44e0d6bb50a73102d8f7ff08f75874de4d7f9f2#installation)).

To check if your Clojure code is formatted correctly, run `cljstyle check`. If there is no output and the return code is zero, you’re good. You can also run `script/style`, but currently it only works if you’re running Linux.

To reformat all your Clojure files in place, run `cljstyle fix`.

### Unused variable checking: `script/carve`, Carve

To set this up, first make sure that a global `clojure` binary is installed. You won’t necessarily have a `clojure` binary installed just because you installed Leiningen.

Next, just run `script/carve`. The first time you run it it will download [Carve](https://github.com/borkdude/carve) as a dependency, which takes about a minute and outputs lots of messages. On subsequent runs `script/carve` won’t output anything unless an unused variable was found.

# Git and GitHub Style Guide

## Commits

Follow guidelines from [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/). Specifically, begin each commit with one of the following types:

```
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

## GitHub Issues

Please create issues using [our templates](https://github.com/athensresearch/athens/issues/new/choose). However, you will almost certainly get feedback and help faster in our [Discord](https://discord.gg/GCJaV3V)!


## Pull Requests

If your PR is related to other issue(s), reference it by issue number. You can close issues smoothly with [GitHub keywords](https://help.github.com/en/enterprise/2.16/user/github/managing-your-work-on-github/closing-issues-using-keywords):

```
close #1
fix #2
resolve #2
```

This repo only allows those with merge permissions to "Squash and Merge" PRs. This makes reverts easier if they are needed.
