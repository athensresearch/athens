# Athens

Open-Source [Roam](http://roamresearch.com/). For more background, please read the [Athens FAQ](https://roamresearch.com/#/app/ego/page/OaSVyM_nr) on my public Roam.

# Contributing

Please refer to [CONTRIBUTING.md](https://github.com/tangjeff0/athens/blob/master/CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](https://github.com/tangjeff0/athens/blob/master/CODE_OF_CONDUCT.md).

## Getting Started

You will need:

- a `java` sdk: I'm using [openjdk 11.0.2](https://jdk.java.net/archive/)
- `lein`: Clojure's main [package manager](https://leiningen.org/) (lein installs the correct version of Clojure for you)
- `yarn` or `npm`: I'm using [yarn](https://www.npmjs.com/package/yarn)

Because `yarn` and `lein` are straightforward, and the only system dependency needed is `java`, I don't think `docker` is critical for local development.

Once your OS recognizes your `java` sdk, you have `lein` and `yarn`, simply clone the repo, run `lein dev`, `yarn`, and go to [localhost:3000](http://localhost:3000).

## Built With

- [datascript](https://github.com/tonsky/datascript) frontend port of Datomic database
- [re-frame](https://github.com/day8/re-frame) for reactive state management (CLJS's redux)
- [re-posh](https://github.com/denistakeda/re-posh) re-frame + datascript
- [reitit](https://github.com/metosin/reitit) for routing
- [shadow-cljs](https://github.com/thheller/shadow-cljs) for JS transpiling and hot reload
- [instaparse](https://github.com/Engelberg/instaparse) for format and link parsing

# Roadmap / Objectives

- to provide a self-hosted option, easily deployable on your machine
- to provide a hosted option using Datomic and their open-source license
  - if hosted, maintaining best practices (such as end-to-end encryption) and complying with standards like GDPR
- to provide a React Native mobile client
- to begin development of an open protocol for bi-directional links between Roam and other open-source alternatives

# Questions

Send a message in the #athens channel of the [Roam Slack](https://roamresearch.slack.com/join/shared_invite/enQtODg3NjIzODEwNDgwLTdhMjczMGYwN2YyNmMzMDcyZjViZDk0MTA2M2UxOGM5NTMxNDVhNDE1YWVkNTFjMGM4OTE3MTQ3MjEzNzE1MTA) or ping me on Twitter at [@tangjeff0](https://twitter.com/tangjeff0).

---

![Athens](https://www.greeka.com/photos/attica/athens/hero/athens-1920.jpg)
