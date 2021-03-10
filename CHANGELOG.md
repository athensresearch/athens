# Changelog

All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

## [1.0.0-beta.53](https://github.com/athensresearch/athens/compare/v1.0.0-beta.52...v1.0.0-beta.53) (2021-03-09)


### Bug Fixes

* **block-ref:** better reference number ([#781](https://github.com/athensresearch/athens/issues/781)) ([1c75578](https://github.com/athensresearch/athens/commit/1c75578c8496725c5f7debbad20972c60ea24c97)), closes [#742](https://github.com/athensresearch/athens/issues/742)
* **electron:** issue updating Mac App ([#777](https://github.com/athensresearch/athens/issues/777)) ([cf259a1](https://github.com/athensresearch/athens/commit/cf259a1de5e98d44f4cf3c8525b793b54a131c72))
* **enter/tab:** do not apply changes to previous line when fast keystrokes ([#768](https://github.com/athensresearch/athens/issues/768)) ([f449fcb](https://github.com/athensresearch/athens/commit/f449fcb51821ef073b9f666677372863b4826c26))


### Documentation

* use gifs ([07dc655](https://github.com/athensresearch/athens/commit/07dc65528059d943531ab14d15e0ce5b53ed5819))

## [1.0.0-beta.52](https://github.com/athensresearch/athens/compare/v1.0.0-beta.51...v1.0.0-beta.52) (2021-03-09)


### Bug Fixes

* **clj-kondo:** upgrade CI, with-let, specter, remove #_:clj-kondo/ignore ([#769](https://github.com/athensresearch/athens/issues/769)) ([40dabd7](https://github.com/athensresearch/athens/commit/40dabd71998514b7368a8d73959b4e6e27f5cfd1))
* **web:** theme and graph conf issue [#773](https://github.com/athensresearch/athens/issues/773) ([#779](https://github.com/athensresearch/athens/issues/779)) ([8c42437](https://github.com/athensresearch/athens/commit/8c424372d9c5f8f15f54f3b9703b1d4196909e4c))
* unlinked reference overflowing content ([#770](https://github.com/athensresearch/athens/issues/770)) ([defc78d](https://github.com/athensresearch/athens/commit/defc78d3ddfa7faafc585f7e9203d5cdd5c70bb2))


### Documentation

* add download links for M1 ([6d33825](https://github.com/athensresearch/athens/commit/6d33825605ce0e09d4a5b37887c318bf6be1d96a))
* update screenshot url ([b76f45f](https://github.com/athensresearch/athens/commit/b76f45fe76fe89ba6a37764d369b23289cbaa582))
* **README:** update screenshots, add PH badge ([#774](https://github.com/athensresearch/athens/issues/774)) ([713b4a3](https://github.com/athensresearch/athens/commit/713b4a337b5716306a525d89e353311fa8cbcb7b))

## [1.0.0-beta.51](https://github.com/athensresearch/athens/compare/v1.0.0-beta.50...v1.0.0-beta.51) (2021-03-07)


### Features

* **graph:** local-graphs and view customization ([#767](https://github.com/athensresearch/athens/issues/767)) ([e015a04](https://github.com/athensresearch/athens/commit/e015a049f8a77f9e7f5d09638307d18fb27356ac))
* **toolbar:** add a "Load Test DB" button for web demo ([#764](https://github.com/athensresearch/athens/issues/764)) ([685117c](https://github.com/athensresearch/athens/commit/685117c0598e9ab8f58baf7ebfa938b67a003401))

## [1.0.0-beta.50](https://github.com/athensresearch/athens/compare/v1.0.0-beta.49...v1.0.0-beta.50) (2021-03-05)

### Chore

* update electron and electron-builder ~> darwin-arm64 for M1 Mac


### Bug Fixes

* **linked-refs/block-embed:** drag & drop multiple blocks ([#743](https://github.com/athensresearch/athens/issues/743)) ([6f7407d](https://github.com/athensresearch/athens/commit/6f7407d723a4d7638d65475a9ae2398326059166))

## [1.0.0-beta.49](https://github.com/athensresearch/athens/compare/v1.0.0-beta.48...v1.0.0-beta.49) (2021-03-04)


### Bug Fixes

* **electron:** Avoid race-condition with filesystem mtime ([#734](https://github.com/athensresearch/athens/issues/734)) ([fdc5bbf](https://github.com/athensresearch/athens/commit/fdc5bbf5a6c5556fc0a6d1933c00e9b1286a97d0))

## [1.0.0-beta.48](https://github.com/athensresearch/athens/compare/v1.0.0-beta.47...v1.0.0-beta.48) (2021-03-04)


### Bug Fixes

* **typo:**  package.json hiddren->hidden ([#725](https://github.com/athensresearch/athens/issues/725)) ([148d02e](https://github.com/athensresearch/athens/commit/148d02ebad8b5ac949ca47b39f4ec2c27937b1c1))


### Enhancements

* **auth:** have another cond in case networking or other error ([#721](https://github.com/athensresearch/athens/issues/721)) ([bdae4ec](https://github.com/athensresearch/athens/commit/bdae4ec56381f3d46619b17be2dc568e2d06b5c3))
* **graph:** clicking on node navigates to page ([#731](https://github.com/athensresearch/athens/issues/731)) ([94fbd64](https://github.com/athensresearch/athens/commit/94fbd64192b36726f68e5b366fd5c487af7d9c5e))
* **parser:** Add LaTeX support ([#726](https://github.com/athensresearch/athens/issues/726)) ([3a3fb1f](https://github.com/athensresearch/athens/commit/3a3fb1f28cad0038e3e11a4d7c5418233cb519d2))

## [1.0.0-beta.47](https://github.com/athensresearch/athens/compare/v1.0.0-beta.46...v1.0.0-beta.47) (2021-03-02)


### Features

* **block-embed:** block-embed and component refactor [#584](https://github.com/athensresearch/athens/issues/584) ([#719](https://github.com/athensresearch/athens/issues/719)) ([7b65099](https://github.com/athensresearch/athens/commit/7b65099a1116caad7d873791ddd294e52f068c1b))


### Bug Fixes

* **title:** Empty title and regex in block query ([#720](https://github.com/athensresearch/athens/issues/720)) ([785ee38](https://github.com/athensresearch/athens/commit/785ee3834b66315d56c865e5c10879a620f337b6))

## [1.0.0-beta.46](https://github.com/athensresearch/athens/compare/v1.0.0-beta.45...v1.0.0-beta.46) (2021-02-26)


### Performance

* use shadow-cljs release, improve job dependencies ([#704](https://github.com/athensresearch/athens/issues/704)) ([f6793d6](https://github.com/athensresearch/athens/commit/f6793d67df79f8d77b695b9f9a9c0e75a9e537db))

## [1.0.0-beta.45](https://github.com/athensresearch/athens/compare/v1.0.0-beta.44...v1.0.0-beta.45) (2021-02-26)


### Bug Fixes

* **electron:** make sure main-window exists before sending ([#699](https://github.com/athensresearch/athens/issues/699)) ([68eb2d8](https://github.com/athensresearch/athens/commit/68eb2d8e9364caf6392aa2edef91e19f4c12b903))


### Documentation

* added calva jack-in command for windows ([#701](https://github.com/athensresearch/athens/issues/701)) ([49dbddf](https://github.com/athensresearch/athens/commit/49dbddfe19bd0124fea12e472cd08238b7fc57b0))

## [1.0.0-beta.40](https://github.com/athensresearch/athens/compare/v1.0.0-beta.39...v1.0.0-beta.40) (2021-02-16)


### Features

* **graph-viz:** first pass ([#645](https://github.com/athensresearch/athens/issues/645)) ([e57f0ae](https://github.com/athensresearch/athens/commit/e57f0aed323ee8064b4cd92d7f9eac8a800dbede))


### Enhancements

* **analytics:** opt-out includes Sentry, don't capture info logs, global alert ([#652](https://github.com/athensresearch/athens/issues/652)) ([4964c76](https://github.com/athensresearch/athens/commit/4964c765bb4ef3f269ec94cd09e63ab3d515cca1))
* **analytics:** track opt-in/opt-out ([#654](https://github.com/athensresearch/athens/issues/654)) ([24cdc04](https://github.com/athensresearch/athens/commit/24cdc04f798770827102beef1703e7fb18895dc2))

## [1.0.0-beta.39](https://github.com/athensresearch/athens/compare/v1.0.0-beta.38...v1.0.0-beta.39) (2021-02-14)


### Bug Fixes

* **keybindings:** partial solution to [#573](https://github.com/athensresearch/athens/issues/573) ([#578](https://github.com/athensresearch/athens/issues/578)) ([c33f283](https://github.com/athensresearch/athens/commit/c33f283963bc7bb02e5163884e9c97a97c7c4c0f))
* **parser:** parse bare urls [#549](https://github.com/athensresearch/athens/issues/549) ([#636](https://github.com/athensresearch/athens/issues/636)) ([c0dfa79](https://github.com/athensresearch/athens/commit/c0dfa797085b25598c5f27ca1e53c89d4ba45215))
* [#635](https://github.com/athensresearch/athens/issues/635), only open in right sidebar on SHIFT-click ([#637](https://github.com/athensresearch/athens/issues/637)) ([f6b88b5](https://github.com/athensresearch/athens/commit/f6b88b5e6ddcc319f7d083c938b9a9a9cff7efd8))
* linked reference should show the formatting ([#634](https://github.com/athensresearch/athens/issues/634)) ([d5b1241](https://github.com/athensresearch/athens/commit/d5b12419b9eb43965d799ae38872653e6d4c6491))
