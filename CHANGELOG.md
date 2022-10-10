# Changelog

All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

## [2.1.0-beta.5](https://github.com/athensresearch/athens/compare/v2.1.0-beta.4...v2.1.0-beta.5) (2022-10-10)


### Features

* add top-level css variable for app height ([dd3791c](https://github.com/athensresearch/athens/commit/dd3791cd0dfd1052e3d1a9edbaf87a2ff1f5ec68))
* clearer menu in dark mode ([9315c7f](https://github.com/athensresearch/athens/commit/9315c7f9633f6dcc536aac55ff820c47a5edc735))
* highlight current page name in sidebar ([ce44032](https://github.com/athensresearch/athens/commit/ce44032e71bd4dd7435b5a1d7f6ce19058803b2b))
* more prominent notifications ([94cc942](https://github.com/athensresearch/athens/commit/94cc942cbbc86fab690a3e7c58f0c91e5370ecdd))
* notification content parsed and rendered ([37abf6c](https://github.com/athensresearch/athens/commit/37abf6c2fdb1356c553c69a8e2c08f1d1fdd7e8b))
* notifications grouped by page ([7867c78](https://github.com/athensresearch/athens/commit/7867c78a0b09e43f2b899dacae8cead440f912e2))
* rendered notification object title ([57b7ac5](https://github.com/athensresearch/athens/commit/57b7ac54b9bb85c2d30ba3e1064a5083dcf17642))


### Bug Fixes

* daily notes dont break ([05582cb](https://github.com/athensresearch/athens/commit/05582cb4a55cea6bb6ba74e5089160ab4b0e650f))
* don't crash on bad position ([086d034](https://github.com/athensresearch/athens/commit/086d034ee53b73678d25aaaff960cc02938b26da))
* don't error out when re-frame-10x is missing ([b28117d](https://github.com/athensresearch/athens/commit/b28117da4c8d68bca9e9f32e9224bfe9655354e6))
* ignore properties when testing whether page is empty ([b222614](https://github.com/athensresearch/athens/commit/b222614fa7c3a00b3dfed5fe79cde53fe2d8bd1b))
* items on page shouldn't overlap ([0fb46e7](https://github.com/athensresearch/athens/commit/0fb46e7ce0ce7c86d4319f30a12372bf263ee99b))
* left sidebar respects tasks ff ([bfce6af](https://github.com/athensresearch/athens/commit/bfce6afa282ebd0bbebeff80bfd7834aa2fff57d))
* main items in left sidebar aligned properly ([ac1ac3b](https://github.com/athensresearch/athens/commit/ac1ac3bec1cce2ccfb565c742113ea15aa38bf61))
* notification contents should appear again ([419c140](https://github.com/athensresearch/athens/commit/419c14009fa87085a43446a65437a29a1b3e0069))
* page header aligns correctly ([2b96c5b](https://github.com/athensresearch/athens/commit/2b96c5b5e2fe95474d0cdc7e26fa3fca80b26805))
* Show comments on zoomed in view ([c428319](https://github.com/athensresearch/athens/commit/c42831974e1bdb411c92e46f11f8db6696d537f6))


### Refactors

* add menu to notification items ([d0bdc7a](https://github.com/athensresearch/athens/commit/d0bdc7adeda581f5e24ae4806fff50d7b969b81b))
* cleaner simpler notification style ([1c08a1d](https://github.com/athensresearch/athens/commit/1c08a1d762cd47451f49674c1c123a75af90c3b8))
* improve page sizing implementation ([0da1e16](https://github.com/athensresearch/athens/commit/0da1e16d61e6b4019b46dd4fcf879e5e16b78ad7))
* new page components ([22ac1e4](https://github.com/athensresearch/athens/commit/22ac1e4840e6fdacdf947af98376985d5229a32a))
* remove unnecessary line ([7d44280](https://github.com/athensresearch/athens/commit/7d442800ea3641a2e970503d76dea61536157300))
* use app height instead of just vh units ([67c46ac](https://github.com/athensresearch/athens/commit/67c46ac2bdbddb1e067af2c7e57baae2a5a6482f))
* way simpler notifications renderer ([132f270](https://github.com/athensresearch/athens/commit/132f270b98ebd95c8e3fdaf5379b152bbccc2934))


* lint ([b9f1f65](https://github.com/athensresearch/athens/commit/b9f1f65ad94562f9fc1d8b08c9051951acb4107a))
* lint ([0bc66d1](https://github.com/athensresearch/athens/commit/0bc66d1ca0fbbf85a5736eaf907c475769dcf390))
* lint ([be7bdff](https://github.com/athensresearch/athens/commit/be7bdff081ae057e7e02a1c6e5e9db619ece7d86))
* lint ([f891163](https://github.com/athensresearch/athens/commit/f8911639e5cff99ab005dbf174bbc85858b801fb))
* lint ([03baf5a](https://github.com/athensresearch/athens/commit/03baf5af221ba7dd651d1b619657a926bfa38209))
* lint ([a0cd186](https://github.com/athensresearch/athens/commit/a0cd1865195537951aa204a5757f0b9595526e37))
* remove commented code ([df45c41](https://github.com/athensresearch/athens/commit/df45c4131e77b6bf33dd32f12ae38e4da98e9b35))
* remove storybook ([4c718b2](https://github.com/athensresearch/athens/commit/4c718b2aa578d7ba620e7135b9fd35a1fefdede3))
* remove unused files ([287af51](https://github.com/athensresearch/athens/commit/287af51357f01d1fcd0e90573b90ff646eab429d))
* show hints for null fn calls ([dac2bbe](https://github.com/athensresearch/athens/commit/dac2bbe3ee4b8087a438eae8abc6cb933c777872))

## [2.1.0-beta.4](https://github.com/athensresearch/athens/compare/v2.1.0-beta.3...v2.1.0-beta.4) (2022-09-27)


### Features

* add auth to api ([e470bb3](https://github.com/athensresearch/athens/commit/e470bb38abdd59371b100c5851ad2de6d27e5d1f))
* add basic content negotiation to api ([8914d37](https://github.com/athensresearch/athens/commit/8914d37c9aeb3b943686045d9ed6f79b1ab4289b))
* add card layerstyle ([c9d6afd](https://github.com/athensresearch/athens/commit/c9d6afdcc628b6f5818f39007a223ec2a4cd07e6))
* add dummy block editor to card edit popover ([5ccb104](https://github.com/athensresearch/athens/commit/5ccb104867aff81abdc95850e67ffba814309fb0))
* add empty component, show in tasks ([161c67c](https://github.com/athensresearch/athens/commit/161c67c0975b0171b63db5e230be9021a2bb6b6d))
* add empty for sidebar shortcuts ([c373da4](https://github.com/athensresearch/athens/commit/c373da429d1e84a64198cf23e9f22f70e616a677))
* add empty to notifications popover ([ee284cb](https://github.com/athensresearch/athens/commit/ee284cbd526d1044f51e43f165f785cf0b148175))
* add rect tabs style, make default ([b11ff22](https://github.com/athensresearch/athens/commit/b11ff221ac2a8f7591231f1211995c6f2d0b028e))
* add selectors to path api ([ea09793](https://github.com/athensresearch/athens/commit/ea0979362219dd656c7b87a6be6a8f95bfa0ccd6))
* Added some style magic from shanberg to task refs ([363b5ca](https://github.com/athensresearch/athens/commit/363b5ca13529f8e1a4150c3e4a3ae34d2dd2416b))
* better handling of multiple menu sources ([11f6e1f](https://github.com/athensresearch/athens/commit/11f6e1f7a2aacab6f7835a59ea390187985954a5))
* block in new interactions for tasks in sidebar ([62ee269](https://github.com/athensresearch/athens/commit/62ee269007b3e2d5fc1dbaa52666667d0d24397d))
* break up import events into ~900kb chunks ([fdd6ab3](https://github.com/athensresearch/athens/commit/fdd6ab31c370ae30a53439b66e107e8471a631d4))
* can convert block to task ([ac6aaa1](https://github.com/athensresearch/athens/commit/ac6aaa12d333eb892727b13c7ba67924aced97bc))
* content spacing improvements ([c21c112](https://github.com/athensresearch/athens/commit/c21c112feeaf8455332d82b714437892e73e45ff))
* context menu supports mutliple targtes ([a2b2123](https://github.com/athensresearch/athens/commit/a2b2123a1b3826bf30b36e4d9983f968f9a16aea))
* context menus can combine ([0e6f3a8](https://github.com/athensresearch/athens/commit/0e6f3a85cd91dab89a74353883dc70bad8d6254b))
* contextmenu offerors are keyed ([df4cf85](https://github.com/athensresearch/athens/commit/df4cf8544da9f049116aa9b0f7b0a42f20ea2be0))
* Convert block to task & save task title. ([649ae96](https://github.com/athensresearch/athens/commit/649ae96a4b704cd323466a93dfe895e7a8105e79))
* first pass on queries for tasks ([#2235](https://github.com/athensresearch/athens/issues/2235)) ([30fdffc](https://github.com/athensresearch/athens/commit/30fdffce25dc4a5645eda51eb1483bb8b0e6f1c7))
* gate api on feature flag ([dc6c3b8](https://github.com/athensresearch/athens/commit/dc6c3b8001473bed4d08150b9576b023e41474ea))
* import from roam using internal representation ([a755b75](https://github.com/athensresearch/athens/commit/a755b75952e64f6b2bf0b4578a974af86ef06d63))
* improve card over column style ([21a1257](https://github.com/athensresearch/athens/commit/21a12574ba36d03898f52cef0456fdfdd2732b28))
* improve tasks and tasks in refs ([1dc191d](https://github.com/athensresearch/athens/commit/1dc191dc9c8f95ed731a4ccfce35e7b70d963879))
* improved ui around blocks ([d0ef9b3](https://github.com/athensresearch/athens/commit/d0ef9b3f2f6b092af52b4eb2b3d2ee1cda4f227f))
* improving task layout ([74a3045](https://github.com/athensresearch/athens/commit/74a3045cc10e78b99d84335c61d2422dd604818c))
* initial menu works ([c9069bf](https://github.com/athensresearch/athens/commit/c9069bfda59ed2b668ba2ca7bebd67a432e1406e))
* kanban components forward ref ([7b65f4d](https://github.com/athensresearch/athens/commit/7b65f4d8a4bd761f33e1eb5fd0d6afdf7a650d11))
* more data shown in task popover ([aeebe0c](https://github.com/athensresearch/athens/commit/aeebe0c9918ef14382169c482361b43f834ca779))
* more data shown in task popover ([54550ee](https://github.com/athensresearch/athens/commit/54550ee2b4caea22b1a46e8603bfcdecc3b95cbc))
* more work on comments around tasks ([a1a2876](https://github.com/athensresearch/athens/commit/a1a287647c786f77056a4207f409a9740c1038a9))
* more work on comments around tasks ([c4c166c](https://github.com/athensresearch/athens/commit/c4c166c6dd89a0f7ca7c44837df7acb9cb2120ed))
* much added sidebar task stuff ([9485ad6](https://github.com/athensresearch/athens/commit/9485ad678eb410a5e46ff3d3ff1f160e35f70ab7))
* Navigate tasks with keyboard part 1 ([e71dbe8](https://github.com/athensresearch/athens/commit/e71dbe8281bf96d12298f95171147973d890a6c5))
* new icons ([02e91d0](https://github.com/athensresearch/athens/commit/02e91d043c3c87a70f97a1de1aad65c719de6d0a))
* nice sidebar goodies ([6108f0d](https://github.com/athensresearch/athens/commit/6108f0d81d2a853ddbe9105da4894fe476fe440b))
* objects can claim exclusive menus ([b02b1d2](https://github.com/athensresearch/athens/commit/b02b1d2a0953bf18e3ca353363c06c7944233349))
* Parsing to text wraps block-refs in `(())` ([db9e868](https://github.com/athensresearch/athens/commit/db9e868f8a86f941e42fe9f2633869dbccf03dd0))
* polish taskbox component ([47b57cc](https://github.com/athensresearch/athens/commit/47b57cc0f572f3b513bfc1e5e5d6ce6662f49e6a))
* popover-style task editing ui ([7f71d67](https://github.com/athensresearch/athens/commit/7f71d6770de1dc44202dc6c2ff574333f61b7174))
* reactive task status ([f0d7a80](https://github.com/athensresearch/athens/commit/f0d7a80205b34b1d895848d025a7a9daabbf748d))
* readable table query ([#31](https://github.com/athensresearch/athens/issues/31)) ([aaef7d4](https://github.com/athensresearch/athens/commit/aaef7d46da363797605464345ae14e495079055f))
* restyle things shown in sidebar ([99b58e8](https://github.com/athensresearch/athens/commit/99b58e86c5937856b5bb035dd6a0dfe1cd03b83b))
* show fake traffic lights on macos when not focused ([9237ec4](https://github.com/athensresearch/athens/commit/9237ec45083d7ddaf8aab5ec916ccd84f0c123bb))
* showing current block title in toolbar ([d94095d](https://github.com/athensresearch/athens/commit/d94095d7d776f1d3db5a1a542f0835d8f326d2ad))
* simple GET /block/uid POST /add API ([5a25533](https://github.com/athensresearch/athens/commit/5a255339458cc7911e27db78a5a5a10843f812ae))
* stable api for context menu ([3df5878](https://github.com/athensresearch/athens/commit/3df5878d0ea8d6e51c3f57b930fe9dbccb81e9ec))
* starting out ([9e3f35a](https://github.com/athensresearch/athens/commit/9e3f35a225f6a6393c48dfbb3ee5bac784d7e1d4))
* Task indent & unindent. ([c53bfda](https://github.com/athensresearch/athens/commit/c53bfda05f880fff616e848251ed574a39d763c7))
* Task ref to navigate just like block refs do. ([3280202](https://github.com/athensresearch/athens/commit/3280202af2e8813138d1d79aad9898f16b12466f))
* Task Ref using `:task/title` ([b0ef860](https://github.com/athensresearch/athens/commit/b0ef860b5101cd20c02826bd56eb7d33545495ca))
* tasks completable from sidebar ([ca33f7d](https://github.com/athensresearch/athens/commit/ca33f7d13d00749f30a9717b53140939dbc48a3e))
* Tasks keyboard navigation up ([160c4be](https://github.com/athensresearch/athens/commit/160c4bed7c828d6d50dd3af90ae34ce4e1c7079d))
* Tasks title editor [Enter] support ([d0ca422](https://github.com/athensresearch/athens/commit/d0ca422b05b5a45217a82ae08d18946787e76fc2))
* theme toggle in sidebar ([85a105b](https://github.com/athensresearch/athens/commit/85a105bb5058d0956f9c999f795f2588cffe8bfe))
* update task box style ([88c8f01](https://github.com/athensresearch/athens/commit/88c8f0144a875f45f3bf59b4968c06fc45ed4d33))


### Bug Fixes

* [Enter] and [Backspace] fixed ([b0d2f22](https://github.com/athensresearch/athens/commit/b0d2f222a3400c7f3494412088968ca9823ec13a))
* `unindent` not loosing focus. ([8fa9b7c](https://github.com/athensresearch/athens/commit/8fa9b7c77724be334bbf3755502b7026e80e724d))
* add grid space for reactions on comments ([2c6879d](https://github.com/athensresearch/athens/commit/2c6879de5ef8be3676e542bcd5d7639779eee744))
* add key to interface ([ecce381](https://github.com/athensresearch/athens/commit/ecce3812398ede16393fd5d2e8db4e803aaf0ab9))
* add padding to end of list ([cbca488](https://github.com/athensresearch/athens/commit/cbca488d29475941038193f1e36464683954a20c))
* add some space to bottom of sidebar items ([7454f47](https://github.com/athensresearch/athens/commit/7454f4793e6da5db1e903ac3264671946cd71949))
* also allow lists in internal-representation->atomic-ops ([f860f9f](https://github.com/athensresearch/athens/commit/f860f9fcdba4fa6a9e4db8e4919a078f3e2df456))
* Backward compatible focusing during unindent ([af05226](https://github.com/athensresearch/athens/commit/af052261573049751f9a0a8d4c6fe102cff6606f))
* better alignment for buttons in left sidebar ([0552c9a](https://github.com/athensresearch/athens/commit/0552c9a3ee4b31b5d7519296c82f55d43bd20f6b))
* better alignment for task display settings control ([05b519a](https://github.com/athensresearch/athens/commit/05b519a39fb9110d93a21102f8d66ffb9a239cf4))
* better style for input in task form ([5e8f68a](https://github.com/athensresearch/athens/commit/5e8f68a5304163849799f1a337034c78d85504de))
* block anchors draggable again ([f037cb6](https://github.com/athensresearch/athens/commit/f037cb66977b78ed4152c65f2dc1780174498114))
* block background should fit block size ([535e4bc](https://github.com/athensresearch/athens/commit/535e4bc01e8c5cc76795549d94ae762bc17025d9))
* block buttons and line height match up ([677f42b](https://github.com/athensresearch/athens/commit/677f42bd8c7a8cfd8b37e6ea620f8ea3cc9ad491))
* block container gets correct ref ([408bcf3](https://github.com/athensresearch/athens/commit/408bcf357a68a1955c52073a7fa6d4fa81fc142f))
* block errors render properly ([a3642c5](https://github.com/athensresearch/athens/commit/a3642c57952c99a8cd7f2e4a7b9a7bb5cc2074ca))
* block menu appears on rightclick of anchor too ([bd3149c](https://github.com/athensresearch/athens/commit/bd3149c857db58448a71a25a8290cf18d9920ea5))
* block selection background fits block better ([8c00610](https://github.com/athensresearch/athens/commit/8c00610d70f5075f5a8190eadde7326b4da8ec1f))
* bookmark icon has right border size ([fc510eb](https://github.com/athensresearch/athens/commit/fc510eb2fc56bd2b3dbb344a1ac335941a3749ab))
* can copy multiple refs ([c8fa1e6](https://github.com/athensresearch/athens/commit/c8fa1e6b4a1031cddcdc36b43f0c7e1f26c80138))
* can drag blocks again ([67d4480](https://github.com/athensresearch/athens/commit/67d44805b8b77baa939b04c1b5dd8884f4bcf4aa))
* can scroll kanban cols again ([5968e22](https://github.com/athensresearch/athens/commit/5968e22a182f9a17619fd0d23058f84209918c67))
* can show menu items in comment ([e430cae](https://github.com/athensresearch/athens/commit/e430cae3023708f752facc2ea181278a3ea277c3))
* clearer indicator for task details form ([5160e0c](https://github.com/athensresearch/athens/commit/5160e0cf47d990cb0266b306319e90b9bcd8474d))
* clearer text in db dialog ([40d2b5d](https://github.com/athensresearch/athens/commit/40d2b5d29d9ed09eebaa881636168a0d3a7d09ef))
* code cleanup ([8d55f68](https://github.com/athensresearch/athens/commit/8d55f686dc80db7a91cbb0491f4554dadd4e8d00))
* comments boxes don't stretch to far ([bb383c7](https://github.com/athensresearch/athens/commit/bb383c7102d4b4333e5d20c0510b0a25a916d64b))
* couple menu issues ([b90b6d9](https://github.com/athensresearch/athens/commit/b90b6d9fb6e9602cecc17f4b4655d578fea5733d))
* daily notes pages shouldn't get squished ([b7b8b16](https://github.com/athensresearch/athens/commit/b7b8b16ae5c7bc0fa81a4275b1eb54f8c987c3eb))
* db-dump handling even if `:block/key` is `nil` ([e25e989](https://github.com/athensresearch/athens/commit/e25e9891eb630afd58869643087e1465fce6000a))
* disable user button when user not on a page ([445f9d2](https://github.com/athensresearch/athens/commit/445f9d224eacf5034c0e3be8de4540cad10600d9))
* docker-compose.yml only need 1 restart in each services ([256845b](https://github.com/athensresearch/athens/commit/256845bfce4e50343ede1390b218b2a087234224))
* don't animate taskbox state on initial render ([40415be](https://github.com/athensresearch/athens/commit/40415be24d35811676ce1e1751ea3384bcd5e883))
* double block new is a block move instead ([6f4e56e](https://github.com/athensresearch/athens/commit/6f4e56e057461e589f14c3711b2f2698e152f8d1))
* double click anchor to navigate works ([15712c6](https://github.com/athensresearch/athens/commit/15712c69a68e056aafefbbb2042a298bd14ad787))
* edit on embed should edit transcluding block ([0b6919d](https://github.com/athensresearch/athens/commit/0b6919df1175cccbe16cc71693df7d883a3663a9))
* eliminate console warnings from unused props ([a8a7485](https://github.com/athensresearch/athens/commit/a8a7485c7adcff70fc238bd1cc4dfcd03351cf9f))
* eliminate console warnings from unused props ([31408c1](https://github.com/athensresearch/athens/commit/31408c1b5f07b2c85387e4c77153b5824fbdcc70))
* eliminate misc console errors ([94fc666](https://github.com/athensresearch/athens/commit/94fc66647e5644aed3dc786f6ad7087c69ffb971))
* embeds should show children ([448b134](https://github.com/athensresearch/athens/commit/448b1348870152bfc34adc99cd93b78da345e4e9))
* emoji shouldn't break out of bounds ([392bafa](https://github.com/athensresearch/athens/commit/392bafa2b79a4b209dd2d6915473dde3cf44578d))
* enter handler for block page, show title for tasks. ([0bf5192](https://github.com/athensresearch/athens/commit/0bf5192bffd7ea419a6d8791a379f8bda01fff3c))
* expand button disabled when no tasks present ([ec7c4b1](https://github.com/athensresearch/athens/commit/ec7c4b118bcc0b949db963c8f2a4be69a649e261))
* ff should be {} if missing ([8a75180](https://github.com/athensresearch/athens/commit/8a75180d29c64d3b047ad689d9abb59feed50e5a))
* Handle tasks without creation time ([b4a1180](https://github.com/athensresearch/athens/commit/b4a118066cc978cd1f24f8821df57e0f08481f8d))
* hide cancelled tasks from sidebar ([30068e6](https://github.com/athensresearch/athens/commit/30068e6bcacad281c109c56751109b32b050449e))
* icons in menu buttons should be the right size ([478818e](https://github.com/athensresearch/athens/commit/478818e2191834186dc7bef78aadd58459370b75))
* icons shouldn't have underlying grid ([fb7f453](https://github.com/athensresearch/athens/commit/fb7f4530ed60e6df7f52eb3429655f7c7e5db6d8))
* icons were wrong ([37ffc74](https://github.com/athensresearch/athens/commit/37ffc74775e381aa5d86bf83b9c5cd3d8f64f6da))
* increase server max memory ([e727393](https://github.com/athensresearch/athens/commit/e72739370e4be201f87092b9ba53ce63318d8e01))
* Indent keeps focus ([3d54ce2](https://github.com/athensresearch/athens/commit/3d54ce2b2053c2bfe0803821e60a6a0689bb4285))
* internal-representation->atomic-ops should throw on invalid repr ([06f483e](https://github.com/athensresearch/athens/commit/06f483e2259e99d858018ad1d3caeb629ba850db))
* janky scrolling when dragging card on kanban ([ac0c8b8](https://github.com/athensresearch/athens/commit/ac0c8b8773b5f9f65cd20065a5d5cf3238876caa))
* long titles in sidebar items shouldn't overflow ([f6544e5](https://github.com/athensresearch/athens/commit/f6544e59ba968d41aab1ed49879931990a6f65f5))
* make icons happy again ([5ed9f30](https://github.com/athensresearch/athens/commit/5ed9f305e94eba247ce867bcfa0214b799f19f3e))
* menu should close when opening block, menu should support block selectiosn ([f5b94c2](https://github.com/athensresearch/athens/commit/f5b94c234038f8c44a8ff61b1da8f3aa1f77d955))
* minor code style ([3a13c71](https://github.com/athensresearch/athens/commit/3a13c71b3f13d7fb59c21f3a2f89097fa6b779cd))
* minor improvement ([169b241](https://github.com/athensresearch/athens/commit/169b2419e05c96fe1dbc45f30f51734556de2190))
* misc prop passing errors ([900d412](https://github.com/athensresearch/athens/commit/900d412ab9d50d00d763e6350f24051134292d4b))
* option menus have same line height as other menus ([808ccc8](https://github.com/athensresearch/athens/commit/808ccc8d8ea0204ca80c0136400caa96ba412b24))
* prevent error when using block type menus ([9a88a38](https://github.com/athensresearch/athens/commit/9a88a38d39ea89596553f9ab5493b9ae9de2292c))
* proper icon import ([1fe0f90](https://github.com/athensresearch/athens/commit/1fe0f90aedcb3b82c7819d40aea782bd7bb9153c))
* reffed tasks wrap text ([93e53ed](https://github.com/athensresearch/athens/commit/93e53edfa1e056016197479d9ff179a0daa89e7d))
* remove extra contextmenucontxt declaration ([25f79d5](https://github.com/athensresearch/athens/commit/25f79d555567cb2815f4e0bd87fb5fded4f33ea0))
* remove extra space after task ([95d1dba](https://github.com/athensresearch/athens/commit/95d1dbab85274dfd9ac5254969a971f0b5b3b8ba))
* restore focus behavior prior to chakra update ([cc54c4c](https://github.com/athensresearch/athens/commit/cc54c4c2afb53951548c0ae6eadc6f23776b2af5))
* restore focus outlines when desired ([bbe9625](https://github.com/athensresearch/athens/commit/bbe962509e036141eb2045e74672bd5ceca7b2db))
* right sidebar drag handle doesn't scroll away ([e2b2749](https://github.com/athensresearch/athens/commit/e2b27496234b2e774dbd08bc98ade50d5849d4fa))
* right sidebar drag handle shouldn't get disconnected ([9e6e381](https://github.com/athensresearch/athens/commit/9e6e381567f9579344b3f3e59407b48c6844b41d))
* right sidebar els align to top ([6e37056](https://github.com/athensresearch/athens/commit/6e370568cf398ec1865a383fbd1997c59626e6f6))
* right sidebar items have proper bg ([d301322](https://github.com/athensresearch/athens/commit/d3013227a9c613a6ca4ab23fa1254c222721b669))
* right sidebar items shouldn't be squished ([df1bb46](https://github.com/athensresearch/athens/commit/df1bb46f2b8cac524ea45b950ab1ebd05fdc1704))
* right sidebar should trigger toolbar when scrolled ([c93b16c](https://github.com/athensresearch/athens/commit/c93b16c8040b2ec0bd538a68112c7128afc531b0))
* Right Sidebar title block-ref support ([3b6f554](https://github.com/athensresearch/athens/commit/3b6f554c1b7cbc094716fb3074ae7716eaebbab8))
* sidebar bottom section sticky ([ad22047](https://github.com/athensresearch/athens/commit/ad220474eab78b8b5900b43b867a41ab7840256b))
* smaller sidebar footer icons ([eb0c877](https://github.com/athensresearch/athens/commit/eb0c877f8f4865054f554f3b5901a517360a21e0))
* task children when clicked don't show up in zommed-in view ([9a3dc5f](https://github.com/athensresearch/athens/commit/9a3dc5f01ee6a230a58ed67b683b39b219de83fe))
* task menu gets correct default status ([83085d1](https://github.com/athensresearch/athens/commit/83085d1f231993c10a6a5278458a06b48e604881))
* Tasks Embed used wrong lookup vector. ([111e13d](https://github.com/athensresearch/athens/commit/111e13df8739be2a827b17cdc93dee1a96b1fb4f))
* title doesn't appear unless main content scrolled down ([d27eaac](https://github.com/athensresearch/athens/commit/d27eaac0cef13d91865ee1d2f635d0e1951316e1))
* toggle appears only on interaction ([3df5bad](https://github.com/athensresearch/athens/commit/3df5bad30278b16b1890d3d43bd23d07f0dc546d))
* transcluded tasks dont break ([a7c6750](https://github.com/athensresearch/athens/commit/a7c6750ff4a55d26bb6d4e21cd5643d54a364b03))
* turn properties/update-in to graph/update-in ([8e74817](https://github.com/athensresearch/athens/commit/8e74817dfce8fcff7dccafc40bd1ef9b73d882f9))
* vercel builds failing ([d64daa8](https://github.com/athensresearch/athens/commit/d64daa80b830bddb542ba91bf07de0d1f3c6a07b))


### Work in Progress

* basic API ([e9487eb](https://github.com/athensresearch/athens/commit/e9487eb51a6f440b7f4c31f81681605820f8feb6))


### Performance

* defer updating sidebar width in graph ([bc3155a](https://github.com/athensresearch/athens/commit/bc3155a7fd0177ac30994be172fa3202a20f1de2))
* Do not load `re-frame-10x` by default ([42287bb](https://github.com/athensresearch/athens/commit/42287bb73d7f5cddb612fe666a1168d35c12a06b))
* Don't spam `::inline-search.events/close!` ([a086a06](https://github.com/athensresearch/athens/commit/a086a06e9c7db9c6021d6d48fe798766c2aa6fd6))


* add scripts for debugging prod builds ([4de40c6](https://github.com/athensresearch/athens/commit/4de40c692f4d97257f3c95643010ac25d407d06e))
* carve ([7466804](https://github.com/athensresearch/athens/commit/746680420055158c7b57eaff42beac714d5f46ee))
* clean up testing code ([ea4aa06](https://github.com/athensresearch/athens/commit/ea4aa0674f5004385dcdfe2d1bd91f818387320e))
* comment unused notification fns ([afd5002](https://github.com/athensresearch/athens/commit/afd50025fd2a890b5a7a01dad29a30066fb38cb5))
* disable e2e on ci ([ae85676](https://github.com/athensresearch/athens/commit/ae85676e806997e6a0e9730c7d8cf9794c9664bb))
* don't auto load re-frame-10x on both builds ([e02abe2](https://github.com/athensresearch/athens/commit/e02abe294648548216c025d4ee73fd2ad084ff51))
* fix ([f95240e](https://github.com/athensresearch/athens/commit/f95240e825e1baea58aab58d8f6fc3772979a02f))
* fix ([f3e4369](https://github.com/athensresearch/athens/commit/f3e4369936c832c1ab2f745df8a2dd5d498f2584))
* fix lint ([b1a13a7](https://github.com/athensresearch/athens/commit/b1a13a7724f7e36b1e8e0e4b2db41fa2e3b509ce))
* fix lint ([3933dca](https://github.com/athensresearch/athens/commit/3933dca7b3f59347f07e80d6e825961260157c4d))
* lint ([e7cc0a1](https://github.com/athensresearch/athens/commit/e7cc0a1b912d349a605de36026e461e7a2764212))
* lint ([4fe5c43](https://github.com/athensresearch/athens/commit/4fe5c43b9c9b7969ac46f16393da6583d3288ba3))
* lint ([5b0ed47](https://github.com/athensresearch/athens/commit/5b0ed47862b511a6a8ba452ddfbe80b796449558))
* lint ([b852c97](https://github.com/athensresearch/athens/commit/b852c9714b946cd15adea1609cbd5b71ad9930d4))
* lint ([c674c6d](https://github.com/athensresearch/athens/commit/c674c6d5d4353c71bbb603b17b16d4ef47a4790f))
* lint ([3149dc2](https://github.com/athensresearch/athens/commit/3149dc23cda18fcfd61ef158d41f49ed2edf847a))
* lint ([addb56e](https://github.com/athensresearch/athens/commit/addb56eca231a7d5b3dd0be1f2dff50803aa28f5))
* lint ([ffe8bf8](https://github.com/athensresearch/athens/commit/ffe8bf8139563d84b74298edb6b15c32e4ce75f4))
* lint ([f71304f](https://github.com/athensresearch/athens/commit/f71304feadf2d1b5d1027a6f35abc4e57dfe528a))
* lint ([92de934](https://github.com/athensresearch/athens/commit/92de934a0275ce3e67ed3c0ee76036ecc53772f2))
* lint ([04d0c94](https://github.com/athensresearch/athens/commit/04d0c949587cd72b96857cb27178bb42799bee6e))
* lint ([2aedc69](https://github.com/athensresearch/athens/commit/2aedc69286e6bb6d6e7469b80eea65a4e5bf5756))
* lint ([aedead3](https://github.com/athensresearch/athens/commit/aedead30988d139822117b722bf46062136ee457))
* lint ([9c07042](https://github.com/athensresearch/athens/commit/9c07042f4835adfe12413092ff41a0064335411f))
* lint & carve ([84b7797](https://github.com/athensresearch/athens/commit/84b779799f235c29864fd84ec06a7a36d83fd07b))
* remove commented code ([4a0f28e](https://github.com/athensresearch/athens/commit/4a0f28e06e54e07fc180885f8cbc6a5c95086599))
* remove console log ([9d39632](https://github.com/athensresearch/athens/commit/9d3963252908522fe9c829c407c904a4b04f83eb))
* remove console log ([4a95e04](https://github.com/athensresearch/athens/commit/4a95e0402b638cecabcb2dfcf550e3293725f4a3))
* remove debug logs ([27d1b75](https://github.com/athensresearch/athens/commit/27d1b75db1f5e7878b9295a010ed71c88bbc1605))
* remove debug style ([b700e3a](https://github.com/athensresearch/athens/commit/b700e3a3c1144eb7807919be8a3bb228c76e4999))
* remove shared state in open undo test ([840700f](https://github.com/athensresearch/athens/commit/840700f0fba561d9bcff809b1791c5723dc5fbc1))
* remove storybook ([b1af957](https://github.com/athensresearch/athens/commit/b1af957c97af1bee9f9a7ddf85f9ba29020b7ec8))
* remove unused comments ([786b096](https://github.com/athensresearch/athens/commit/786b096841a6d6d84f9c50750f61143fc0ec206e))
* retire old menu ([a6368e1](https://github.com/athensresearch/athens/commit/a6368e137a3f626dce45a5a400472ae5dc6039f5))
* saving progress ([33c1e55](https://github.com/athensresearch/athens/commit/33c1e55b11ea45ea117de41635ed55a8ab4fce9a))
* show hints for null fn calls ([16324de](https://github.com/athensresearch/athens/commit/16324de1a018725decaa8d5947651c582cb3f3bf))
* style fix ([dc0727b](https://github.com/athensresearch/athens/commit/dc0727b1f8ba7b6ad3c5d409b85a8e8782d1cb3a))
* style, link & carve fixes ([a73b9bd](https://github.com/athensresearch/athens/commit/a73b9bd53d89e8ffc5adaa1599479a2bfa1aaac3))
* style, lint & carve happy ([d0a1c51](https://github.com/athensresearch/athens/commit/d0a1c51367bfb5a55f71e9d71eae2dfacdb7e47b))
* transpile class fields out of node_modules ([8f597b6](https://github.com/athensresearch/athens/commit/8f597b6c61b33d027623928467a5c21169fca75a))
* update chakra ([2c700ba](https://github.com/athensresearch/athens/commit/2c700baa1b11ce7c15c01beb661515cec9c6dfc7))
* use react 18 ([a6e5edb](https://github.com/athensresearch/athens/commit/a6e5edba24685654a55d7dc8ca971c27584283a6))


### Enhancements

* checkmark update status ([acfc2ca](https://github.com/athensresearch/athens/commit/acfc2ca7f7ca82cd68b6bf1a8ca5228f220ac0ac))
* improved task edit ui ([d9bca25](https://github.com/athensresearch/athens/commit/d9bca25fc767987a78253e8e3f6533886f561ddd))
* nicer style for unrenderable blocks in table ([f78a1d6](https://github.com/athensresearch/athens/commit/f78a1d6c785d33e749412978d2b32ee5755c8820))
* polish block rows, refactor grid size ([21c00e5](https://github.com/athensresearch/athens/commit/21c00e5e29f4ff5540d6c88b10e410e90d2d2dfe))
* table can scroll when needed ([2e5e533](https://github.com/athensresearch/athens/commit/2e5e533b2e18634d2023f0a64eea32ada5684297))
* transition block background ([5b815b3](https://github.com/athensresearch/athens/commit/5b815b3e2d35fd6a166f1892254b65141f29ffe4))


### Refactors

* :properties/update-in supports first/last children and is named :graph/update-in ([84e9eac](https://github.com/athensresearch/athens/commit/84e9eac03ae7db306413f2044d9797ffbb33e8e3))
* anchor uses new menu fn ([68fa5fa](https://github.com/athensresearch/athens/commit/68fa5fae66ca4f1fe42cc9e20722a67b64491b93))
* api based on path read/write ([cc246e2](https://github.com/athensresearch/athens/commit/cc246e25304546eeb238269dc515634824d3c898))
* block and anchor use same menu, plus support for debug menu content ([d2a10f2](https://github.com/athensresearch/athens/commit/d2a10f2e1865f4dade35d82fc5ca1ce6e6f53fb3))
* block container uses new contextmenu ([2958339](https://github.com/athensresearch/athens/commit/29583398c1ebaeef9c2e856298d64b412052d64c))
* block menu comes from block core ([f583022](https://github.com/athensresearch/athens/commit/f5830225b514ffae82528d534e03046fbc8bb313))
* block mouse interaction stuff moved out of container el ([dbc779c](https://github.com/athensresearch/athens/commit/dbc779cccf4c72e9413699d61ca3e3eeb6f2775c))
* clean up anchor and toggle ([96cc151](https://github.com/athensresearch/athens/commit/96cc151ed840f442d566a9e3c50b381bf6d7a520))
* cleaner block container impl ([ef9a01b](https://github.com/athensresearch/athens/commit/ef9a01b1a27307948006202e9337497431c8130a))
* cleaner context menu prop api ([312673c](https://github.com/athensresearch/athens/commit/312673c07ebcd36ead797c5dc9fe181634065a07))
* cleanpup ([86b2215](https://github.com/athensresearch/athens/commit/86b2215a2e5adbbb0f81f9b5c2c382d6704a2ba6))
* clearer name for menu fns ([cf07022](https://github.com/athensresearch/athens/commit/cf0702279d4ab2d5c986c065f2e4c56152a9ee47))
* comments use new context menu ([a132e2f](https://github.com/athensresearch/athens/commit/a132e2f70a88a0a6db652d2d579fc42c69d4d061))
* handlers not events ([5d338be](https://github.com/athensresearch/athens/commit/5d338be6ecc4623e12c3c73bad6ce84fa4ca8a4b))
* Introduced `tasks.view` ns ([7db9a02](https://github.com/athensresearch/athens/commit/7db9a029dc64128827831a16bcafc54a477f8883))
* misc cleanup ([cb362a4](https://github.com/athensresearch/athens/commit/cb362a4279d9630101c282ef426e6d93cd611e1a))
* move context menu into its own space ([7264152](https://github.com/athensresearch/athens/commit/7264152221ab51093b0faff36152fb73fff7967c))
* move roam import into own ns ([6015481](https://github.com/athensresearch/athens/commit/60154813847f59644114b431e1f39c809b77e449))
* nicer comments, anchors have menu ([d4b4906](https://github.com/athensresearch/athens/commit/d4b49067bb94ed088a0557bd3e93e3e462ff279f))
* put presence and ref count in page gutter ([4fd96d1](https://github.com/athensresearch/athens/commit/4fd96d1b1e912fab9e1c30360f6fe2cc2b56c539))
* remove errorboundary from content, leave it up to container ([51266f4](https://github.com/athensresearch/athens/commit/51266f41a8d288ca8b7c027afcb90418ac093a77))
* remove extra dom elements ([fb356b2](https://github.com/athensresearch/athens/commit/fb356b20c85a998506fdb76fbcf8db53f30b3fba))
* remove extra wrappers ([55b7272](https://github.com/athensresearch/athens/commit/55b72720e1a20b31137b5c4155262f659493985e))
* remove redundant position op ([5c19765](https://github.com/athensresearch/athens/commit/5c19765f5ce632f6755827d8b7b1472ee14bb408))
* remove unused code ([b9bbef6](https://github.com/athensresearch/athens/commit/b9bbef6fb8577b9cf303d7f835f747530b0b1df5))
* rename 'database' to 'workspace' on front end ([ad12b6b](https://github.com/athensresearch/athens/commit/ad12b6bfa9921a931584945166636648ec0e09cd))
* rename inline task title fn ([8a8c76f](https://github.com/athensresearch/athens/commit/8a8c76fb01fb8088b4e7fb675b971e71e0f40495))
* shortcuts list uses same widget component ([d8ae781](https://github.com/athensresearch/athens/commit/d8ae7811e8421cd36e20e56a3432c2bcb343919f))
* tighter task outline view ([006c3b0](https://github.com/athensresearch/athens/commit/006c3b0bc654228461c12798a5f4a589e5f9b29e))
* use atomic events for dnd-image ([3a976b0](https://github.com/athensresearch/athens/commit/3a976b0f0e34e31a31344272dcbed0c052975eff))
* use existing binding ([0d5ab5c](https://github.com/athensresearch/athens/commit/0d5ab5c87e3dbd109a1eddabd20f99eae7b16204))
* use new empty component for sidebar ([d168416](https://github.com/athensresearch/athens/commit/d168416313280b21d782302135bc299a84b18d72))
* use new empty component for sidebar ([e94c2d0](https://github.com/athensresearch/athens/commit/e94c2d079c3d581331296d7dcb8611b0eb99bebe))
* use same component for task block and ref ([f31fce5](https://github.com/athensresearch/athens/commit/f31fce527fd11ff5355db75cc54e69743b4e059a))
* use same strategy for both toolbar showers ([e203b13](https://github.com/athensresearch/athens/commit/e203b1321118513d308f576afd7d2917bdcc4439))

## [2.1.0-beta.3](https://github.com/athensresearch/athens/compare/v2.1.0-beta.2...v2.1.0-beta.3) (2022-08-22)


### Features

* add notifications popover ([0f086a7](https://github.com/athensresearch/athens/commit/0f086a78f1a09faf5a36ba216d5ddc3150e5ecf6))
* add overline to distinguish daily notes ([2314be0](https://github.com/athensresearch/athens/commit/2314be02d4df94d280ed69b5897108ec44e6cb74))
* Athens Task Assignee support. ([0894819](https://github.com/athensresearch/athens/commit/0894819d4113a220a7104eddd5ffe9e4287747bf))
* Athens Task Priority. ([2ba1973](https://github.com/athensresearch/athens/commit/2ba197393505adfcd70cd360c9c15b9a36f1b742))
* basic implementation of updated layout ([bf6f8d4](https://github.com/athensresearch/athens/commit/bf6f8d4ca9d658f23f5a66403200e5732e5eeba6))
* basic updated reaction ([bf81c09](https://github.com/athensresearch/athens/commit/bf81c09d832b7409b5dd9cc28d1878e5960d87db))
* better shorcuts states ([e9fbd99](https://github.com/athensresearch/athens/commit/e9fbd9901cd65978f29edce89e952712d0d042d0))
* better shorcuts states ([0ac50aa](https://github.com/athensresearch/athens/commit/0ac50aaf201464696beb3f49ed5fa5a82a4c78f6))
* bootstrapping `:task/status` page ([62c3a99](https://github.com/athensresearch/athens/commit/62c3a99fd295ea6504f533c070b83419f8d227e0))
* comments have context menu ([c33dcd4](https://github.com/athensresearch/athens/commit/c33dcd472a3986f846fef88e968f537a16a871ad))
* comments toast reuses same toast ([1934bda](https://github.com/athensresearch/athens/commit/1934bdad93bc46ee171b2a1b3c14abade651dc6d))
* different reactiosn ([53b4dc2](https://github.com/athensresearch/athens/commit/53b4dc2e15fa785da6e9c13bf3adc5d063a48ad0))
* edit Task status ([80639d4](https://github.com/athensresearch/athens/commit/80639d40a286538543386d403cc6ec3d92b3f6c1))
* editing title via editor ([cfd91a0](https://github.com/athensresearch/athens/commit/cfd91a009b7a01b37d5123e93c089bb2f92aa463))
* hide properties auto-complete and property blocks if feature ([7399334](https://github.com/athensresearch/athens/commit/7399334ee392ec8ec9891b0014d6206c013d288a))
* in progress blocking in notifications ([9bcf87b](https://github.com/athensresearch/athens/commit/9bcf87b7f1001a202c44a0c91cef37c3fa4d2a75))
* in progress blocking in notifications ([12e4933](https://github.com/athensresearch/athens/commit/12e49331bc31e4cdc510ffaf4756a895d8947e1f))
* inbox page ([e3b25fb](https://github.com/athensresearch/athens/commit/e3b25fb00f01f01151bc80d8ab1b6849e9772827))
* popover apears, added message body ([4eb68ef](https://github.com/athensresearch/athens/commit/4eb68ef5f5c720eb141854df38d8a91081bfb9ef))
* reactive renderer use, and gated on feature flags ([0b06711](https://github.com/athensresearch/athens/commit/0b06711915dd9f4cc3cb2f121260d1874af6e517))
* reactive title ([7808239](https://github.com/athensresearch/athens/commit/780823979bbf35a7491c9aa7d8f51b2f67b46908))
* reactive-get-entity-type ([81cc3e9](https://github.com/athensresearch/athens/commit/81cc3e981edecfdb49af682225d29f5db5b324d4))
* real block type discovery ([e228ae4](https://github.com/athensresearch/athens/commit/e228ae4d415c693dd126cb6b17ef16b1ae946ea0))
* saving progress ([d62d374](https://github.com/athensresearch/athens/commit/d62d374ac86840f8c76998dac5020b186c891bca))
* support creating pages in :properties/update-in ([290e48c](https://github.com/athensresearch/athens/commit/290e48cc2ede74373d6b78e9cf4eb31ff1dd0bcb))
* Support editing of Task Title when there is no title block prop. ([5b80653](https://github.com/athensresearch/athens/commit/5b80653ed9411c5e5ad773815cee13d5b80fc381))
* Support presence on Task Title editor. ([2b9b580](https://github.com/athensresearch/athens/commit/2b9b58038aa901ebe6b64bac60e0de95fd568f3b))
* Task statuses configured on a graph. ([a294605](https://github.com/athensresearch/athens/commit/a294605e6b78dca80b56a71157f6ca2153fb5fa6))
* try info text for notification badge ([2bcfa72](https://github.com/athensresearch/athens/commit/2bcfa7277a1a99aa2598a048d31f24b39ba250e7))


### Bug Fixes

* -rule to Rule in tsx ([fe5ae24](https://github.com/athensresearch/athens/commit/fe5ae24208b099b2b4ec6ed0e19de108098ad83b))
* add missing icon ([facdae4](https://github.com/athensresearch/athens/commit/facdae488f80c175195611a905a0896eb070c0cf))
* all reaction items are buttons ([8c13b4b](https://github.com/athensresearch/athens/commit/8c13b4b7292b15ac526d4a7b975f2fd87ba1ade6))
* allow scrolling shortcuts ([9bcb6d8](https://github.com/athensresearch/athens/commit/9bcb6d80247f364f4b2c7369dddd6d5d96d77e58))
* always show open in main for daily ([f2c7553](https://github.com/athensresearch/athens/commit/f2c7553b33b60f7bed639cf73ecd3ff3cd4e7dcd))
* autocomplete menu works again ([848dd16](https://github.com/athensresearch/athens/commit/848dd16be316500ecfb50f3dddd611c182618e41))
* autocomplete menus work again ([153bc64](https://github.com/athensresearch/athens/commit/153bc64fc59dcb811d30a058da6d3597fb9da340))
* autocomplete should be autoComplete ([50ce9ed](https://github.com/athensresearch/athens/commit/50ce9eda135f0720a82deddff8808d1d3adaa88f))
* better daily notes fn ([79a07ec](https://github.com/athensresearch/athens/commit/79a07ecca2ff068b91019346613b33b896908cdd))
* better formatting ([2283663](https://github.com/athensresearch/athens/commit/22836639eaa66a402691a7cc40885bc7f0defb69))
* better icon sizing in comments ([585b9f1](https://github.com/athensresearch/athens/commit/585b9f1f91c8ce28ac5f863655b099993c3fc8c0))
* better layout, scrolling ([831c5fd](https://github.com/athensresearch/athens/commit/831c5fd18b60ab01ec9bd5fcec48eebbddb16f72))
* block toggle shouldn't be draggable ([21385e5](https://github.com/athensresearch/athens/commit/21385e5afa902b8e96b1aec621acf8f97fc1b66f))
* block-el should prioritize the reactive props ([172029a](https://github.com/athensresearch/athens/commit/172029ac3fc0cf13e95777bf8afecf05a4395f1e))
* brighter notification badge ([0bea69d](https://github.com/athensresearch/athens/commit/0bea69dc63568381435a4b427ab2602629275b59))
* Bring mouse selection back. ([b8ead5c](https://github.com/athensresearch/athens/commit/b8ead5c839c5dbbb84abf09fb824704f1ab08a73))
* can click other people's reactions ([03532ec](https://github.com/athensresearch/athens/commit/03532ec34e0035dc12286bcace89f9e78f03f3f9))
* can resize right sidebar ([fe30ee6](https://github.com/athensresearch/athens/commit/fe30ee61c3c9abecf5bfc75e79cf920ea475be3b))
* cannot move bullet under own children ([7301f96](https://github.com/athensresearch/athens/commit/7301f96784c2e91a4182a88ad899db05b30e648c))
* clean up page buttons ([07d8da4](https://github.com/athensresearch/athens/commit/07d8da40d371dff7d126b32b95c39820bc1ad7cd))
* correct icon attr casing ([092c6af](https://github.com/athensresearch/athens/commit/092c6af1748e855dc83c5bbc14b20b5aa72ce38f))
* correct prop name for sidebar width ([68e9b9f](https://github.com/athensresearch/athens/commit/68e9b9f77ab68121bbb255e99d5f98b1fce73068))
* correct transition for toolbar underlay ([01f7093](https://github.com/athensresearch/athens/commit/01f70939491529057955953ff03356f933e62b86))
* correct transition for toolbar underlay ([801455c](https://github.com/athensresearch/athens/commit/801455c4ade527953f716f4ca366812930c1d4d8))
* dom events ([abe6997](https://github.com/athensresearch/athens/commit/abe6997a115b7c19062b24acda357963aa6a7afe))
* don't allow removing in-mem db ([b441eab](https://github.com/athensresearch/athens/commit/b441eab19f55ccd43a1be60fd22faab0fcc91a18))
* don't clip daily notes ([a524d2d](https://github.com/athensresearch/athens/commit/a524d2d68aa143d5b78a8abacf1e86d02f3a8dfd))
* don't mount autocomplete els too often ([6b62ab4](https://github.com/athensresearch/athens/commit/6b62ab485e033e06ba92711af4dd4658c17a48d0))
* drag and drop properties ([4cb96cf](https://github.com/athensresearch/athens/commit/4cb96cf7a41a8b3e60aae49cc868c1f0ed17a707))
* drop after/before prop moves block to first ([3d2b1ba](https://github.com/athensresearch/athens/commit/3d2b1ba6e45f3fbf5ee9908cd11f2c2012823028))
* Enter behavior in tasks ([51322d7](https://github.com/athensresearch/athens/commit/51322d75d930f38c391bc4f0bf3d292eaa09ff97))
* enter on prop should split block ([5210a4d](https://github.com/athensresearch/athens/commit/5210a4d2c57b8afb78010c66e0a385d1b8094732))
* faster daily notes ([00c5ac2](https://github.com/athensresearch/athens/commit/00c5ac29419bdd37be520990672123a4de8cd432))
* gate emoji picker element on reactions feature flag ([3a31fb8](https://github.com/athensresearch/athens/commit/3a31fb8f78e70e1b4f824d6da0afa6b68e0c4169))
* hide debug cursor ([fed68c2](https://github.com/athensresearch/athens/commit/fed68c28ae2f9108042a9fb1cc0c27b255f97220))
* if multiple blocks selected, selection doesn't clear on context menuclick ([773dac7](https://github.com/athensresearch/athens/commit/773dac7ac5ef1bded55e8b6da5d54db0ba9dca33))
* import textarea pos correctly ([3a8171a](https://github.com/athensresearch/athens/commit/3a8171a5f7c98ed6a87fbcf9027cae4395d98343))
* include icon ([ac2eaef](https://github.com/athensresearch/athens/commit/ac2eaef8ff03aba9c5d94f91a090ccb25c6f3b36))
* include rest props ([4740646](https://github.com/athensresearch/athens/commit/4740646730084013586c2bb6b71664aa462510c3))
* layout fixes ([6daf077](https://github.com/athensresearch/athens/commit/6daf07744bd64d03bf32c71e8963508b1d2f94c0))
* minor fixes ([e2de0ef](https://github.com/athensresearch/athens/commit/e2de0ef76e378f80c937fa03ac03016e7023e302))
* misc issues ([5a75a0e](https://github.com/athensresearch/athens/commit/5a75a0ede0c638484cba6234a95d9112f746bcf5))
* misc issues ([0201521](https://github.com/athensresearch/athens/commit/02015214088f203d6c91c62b05d53fc47c883be2))
* misc toolbar cleanup ([54310a4](https://github.com/athensresearch/athens/commit/54310a4aa121bd5d9731b27ccc22a8c2674018c2))
* misc ui fixes ([c016278](https://github.com/athensresearch/athens/commit/c01627819cc3a85ed4fdf4d2c7adf0193b8e1e67))
* more predictable daily scrolling behavior ([d379df8](https://github.com/athensresearch/athens/commit/d379df8b4b7d36e05da63e4979648f91ee7dd770))
* no error from tooltip in menu ([ea38f5b](https://github.com/athensresearch/athens/commit/ea38f5b1cc02069ca6690d26d994f3c618fd04d6))
* no focus fighting on db modal ([55b6481](https://github.com/athensresearch/athens/commit/55b64819f146e583ed71d37ec72b3589813e8c11))
* notification for block author and don't double-add user when they comment on their own block ([b3f3c93](https://github.com/athensresearch/athens/commit/b3f3c93732d8c9c7b8ba2207c61f90e23171ce67))
* onArchive, stopPropagation ([ebdd539](https://github.com/athensresearch/athens/commit/ebdd5393031b383b28488279df9cbc7f842a941a))
* pages should stretch-sorry ([2b0fb35](https://github.com/athensresearch/athens/commit/2b0fb35ff704216079275fe5945b747e3ab89199))
* pass string to block/move ([d46b175](https://github.com/athensresearch/athens/commit/d46b175b35c4aca5d733b344f51866fe370870e1))
* props can also be dragged by name ([b098bcc](https://github.com/athensresearch/athens/commit/b098bcccccf19193a732634a613800c3d548d00d))
* remove broken icon ([70b3829](https://github.com/athensresearch/athens/commit/70b3829ef1218531eff3fda01f3370ceef4e115b))
* remove broken icon ([4e2d250](https://github.com/athensresearch/athens/commit/4e2d250f256d527fabe7c75bbff05c29e9f1a301))
* remove console log ([c2e701c](https://github.com/athensresearch/athens/commit/c2e701c2c355021a642108f6b05de01b035a37be))
* remove console log ([95eaa13](https://github.com/athensresearch/athens/commit/95eaa13444bbd481a2a729db9fe980033ee6d9ab))
* remove separation between context menu and click location ([5487840](https://github.com/athensresearch/athens/commit/54878402eea7c924c6d5a99acacf0d9984f92c32))
* resuable toast should be reusable ([b4bd9a6](https://github.com/athensresearch/athens/commit/b4bd9a68d8465baebdc517bb803952d4f23a830e))
* revert unintended changes ([bb8e5c0](https://github.com/athensresearch/athens/commit/bb8e5c043d2e398425644cefdc02f678f177b5a4))
* right-sidebar graph and page views ([15a226b](https://github.com/athensresearch/athens/commit/15a226b141e74618e4f17ec8c812615d8431c7d5))
* shortcut names don't overflow ([1d44670](https://github.com/athensresearch/athens/commit/1d44670afaeeb6f70fcea58cf0fa7087c218d792))
* simplify reaction style ([93da313](https://github.com/athensresearch/athens/commit/93da313316e62d6a5f4cdcc76ac1202a3f3a49f0))
* solve theme issue with buttons ([07fa188](https://github.com/athensresearch/athens/commit/07fa1888157a3f341e202ab010f31b392dc66889))
* solve theme issue with buttons ([66fd75a](https://github.com/athensresearch/athens/commit/66fd75a861009c884e26928ad623e6955ddcdf33))
* solve theme issue with buttons ([cbd8534](https://github.com/athensresearch/athens/commit/cbd85343fcbf62cce343823e138df9ad470d231b))
* solve theme issue with buttons ([9daac29](https://github.com/athensresearch/athens/commit/9daac29b2339b1398eda4edd1a031e08f5a18a98))
* working daily notes ([cd79554](https://github.com/athensresearch/athens/commit/cd795543e2186dec520398ead40586ff0708ba85))
* working main nav ([21ddeed](https://github.com/athensresearch/athens/commit/21ddeedc61361563aadde52aa28ede092fd9e57b))


### Performance

* blocks out of view don't do as much ([9809ee8](https://github.com/athensresearch/athens/commit/9809ee8f82068a6706820dd6d1feb5893fe1b0ae))


* **deps-dev:** bump karma from 6.3.3 to 6.3.16 ([400bb79](https://github.com/athensresearch/athens/commit/400bb794ee3cdcd612f62edc6470835e89b3a828))
* **deps:** bump ejs from 3.1.6 to 3.1.7 ([c926516](https://github.com/athensresearch/athens/commit/c92651632bde6a702b777e5d0e66265daaad5bb1))
* **deps:** bump follow-redirects from 1.14.7 to 1.14.8 ([433319d](https://github.com/athensresearch/athens/commit/433319dadf8e36fb3c8279d60f0ba449f50262b3))
* **deps:** bump jpeg-js from 0.4.3 to 0.4.4 ([283a583](https://github.com/athensresearch/athens/commit/283a58319a1d5c26b990e1e6f48636c813970c1b))
* **deps:** bump minimist from 1.2.5 to 1.2.6 ([56b7dd4](https://github.com/athensresearch/athens/commit/56b7dd46011cc41fc4ac9e94d63bd0c031ca0b0e))
* **deps:** bump plist from 3.0.2 to 3.0.5 ([4059c16](https://github.com/athensresearch/athens/commit/4059c1617e0d7963e0f8c2df64cbd741634f08a8))
* **deps:** bump terser from 4.8.0 to 4.8.1 ([dbcc1dd](https://github.com/athensresearch/athens/commit/dbcc1dd252043537be8446cc1109bed3302391e9))
* fix ([5da1512](https://github.com/athensresearch/athens/commit/5da1512e654774039f4eca678fadd5252b81e2e8))
* fix lint ([bce03e6](https://github.com/athensresearch/athens/commit/bce03e656f2b0eb013ca6ad014719c3d62806658))
* fix lint ([76a0a23](https://github.com/athensresearch/athens/commit/76a0a23eddfd4ee576b43e689fbfdbdd5c55c91d))
* fix lint ([2deb9f0](https://github.com/athensresearch/athens/commit/2deb9f0213fd5c20c28fe1a34b1f16992e8addbb))
* fix linting ([2866a45](https://github.com/athensresearch/athens/commit/2866a4598e626904a6e7bdde1432d1121734e071))
* fix linting ([79aa03c](https://github.com/athensresearch/athens/commit/79aa03cb53bc78a187187cd97b1e9141ac39c02c))
* lint ([029ecb9](https://github.com/athensresearch/athens/commit/029ecb92396b1e03e774cf6af54f71313244cba3))
* lint ([f635cea](https://github.com/athensresearch/athens/commit/f635cea302cd1db5420eab708041eafeb9346384))
* lint ([592a857](https://github.com/athensresearch/athens/commit/592a8572f3049e743dfe543b64e5d7b60c6b3ebc))
* lint ([226ddc4](https://github.com/athensresearch/athens/commit/226ddc4b100b086e4c9934062c23c68b4fc311ec))
* lint ([ee9d35b](https://github.com/athensresearch/athens/commit/ee9d35be596bebc05345ee10570ca75429e68398))
* lint ([e17578b](https://github.com/athensresearch/athens/commit/e17578bb3594f71a819967f2a1a3b4054fab0bb9))
* linting ([3f49135](https://github.com/athensresearch/athens/commit/3f4913500453136bcc20fb2f2e618bceb95c051f))
* re-enable e2e job ([c80371c](https://github.com/athensresearch/athens/commit/c80371ce221a5f4745868ff996630023aa219b04))
* remove unused ([ddc5673](https://github.com/athensresearch/athens/commit/ddc567317c032f60be6d1e50d1d9649e7268f969))
* remove unused ([3c2d10b](https://github.com/athensresearch/athens/commit/3c2d10b6368bcd9ec886453209c043bafa9d6c31))
* saving progress ([3f4593d](https://github.com/athensresearch/athens/commit/3f4593d3aa4380f1f25f60518bac6d0b0400a045))
* saving progress ([64760c4](https://github.com/athensresearch/athens/commit/64760c4b7f17cafc7b0821c50f35c709b2bb6098))
* saving progress ([9707463](https://github.com/athensresearch/athens/commit/9707463745a904ef9e783fcdda0707d66f8a4442))
* saving progress ([33fcf55](https://github.com/athensresearch/athens/commit/33fcf551207a64efbe6269d6640376ee2254c00d))
* type interface for rightsidebarresizecontrol ([c56cb94](https://github.com/athensresearch/athens/commit/c56cb94c718c18ad1d67739beb978aa6f26aaaad))
* update e2e ([b24c205](https://github.com/athensresearch/athens/commit/b24c205f794524d91e6ee93f214cc557a7d0b2aa))


### Enhancements

* quality of life fixes for headings in blocks and block refs ([ae06dad](https://github.com/athensresearch/athens/commit/ae06dad0a4e962a410ee20e175e90ebc4d05189c))
* **right-sidebar:** make width % based; persist width to graph ([eb6d1b4](https://github.com/athensresearch/athens/commit/eb6d1b43516f1fc3c7cdaccc94184fc17c3b1717))
* use settings as a modal, not as a page ([64b29f2](https://github.com/athensresearch/athens/commit/64b29f29906136bc0fc1f453a924bf2e8172e6f6))


### Refactors

* `block/content` -> `block/editor` ([8409d47](https://github.com/athensresearch/athens/commit/8409d47c036a22844c073b5786e4db0ee75f0813))
* `BlockTypeProtocol` taking care only of `content` ([2cd2934](https://github.com/athensresearch/athens/commit/2cd293451b4e282c5b75ce7f6afd04ebac202259))
* auto-add missing :block/uid to internal representation ([71640c9](https://github.com/athensresearch/athens/commit/71640c9a4758912a157c3abe84799e1843e1f9f5))
* Batched property value updates. ([25e1aa5](https://github.com/athensresearch/athens/commit/25e1aa578c5267a3f681cf1ed21360556bde8683))
* Better `select` implementation for `:task/status`. ([2195c6f](https://github.com/athensresearch/athens/commit/2195c6f666f114c9a7eb23c5e87fb4844aefc37a))
* current-user is You when not remote ([fefed0e](https://github.com/athensresearch/athens/commit/fefed0ed125d68a0e9b0dc3f370572b3c184fa6a))
* dndkit for sidebar shortcuts ([#2251](https://github.com/athensresearch/athens/issues/2251)) ([8d8871f](https://github.com/athensresearch/athens/commit/8d8871f5b3df107e6497125fe6c577d846525c35))
* Field titles and optional require. ([42547be](https://github.com/athensresearch/athens/commit/42547be1b3af03b625d7f3b90829061cd8665c85))
* init task status via :properties/update-in ([2b1db21](https://github.com/athensresearch/athens/commit/2b1db21774d76e53d5faf738cbac5c2638a65f34))
* look up task uids directly in props ([a10aa11](https://github.com/athensresearch/athens/commit/a10aa11190912798578798708e06d9cf0335b055))
* minor prop cleanup ([81af9a3](https://github.com/athensresearch/athens/commit/81af9a328407d877c52e73efe146e254bf6f7a75))
* more cleanup ([fe8eba4](https://github.com/athensresearch/athens/commit/fe8eba43213551a4c43d503423de5b803bb0d14f))
* move new fn to utils ([d3d5dd2](https://github.com/athensresearch/athens/commit/d3d5dd21cbdc7f9465927dd732b4ee19eacae788))
* moved `linked-ref` & `inline-refs` to `block/core` ([db4ed5e](https://github.com/athensresearch/athens/commit/db4ed5e0964634a84446585c29c6b116e23e24fe))
* only list-no sort filter group ([14c2d03](https://github.com/athensresearch/athens/commit/14c2d03ac9d10e884fa63d19b7b305f4e38879d9))
* optimize search-in-block-content for comments ([147a97b](https://github.com/athensresearch/athens/commit/147a97bb4e6e549cc53237b9915413c47b8d7a76))
* Really use `athens.types` ns ([1f3a046](https://github.com/athensresearch/athens/commit/1f3a046693406cb4220cca5d22093fbbc897393f))
* remove debug code ([e249b6e](https://github.com/athensresearch/athens/commit/e249b6e573c063319a55493763728c16d3472436))
* remove debug message ([baa0377](https://github.com/athensresearch/athens/commit/baa037733e59f257b016cb0cfed7f2730bacda04))
* remove debug messages ([480f491](https://github.com/athensresearch/athens/commit/480f4911846d75a136629eb29b32e42ced9837cd))
* remove unused history listener ([276b86b](https://github.com/athensresearch/athens/commit/276b86b233a47f5f511032097adb36c98f3532b8))
* review items ([e621b1f](https://github.com/athensresearch/athens/commit/e621b1f11c2ed2f80cd0cb0e348d07ea7aa02fd6))
* specify highlight color differently to fix inconsistancy ([d18f2d1](https://github.com/athensresearch/athens/commit/d18f2d11767e5ce4e0b17726dcab86a7a0b2c104))
* unify css transitions for smoother theme switch ([ff96b34](https://github.com/athensresearch/athens/commit/ff96b34decd9ba322fe5c777ff369f2aa5204349))
* use :entity/type instead of :block/type ([73994c0](https://github.com/athensresearch/athens/commit/73994c08d0c6dc8c0592260086d9b757c4b291c4))
* use :properties/update-in in tasks ([b67f96f](https://github.com/athensresearch/athens/commit/b67f96f3361f5aa31a85ef687fa0a4e16ea3ce47))
* use reframe not context ([70ad733](https://github.com/athensresearch/athens/commit/70ad73338c1f6bba335aac9cfe3612dbed0d0c9e))
* use reframe not context ([fde0ae8](https://github.com/athensresearch/athens/commit/fde0ae857922ee0f9e0df17dc6bc2913799928e2))
* use standard icons for block element anchors ([0c3e1fe](https://github.com/athensresearch/athens/commit/0c3e1fe3be525791eb27c6067fdf68702046ba7b))

## [2.1.0-beta.2](https://github.com/athensresearch/athens/compare/v2.1.0-beta.1...v2.1.0-beta.2) (2022-07-15)


### Features

* :time/edits supports multiple edits ([e79c003](https://github.com/athensresearch/athens/commit/e79c00385e6a78e859572b2435471725b4b55d1e))
* `block-ref` rendering via protocol ([203f0b6](https://github.com/athensresearch/athens/commit/203f0b67be4a9658d2ff9381e0a595b51a0da656))
* add :properties/update-in event ([45cfaf4](https://github.com/athensresearch/athens/commit/45cfaf4fd307ba179f8888d6e42a18f622a135bd))
* add order/get ([b66bb78](https://github.com/athensresearch/athens/commit/b66bb78bc934ed0c87ec3290d1a2896805608005))
* allow creation of new prop on lookup ([8d38bd5](https://github.com/athensresearch/athens/commit/8d38bd57a5126f79cd0203dc8b1f2821927e82a3))
* backspace at the start of a property will move it to first child ([8871058](https://github.com/athensresearch/athens/commit/88710583ad623f2613ef42d1aaeb4a96d5dc5351))
* Block embeds ported to `BlockTypeProtocol` ([5972d67](https://github.com/athensresearch/athens/commit/5972d6761367bddd7f7c212cc1478b286dc20f91))
* block in reactions components ([83fa29d](https://github.com/athensresearch/athens/commit/83fa29db9af3a32f8f3f19f25c50ce00e3d1d25e))
* block properties ([8393bdb](https://github.com/athensresearch/athens/commit/8393bdbed0cd11f6401baf7f69eb40eceab8553f))
* Communicate supported transclusions and breadcrumbs ([77d8723](https://github.com/athensresearch/athens/commit/77d87232d8b82168f2e0c9f831c0cb91fe31ad47))
* create properties via :: ([0ae7683](https://github.com/athensresearch/athens/commit/0ae7683e0c3068822ab6d95d36ffceeec8ccc372))
* handle enter on props ([3d1fd99](https://github.com/athensresearch/athens/commit/3d1fd99d6a7ac0682e8f95831835cfd2f36210de))
* handle up/down for properties ([eec6161](https://github.com/athensresearch/athens/commit/eec616112eee7edf59274f96cf382c75b5c53355))
* hide comment functionality behind feature flag ([39dc2be](https://github.com/athensresearch/athens/commit/39dc2be76ab3c1642adb6ee851368d00c2de3980))
* hide reactions and cover photo behind feature flags ([d441816](https://github.com/athensresearch/athens/commit/d4418165228172f132a324bb3570dd1b2a4a4224))
* migrate datascript dbs ([b119f33](https://github.com/athensresearch/athens/commit/b119f33415c633828ee01cf19da81ba83733af79))
* migrate datascript v2 times to v3 times ([5441a04](https://github.com/athensresearch/athens/commit/5441a04fbf7a8997dc3b3673fde324e3a78e9abf))
* pages and blocks get full properties reactively ([0a810ff](https://github.com/athensresearch/athens/commit/0a810ff04620a0c7d581e5a04450e2fa024acb43))
* pages have rough header image UX ([e24818e](https://github.com/athensresearch/athens/commit/e24818e9374f2777290cb370afc9b3c6caa554c5))
* Placeholder for reusable editor. ([0431e98](https://github.com/athensresearch/athens/commit/0431e987d00dcc903fb13109282fc6bea81daa2c))
* render properties in outliner ([2192272](https://github.com/athensresearch/athens/commit/21922724f87ee694481b0b9e43881a89c8f13ee8))
* rework anchor functionality ([62c8144](https://github.com/athensresearch/athens/commit/62c8144172a16fd56c623efb252c7a2533ce7b39))
* saving props. ([2eaf93d](https://github.com/athensresearch/athens/commit/2eaf93d2895ba4e3ca986c315d2129768707cba5))
* show all blocks edited on a daily note ([2d772d1](https://github.com/athensresearch/athens/commit/2d772d177918a389ed25e8ac662022c47a8cd538))
* show linked props on page ([c0517e2](https://github.com/athensresearch/athens/commit/c0517e2aa3c9ad2fa9040e12c9d0ba081c05c007))
* show page name for users on other pages, indicate self user ([c211cae](https://github.com/athensresearch/athens/commit/c211caeed39109291bab78fd90711f320b062ed0))
* show properties in breadcrumbs ([18d2842](https://github.com/athensresearch/athens/commit/18d284266c708069da236b61582317b43737c63f))
* support event creation and log time in protocol ([d997730](https://github.com/athensresearch/athens/commit/d9977302faaae6ad86e5c73d042877bb8b86dcf2))
* support feature flags ([3e64560](https://github.com/athensresearch/athens/commit/3e645601236986c8485e2044e3d028eea0408776))
* support indent/unindent in properties ([5afc80d](https://github.com/athensresearch/athens/commit/5afc80ddb000703c7981d2554d22d6246ad0effd))
* support presence-id on events ([3ba9d18](https://github.com/athensresearch/athens/commit/3ba9d18e5d00e82c2e2c43bd735a5c5fad5d9d96))
* time control under feature flag ([36c9538](https://github.com/athensresearch/athens/commit/36c9538727a90dac9d501ffe9b92df619cf8b1c8))
* update datascript schema with first-class time ([c7566a8](https://github.com/athensresearch/athens/commit/c7566a89361eda618f0f49acd8487c95c44532b5))
* use first-class time in atomic resolvers ([770c68c](https://github.com/athensresearch/athens/commit/770c68c10f8cbbc3029e32851527ca63c0d52d18))
* working reactions from block menu ([b418f9d](https://github.com/athensresearch/athens/commit/b418f9d7479d7fc59bf8d7b12fc21c306902bbd0))


### Bug Fixes

* :after/:before prop is not a valid location ([364129d](https://github.com/athensresearch/athens/commit/364129d1d9d2ac6159da42a5783c56765855cdfc))
* add-property-map should recur on children too ([57650a3](https://github.com/athensresearch/athens/commit/57650a395673c474bad493e2563c89cf51d5a50c))
* block children are reactive, again ([8c2124c](https://github.com/athensresearch/athens/commit/8c2124c16de355298a2a09cfadf459635117973a))
* block move should save before moving ([d5b6fdc](https://github.com/athensresearch/athens/commit/d5b6fdc9a13c073847d8def2d59ff340e6704af7))
* children toggle should show/hide props ([05190e3](https://github.com/athensresearch/athens/commit/05190e325f7b2f90017e8d4b5734ff7e53642407))
* clean up reaction props when removing one ([d083dbd](https://github.com/athensresearch/athens/commit/d083dbd4410b22f0a47a50245d635aab6dc025e3))
* code style ([c70a68a](https://github.com/athensresearch/athens/commit/c70a68a38a3e0f3091b437f89a0866714da94979))
* correct reagent args ([ea04181](https://github.com/athensresearch/athens/commit/ea0418100c85a7f4e70b24fa0e5d5c53bba8dfd9))
* create new props that start with colon ([e74babd](https://github.com/athensresearch/athens/commit/e74babd11dea9e9124d70ddfccd7477df287ac80))
* don't create empty prop ([c1e653b](https://github.com/athensresearch/athens/commit/c1e653b823ec65a313ccf8bc167cccb77cd44a0e))
* don't make up time if there's no create time ([3f2155d](https://github.com/athensresearch/athens/commit/3f2155d03e71efa3a33645126ee84d0e5af60797))
* don't show cover image when disabled ([6b9afa2](https://github.com/athensresearch/athens/commit/6b9afa2c341c9b2e02d4593a419179d83616b364))
* don't show placeholder block when there's props ([72850aa](https://github.com/athensresearch/athens/commit/72850aab8b5aa2291f39742fcf8ed2851c571289))
* don't try to create prop if there's an exact match ([075b6ca](https://github.com/athensresearch/athens/commit/075b6ca8bf193ddf876730d3142edff36b21ce86))
* emoji picker popover el needs key ([9b967d5](https://github.com/athensresearch/athens/commit/9b967d5223d4a47ef8afeedb14e7690e48a9273f))
* ignore IDB errors from emoji-picker-element ([255ef6c](https://github.com/athensresearch/athens/commit/255ef6c19caf2fc01c4b88d824a24bbab7011557))
* make keyboard navigation optional ([94da28f](https://github.com/athensresearch/athens/commit/94da28fd1eafb7cca607c31a08c722f90d8cb5a7))
* new-uids-map should work with props ([61c0f80](https://github.com/athensresearch/athens/commit/61c0f8002e5b1ceb6dd13786dfe5a7167e9084e2))
* page delete should delete blocks like block delete ([50c0a07](https://github.com/athensresearch/athens/commit/50c0a07a05ee88b9716b464a74db39e27fd103a1))
* pprint event and explanation on failed resolve ([909d1d8](https://github.com/athensresearch/athens/commit/909d1d87e1b1fc636c0da8484811841390782f6f))
* property-position should support page-id ([accdc99](https://github.com/athensresearch/athens/commit/accdc99ed5881cbe6390b4f497f1c5f6ccb76354))
* remove comments debug spam ([c159a3f](https://github.com/athensresearch/athens/commit/c159a3f8e54f05861cc2b19e6420b26dc20ee4ec))
* remove unused binding ([bc0e57e](https://github.com/athensresearch/athens/commit/bc0e57ea0999e0e3b4c8e22dff6204a9bb2b0b53))
* remove unused fn ([e139200](https://github.com/athensresearch/athens/commit/e13920046ed748e7522589c0b97f4938dfc97a9f))
* remove unused require ([8879b43](https://github.com/athensresearch/athens/commit/8879b43dcd6e0f701970929322d4950308635e11))
* removing a child should remove the order number ([c500a66](https://github.com/athensresearch/athens/commit/c500a66803e214548b4e6fdfce687686a58abe9b))
* restore cover photo and reactions feature flags ([8b08162](https://github.com/athensresearch/athens/commit/8b08162754f6af0d13ef5b3f3d6cc61c6a23c767))
* some react errors that keep showing up ([dff269c](https://github.com/athensresearch/athens/commit/dff269c8a82e0a357bab1583d3d64b73b3d86144))
* support new time format in all pages, etc ([9c8b626](https://github.com/athensresearch/athens/commit/9c8b62667fc550aac8ad28e50e070d1288b1bf1a))
* test and fix get-block-property-document ([3cd0ef5](https://github.com/athensresearch/athens/commit/3cd0ef56c39aebae68df78b07da7a158ff8e5df0))
* use children? binding on toggle ([13c8057](https://github.com/athensresearch/athens/commit/13c8057283a07dc17eedf2b50a4c3cc1b55b1f3a))
* use db/get-block and get-parent should use common-db helpers ([05a29ed](https://github.com/athensresearch/athens/commit/05a29edf078b7324a92adec16dd5661b742c18f6))
* use time and author from block creation ([84fe37a](https://github.com/athensresearch/athens/commit/84fe37a85cae853bf2f2862b47754d9449b7d5e7))
* wrap time in event to allow for author lookup ([ddca8ef](https://github.com/athensresearch/athens/commit/ddca8efa3547b096ee2a1ed1f89e09a1f08d75a3))


### Refactors

* `:caret-position` out of block local `state` ([9708f65](https://github.com/athensresearch/athens/commit/9708f654cb7c62b19a9a7c9c2d7f42b087dc03b4))
* `:inline-ref/states` replaced with re-frame ([8524c61](https://github.com/athensresearch/athens/commit/8524c61879f5d06a45c2d8558480e9e7d7abc22e))
* `:inline-refs/open` replaced with re-frame ([89c2d31](https://github.com/athensresearch/athens/commit/89c2d31477c5ff3eeac54803e3e6b67dc6f7157f))
* `:linked-ref/open` replaced with re-frame ([907786d](https://github.com/athensresearch/athens/commit/907786d75cfff7348c3002f3b5c486aee3696d14))
* `:search/index` migrated to re-frame ([2a4a863](https://github.com/athensresearch/athens/commit/2a4a863184912d5a2e5a97b901031b18dd939632))
* `:search/query` replaced with re-frame and unused state removed. ([23befb7](https://github.com/athensresearch/athens/commit/23befb70db793d7fb070356db68d71e84f522d59))
* `:search/results` moved to re-frame ([5bc2a84](https://github.com/athensresearch/athens/commit/5bc2a846c798a09f7555ed4b060f502017ea9c45))
* `:search/type` replaced with `re-frame` model ([c6a2e18](https://github.com/athensresearch/athens/commit/c6a2e184e46bf2077cca45cd1a302b66d8bff17b))
* add position helpers in new ns ([a587aee](https://github.com/athensresearch/athens/commit/a587aee5ebb320c83e6f5a8359104b0c9e78d5aa))
* add sub for feature flags ([a7b7c6d](https://github.com/athensresearch/athens/commit/a7b7c6d23fc3b70d4a5170d3e576c35932f35372))
* block local `state` cleanup start ([50cdb1f](https://github.com/athensresearch/athens/commit/50cdb1fb5465edfd301e1e0d3f909c4433a956c8))
* cleanup `inline-refs` & `linked-refs` ([4595e8a](https://github.com/athensresearch/athens/commit/4595e8a65d32e4960d276c0faa1c9134ff9c57e4))
* cleanup debugs code ([0c8d213](https://github.com/athensresearch/athens/commit/0c8d213ee2113ed6ca989ad8c91e3b76ce041517))
* dragging support out of local block state. ([9d75481](https://github.com/athensresearch/athens/commit/9d754817591b71f798982c77239038561ce59381))
* keyboard interactions in editor w/o block on a graph. ([ce6d38c](https://github.com/athensresearch/athens/commit/ce6d38c26e451e81499f62fcab4c5818fa1953e7))
* localized `last-event` state ([7aa0a58](https://github.com/athensresearch/athens/commit/7aa0a58c2c4ed52e82219289e02514411ead2b68))
* log level and binding name ([ec7e24a](https://github.com/athensresearch/athens/commit/ec7e24ac3c510639186b715de747521ad88b50c7))
* make `:block/uid` fn arg, don't need `state` for it. ([d8b6daf](https://github.com/athensresearch/athens/commit/d8b6dafd695c27cf6c027553f45adb2484f211ca))
* make migration runner generic and cljc ([829b078](https://github.com/athensresearch/athens/commit/829b0781b1acc3075f0c13a1c328348271eb9dde))
* move descendants fns to common-db ([b608fb2](https://github.com/athensresearch/athens/commit/b608fb275edbe622d5b2b563d2f28263461c4579))
* move event-tx resolution into own fn ([7b242b7](https://github.com/athensresearch/athens/commit/7b242b7e32ab5d732c54d482f776929e8c56e82b))
* move property sorting into own fn ([6a9248f](https://github.com/athensresearch/athens/commit/6a9248f1eb1d3a357df98bc2d17273e0da14b9c3))
* move reactions up to default renderer ([c22e087](https://github.com/athensresearch/athens/commit/c22e0873f86c88753705975dbe56f10c8fe1a008))
* paste verbatim by default as configuration option to editor. ([b505129](https://github.com/athensresearch/athens/commit/b5051295f3fd3cdf7a8ded06db21a6d6be70d3a3))
* remove changes to unrelated files ([5fe4063](https://github.com/athensresearch/athens/commit/5fe4063ebc7ee8db2142dd416fadb93d2c012b93))
* remove duplicated prev-block-uid code ([10f96b1](https://github.com/athensresearch/athens/commit/10f96b1f45e6edf94501c21b6cd7bfbb4d8a2ac9))
* remove unused rules ([2482da4](https://github.com/athensresearch/athens/commit/2482da4c993e09cfa38ba7612d9548e959edfbce))
* removed `:last-keydown` from block local `state`. ([ec0e19a](https://github.com/athensresearch/athens/commit/ec0e19ae3da453ddaa730821204eee4f31870dc0))
* Removed `:show-editable-dom` from block `state` map. ([c8ea851](https://github.com/athensresearch/athens/commit/c8ea8512b664f317fe48eba047e4ffa32d1caed9))
* Removed `:string/local` & `:string/idle-fn` from `state`. ([3866e49](https://github.com/athensresearch/athens/commit/3866e49352db605ecace2f626a7792451f79e97e))
* removed `:string/previous` from block `state` map. ([b9235b1](https://github.com/athensresearch/athens/commit/b9235b139b9db0fc35e109db52bd82e212a65581))
* removed `embed-id` from `BlockTypeProtocol` ([5fa4a35](https://github.com/athensresearch/athens/commit/5fa4a35744e9db1d621d220aaadd78ab05ac430c))
* removed unused code ([4729b91](https://github.com/athensresearch/athens/commit/4729b91846473e6163172fdd59863410703e1d5f))
* resolve-transact! passes event time to resolver ([eed73bf](https://github.com/athensresearch/athens/commit/eed73bfa96c4586fdf193fcd3071e476ed9dc81e))
* take one of editor component ([a8c0944](https://github.com/athensresearch/athens/commit/a8c0944361e317e895a385e4e1ea33c78447b63d))
* we have editor with chrome and editor with search & slash. ([8e9ff19](https://github.com/athensresearch/athens/commit/8e9ff197beb66e8783b0a758f02f4986c61e341a))


* add deep remove prop test ([80d555b](https://github.com/athensresearch/athens/commit/80d555be6385f5f55cded20d9baea58a27eeb0c0))
* bust nodes cache ([aeef8ca](https://github.com/athensresearch/athens/commit/aeef8cafe306f250c74a4d3b929bb30e853b5c9e))
* disable e2e on CI ([afc9bff](https://github.com/athensresearch/athens/commit/afc9bff70c85dd8df68d08de3ac8db61b36f3b1d))
* file cleanup ([d7ab5a5](https://github.com/athensresearch/athens/commit/d7ab5a59e1fbb4301d5145f1d464126975a803d6))
* fix ([9f44475](https://github.com/athensresearch/athens/commit/9f44475aa7da140997c3da6cdf1932e24edad031))
* fix e2e test, removed modifications done only for testing ([cf9df07](https://github.com/athensresearch/athens/commit/cf9df07533b1f2fc7f291add5931ac5a101dc26d))
* fix lint errors ([4b1cd2b](https://github.com/athensresearch/athens/commit/4b1cd2b5c1476139bf8d0cfd16a2e9b988d4bc11))
* get-block-property-document now returns create and edit data ([9f5dbf5](https://github.com/athensresearch/athens/commit/9f5dbf5e2d2120c6173e494f0652ca86bddbb5ce))
* ignore redef errors ([7c2bce9](https://github.com/athensresearch/athens/commit/7c2bce9d60d6accc723298d5bfde4d9a41c1774d))
* prerelease comparison should be to a string ([f8a6bf0](https://github.com/athensresearch/athens/commit/f8a6bf06ed12dc4f9b0f5d158669b1c4c9aa68c6))
* remove unused types ([c5586a7](https://github.com/athensresearch/athens/commit/c5586a7126a53ef9253a876f69cd270b688e17de))
* run style:fix and carve:interactive ([1dedb60](https://github.com/athensresearch/athens/commit/1dedb60556ff0e51f5312aed73775a3edccb9cab))
* saving progress ([fd02a1d](https://github.com/athensresearch/athens/commit/fd02a1d5e4897393d1266a61ae355e1ef5a4265b))
* style:fix ([fcef334](https://github.com/athensresearch/athens/commit/fcef3341154f81dfd287b87c34221ff825ed4ce4))
* update shadow-cljs and cljs ([00bf824](https://github.com/athensresearch/athens/commit/00bf824465225dc0e1f688f6cdeee56bfe182ce2))
* update shadow-cljs and highlight.js ([5c66731](https://github.com/athensresearch/athens/commit/5c6673109d8f69562579ba4a08cc8677ce204c21))
* update to clojurescript 1.11.51 ([5b4073f](https://github.com/athensresearch/athens/commit/5b4073f869d75bd4417eca10cd3547b1b367adbd))
* use java 17 ([df3be8c](https://github.com/athensresearch/athens/commit/df3be8c66d85d32e7bfb0a00af0f68a2b1ac87de))

## [2.1.0-beta.1](https://github.com/athensresearch/athens/compare/v2.0.0-beta.37...v2.1.0-beta.1) (2022-06-16)


### Features

* add context menu hook ([5c0b549](https://github.com/athensresearch/athens/commit/5c0b549e6ec49d7f6548f70c55a0b13b374d9a85))
* anchor uses new context menu hook ([8c5423e](https://github.com/athensresearch/athens/commit/8c5423effb31d3708ce0bd1173c23001257f4300))
* context menu also works on block container ([7c16441](https://github.com/athensresearch/athens/commit/7c164417b6e579936784c085bea8e58302fd0c4c))


### Bug Fixes

* defaultIsOpen state for linked refs ([59fbb60](https://github.com/athensresearch/athens/commit/59fbb6088471eda9f5915cd2729fdfd5c4c2fc03))


### Enhancements

* if a block open in right sidebar is not in main view, ([0ef8836](https://github.com/athensresearch/athens/commit/0ef8836f57e052b9e1be1c7f2292911e0b0b4290))


### Refactors

* **block:** use new contextmenu for blocks ([66a9d33](https://github.com/athensresearch/athens/commit/66a9d339b1701853885e403dde6edd0e536f46ca))


### Documentation

* add versioning ADR ([1c75e5f](https://github.com/athensresearch/athens/commit/1c75e5f53d266597baf409311092567a0710be2a))


* bump to 2.1.0 range ([0b081e8](https://github.com/athensresearch/athens/commit/0b081e89e65c033c0332a2c9a82bb1016494876f))
* deploy prerelease to beta domain ([f348a99](https://github.com/athensresearch/athens/commit/f348a99498411a09c70148a37885af74159fbbde))
* docstrings ([ffca46a](https://github.com/athensresearch/athens/commit/ffca46aa42e9ec38a1f7df53b0ff2bc946a132ac))
* docstrings ([15176d9](https://github.com/athensresearch/athens/commit/15176d93909e0c477873609b5bcf67beff8bdc66))
* don't build macos in parallel ([6d987e4](https://github.com/athensresearch/athens/commit/6d987e4b7977da3fe81a867b67b55cb0dbc72dab))
* fix ([492c362](https://github.com/athensresearch/athens/commit/492c362e00e226ea5ea027e5fbb96524c4e7beb3))
* re-enable auto updates for electron ([ee29f33](https://github.com/athensresearch/athens/commit/ee29f3325f09801acd9b9cae3fb4af3cea6d5d4c))

## [2.0.0-beta.37](https://github.com/athensresearch/athens/compare/v2.0.0-beta.36...v2.0.0-beta.37) (2022-05-27)


### Features

* remove safari unsupported warning ([e39d530](https://github.com/athensresearch/athens/commit/e39d530f561172591af4c02d80d29a123a12c6fc))


### Bug Fixes

* athena should highlight results ([ea5abe5](https://github.com/athensresearch/athens/commit/ea5abe5d6bdeb77b5849645f3eb32962305e9a9a))


### Refactors

* don't use lookbehind in replace-roam-date ([3f4066a](https://github.com/athensresearch/athens/commit/3f4066a0029e995b7c7a8c293b5a8b3f2be17755))
* move regex fns into athens.patterns cljc ([ded5f2a](https://github.com/athensresearch/athens/commit/ded5f2aafe0a46ae3aaec05e6ad044b1a6d0a9d9))
* refactor highlight to not use lookbehind ([c4d386c](https://github.com/athensresearch/athens/commit/c4d386cff7d11f741b64cab2da28f48ad95fb3c4))
* refactor unlinked to not use lookbehind ([870e39e](https://github.com/athensresearch/athens/commit/870e39ef8bed67bfe22c1a76da5cc58d59c8c6de))
* remove lookbehind from instaparse ([c8bfa5b](https://github.com/athensresearch/athens/commit/c8bfa5bc47303a545b8a58ba3ce8004fe976eb33))
* remove unused backtick token ([d3e1877](https://github.com/athensresearch/athens/commit/d3e18775d594ccff3ea965a33be0af5742d47eea))
* use same file for clj and cljs parser tests ([2575222](https://github.com/athensresearch/athens/commit/2575222d2e178885d3221ad0083380037439826a))
* use word boundary instead of positive lookbehind in parser ([6a4f3cd](https://github.com/athensresearch/athens/commit/6a4f3cd971b0bc1f36834ef6e64a16aa6fc5aad2))


* add boundary tests ([4e66a13](https://github.com/athensresearch/athens/commit/4e66a1306df013595a3d3d8f32462f45fb9f38fb))
* add hashtag test for unlinked ([6405bdc](https://github.com/athensresearch/athens/commit/6405bdc582ac355e7f63561cc2f5c2d24ca89709))
* add tests for athens.patterns ([bab8d71](https://github.com/athensresearch/athens/commit/bab8d71ad62abaa9b78c77027b7098355862b653))
* disable failing lookbehind tests ([6a406a0](https://github.com/athensresearch/athens/commit/6a406a0b8e629735417ebcaf01e0886944eeb0ff))
* more tests for roam-date ([7ea9d3f](https://github.com/athensresearch/athens/commit/7ea9d3f9c107274aee12fa13d41f8a454056575f))
* remove unused vars ([e14e53d](https://github.com/athensresearch/athens/commit/e14e53da0afc7ea0a1f76e740a6e32006cfa8495))
* update backslash escapes test for cljs output ([be1e5df](https://github.com/athensresearch/athens/commit/be1e5dfa13ad8677076e74367e522e105df4dae5))

## [2.0.0-beta.36](https://github.com/athensresearch/athens/compare/v2.0.0-beta.35...v2.0.0-beta.36) (2022-05-24)


### Features

* serve web client from athens server ([775c981](https://github.com/athensresearch/athens/commit/775c98102bf4797d8111015b54a7885e51eee44a))


* release web to vercel ([e52f6e2](https://github.com/athensresearch/athens/commit/e52f6e258919e0eba066192520746174e2d77c88))
* release-server now requires build-app ([1b3ef1d](https://github.com/athensresearch/athens/commit/1b3ef1d1b3236705237bd5cca7e560144694d57b))

## [2.0.0-beta.35](https://github.com/athensresearch/athens/compare/v2.0.0-beta.34...v2.0.0-beta.35) (2022-05-18)


### Features

* permalink includes graph name and password ([5424b05](https://github.com/athensresearch/athens/commit/5424b0558b73aea7a0d67d8448e7844e914ffa48))


### Bug Fixes

* don't control dialog inputs ([7c59bbd](https://github.com/athensresearch/athens/commit/7c59bbdfed4c28910b449a787e758590ae4326fe))
* don't show plaintext password on permalink ([6351d1d](https://github.com/athensresearch/athens/commit/6351d1d53c7ff10b94eabf5e7665fd6280ae14bb))
* show own user on presence if there are no other users ([de5aead](https://github.com/athensresearch/athens/commit/de5aeada02f0f11eb178914d5b3d5d29ea772b85))

## [2.0.0-beta.34](https://github.com/athensresearch/athens/compare/v2.0.0-beta.33...v2.0.0-beta.34) (2022-05-04)


### Features

* add navigation section to help ([590a275](https://github.com/athensresearch/athens/commit/590a275c50d3acca47e03b0d6f8a9bd6e22d49e8))
* mod+alt+o zooms out of current block ([e446f0c](https://github.com/athensresearch/athens/commit/e446f0c1517476ce350cb2c10418b8226341d55f))
* Permalink creates db if needed, and works on electron ([#2175](https://github.com/athensresearch/athens/issues/2175)) ([13b5241](https://github.com/athensresearch/athens/commit/13b5241d10082dc68fb79c28d10194477f853cd2))
* pressing up/down with no focus takes you to last/first block ([c6f7806](https://github.com/athensresearch/athens/commit/c6f7806e6f3847ab45cd740a5f42178ebb8ec1fc))


### Bug Fixes

* :daily-notes/items should always be a vector ([d06a359](https://github.com/athensresearch/athens/commit/d06a359f841dac83e0e1355aa12f4dea99a3f26b))
* `:page/rename` & `:page/merge` w/o regex injection. ([4a817bb](https://github.com/athensresearch/athens/commit/4a817bb01b13dc44b8433206b6f7593056ff3fff))
* also handle naked hashtag in nested page renames ([206d34c](https://github.com/athensresearch/athens/commit/206d34c9ccbc861684b0cc0e8b2682fd505c1757))
* disable "open file..." dialog on cmd/ctrl+o ([18a818a](https://github.com/athensresearch/athens/commit/18a818a083c9f18cdc2aa57b6a062cb573591155))
* fold shortcut should use shortcut key on mac ([79f7ad7](https://github.com/athensresearch/athens/commit/79f7ad701bfbd77b1a61efb3063a3c6ad53ea828))
* page linked refs start closed when 10+ ([ce77fa9](https://github.com/athensresearch/athens/commit/ce77fa96cc7e48d8c444fa002a50c6293599b072))
* prev-block-uid should not try to go to pages ([1ede064](https://github.com/athensresearch/athens/commit/1ede0641b30e6f24425348302390ccacf7f56d85))
* prevent browser defaults that focus URL ([c94b162](https://github.com/athensresearch/athens/commit/c94b16229a76c1f36866aa18cc1fd9c59e7334ee))
* restore the alert/js event ([f34b69c](https://github.com/athensresearch/athens/commit/f34b69cd9edc8bde2158650e209956cb4286a296))
* unfold block is mod+down ([e42c5c7](https://github.com/athensresearch/athens/commit/e42c5c7a83977314e75be0b73569de7d2f58e7f8))
* up on first window child should not lose block focus ([805065e](https://github.com/athensresearch/athens/commit/805065e301701a0485e85b496da9ec03ffc51044))


### Refactors

* also not used anymore ([c2c9ae7](https://github.com/athensresearch/athens/commit/c2c9ae76d9e65f8e67212e45b8f68526a4560c67))
* remove devtool ([b24bad6](https://github.com/athensresearch/athens/commit/b24bad69ca92596a58bec4bb479c740c7943d932))
* removed now dead `patterns/linked` ([0dfe2d0](https://github.com/athensresearch/athens/commit/0dfe2d0dbf3b193de89123b0df37239bf24e8820))

## [2.0.0-beta.33](https://github.com/athensresearch/athens/compare/v2.0.0-beta.32...v2.0.0-beta.33) (2022-04-27)


### Bug Fixes

* row title is addressed by full name ([70ac275](https://github.com/athensresearch/athens/commit/70ac2758d44829bec57a4f4be977f7c5568bb806))

## [2.0.0-beta.32](https://github.com/athensresearch/athens/compare/v2.0.0-beta.31...v2.0.0-beta.32) (2022-04-27)


### Features

* initial virtualizing ([b0e4380](https://github.com/athensresearch/athens/commit/b0e4380fe2660d534153f9738fa73199b9ea1ce7))
* sortable styled table ([0141331](https://github.com/athensresearch/athens/commit/0141331e577fefc04b905259b4ba5f189cb2f50a))


### Bug Fixes

* can merge pages again ([e52234c](https://github.com/athensresearch/athens/commit/e52234cabea30e6d99ee49c9a56d5650f7ada8a9))
* constrain all-pages width ([b397133](https://github.com/athensresearch/athens/commit/b39713384453b89cce75a44df39822a3f6d79f96))
* copy on embed blocks ([3a891fe](https://github.com/athensresearch/athens/commit/3a891fe09d0b3c641e66f710fb9d5e73996e345b))
* disambiguate edit and create time ([5c06600](https://github.com/athensresearch/athens/commit/5c06600a1268478cab14a572e02d1c21c0cbec70))
* properly format and display dates in table ([8467018](https://github.com/athensresearch/athens/commit/8467018185267c2d2f325d1d7101aa5dc07c0104))
* table works for many and few pages ([7e577fb](https://github.com/athensresearch/athens/commit/7e577fb6968c81be6cd357955a4023d0c3a47c88))
* working confirmation dialog for page merge ([8e67abe](https://github.com/athensresearch/athens/commit/8e67abebacf2f13678a5ef476024252fefb50003))


* cleanup ([dcfd5aa](https://github.com/athensresearch/athens/commit/dcfd5aab2cd2032cf6ff8b7f5a26fcd971b5f4a7))
* fix ([48df468](https://github.com/athensresearch/athens/commit/48df46827972236e8f0f98a30e51fe68835be9d8))


### Refactors

* add new colorscheme for subtle and highlight buttons ([4e3a191](https://github.com/athensresearch/athens/commit/4e3a191d11ba8b3f956b3f78d41b28f138524c62))
* cleaner style application in all-pages table ([91e3b2d](https://github.com/athensresearch/athens/commit/91e3b2df8915b764ae77ade612f6369a637bdbcb))


### Documentation

* update readme ([9f545ef](https://github.com/athensresearch/athens/commit/9f545ef45685a08688a2ad1c3c150b2ae6a7b55f))

## [2.0.0-beta.31](https://github.com/athensresearch/athens/compare/v2.0.0-beta.30...v2.0.0-beta.31) (2022-04-20)


### Features

* don't autoblock non-chrome browsers ([81324e0](https://github.com/athensresearch/athens/commit/81324e050c372119711464c44be9d3212fe05a2b))
* Navigate back when user deletes current page ([bbdafc4](https://github.com/athensresearch/athens/commit/bbdafc44e7a454fcc3d7d69bf3ea5bbd41713ea8))


### Bug Fixes

* can click toolbar buttons ([93accb3](https://github.com/athensresearch/athens/commit/93accb38ee4611eb991b1bda94bf88230b2b513e))
* consider unknown OS to be linux ([449653f](https://github.com/athensresearch/athens/commit/449653fb9b97c5d43e1c94094bfd987a7fe8511f))
* safari user agent is lower case ([b0bff69](https://github.com/athensresearch/athens/commit/b0bff693bd5765a965523bd3c38d3ad9572a0d11))
* show unsupported message for safari only ([ee33753](https://github.com/athensresearch/athens/commit/ee33753446ecef06a00dfd347b330b27f822c8e9)), closes [/github.com/athensresearch/athens/pull/2096#issuecomment-1083101498](https://github.com/athensresearch//github.com/athensresearch/athens/pull/2096/issues/issuecomment-1083101498)
* still support chrome ([7cf620a](https://github.com/athensresearch/athens/commit/7cf620a8ea0d25e2900aae90a6625c872ae5d4c4))


* update shadow-cljs, cljs, tick ([0dfcd36](https://github.com/athensresearch/athens/commit/0dfcd36bf43910dfb846bf8e8d2a9b6315d1801a))

## [2.0.0-beta.30](https://github.com/athensresearch/athens/compare/v2.0.0-beta.29...v2.0.0-beta.30) (2022-04-19)


### Features

* add new edit icons ([bb0f40d](https://github.com/athensresearch/athens/commit/bb0f40de9db77a167284859317f402476fe88bca))
* add working 404 page ([21012be](https://github.com/athensresearch/athens/commit/21012be5e0b6b21b5a750a22d7d17fee30ccfcea))
* can open in sidebar from inline ref breadcrumb ([fd4f56c](https://github.com/athensresearch/athens/commit/fd4f56cf12bc6e2674f18690cdf2ac1b13f1542f))
* JVM Crash Reporting ([226c793](https://github.com/athensresearch/athens/commit/226c79374a8fe6a0081f6d487b6566b049eb964d))
* new page header controls ([01724ee](https://github.com/athensresearch/athens/commit/01724ee1be050da21fe9e67db0f5e50e53b81768))
* separate button for open in sidebar ([c35c78c](https://github.com/athensresearch/athens/commit/c35c78c5ea6655202c50e822fae289adb813a29d))
* separate button for open in sidebar ([bf694f1](https://github.com/athensresearch/athens/commit/bf694f1f9564ee4869503de6120bb5b14a02054e))
* show "open in main view" for daily notes ([2f99633](https://github.com/athensresearch/athens/commit/2f9963350f6c3bbb28fdcca7495466940107dc78))
* use new edit icon ([51fcd03](https://github.com/athensresearch/athens/commit/51fcd03fe0c1ff09467af54f120e9e08d380318d))


### Bug Fixes

* 404 shows properly on page-by-title ([8a35ace](https://github.com/athensresearch/athens/commit/8a35ace7528b5b155d75d6f0de4be0dd980a388e))
* add person icon ([35aa390](https://github.com/athensresearch/athens/commit/35aa39036485fe7889fc247716493eb20f4b2564))
* all pages table wraps and stretches ([7463254](https://github.com/athensresearch/athens/commit/7463254fb272ace20e5429881540a765ddd4679d))
* also identify page by uid ([f8d1148](https://github.com/athensresearch/athens/commit/f8d11488757e6503f05a98b15e288f950161a7e4))
* arrow up and down from blocks works ([ba8d3cb](https://github.com/athensresearch/athens/commit/ba8d3cb63fa5fdf1a32225f261285ffef3def0e0))
* block embed controls properly placed ([2f6bd9e](https://github.com/athensresearch/athens/commit/2f6bd9e74824d6312df96d89ddf426d38740c93d))
* block toggle and anchor properly sized ([c15679e](https://github.com/athensresearch/athens/commit/c15679ef2f28259e3eec36cfbc501592d4c36b72))
* breadcrumbs allowed to be big again, and checkboxes not broken ([780876d](https://github.com/athensresearch/athens/commit/780876df33e2efb36be1516ce411cfbc3141ee9e))
* breadcrumbs should wrap ([0266059](https://github.com/athensresearch/athens/commit/02660594dd03f791a1e789904c716e0bdb51a686))
* can click sidebar to scroll ([2574d34](https://github.com/athensresearch/athens/commit/2574d34a8a497b8496f2a337d85858cd5ac62678))
* can copy multiple block refs ([e151792](https://github.com/athensresearch/athens/commit/e15179292f33a252d982d10a5cbfc093f6ce0b6a))
* can drag-select in sidebar ([0c5a4a4](https://github.com/athensresearch/athens/commit/0c5a4a4c171dcbb11fb55f0f789e0fd85946b4ad))
* can scroll to items in sidebar again ([5284874](https://github.com/athensresearch/athens/commit/528487497663689b2f892a06be6a761b3f11b184))
* centered daily notes ([653ccc8](https://github.com/athensresearch/athens/commit/653ccc86b70751fabf3408c87d7f22cec5b19d8f))
* consistent page widths ([a82f418](https://github.com/athensresearch/athens/commit/a82f418fbbc9a2f64173716f6e11886f865332c8))
* correct chevron direction in references ([b9b92d6](https://github.com/athensresearch/athens/commit/b9b92d617005627d527069ec780ad2a2576e1754))
* correct transition on apptoolbar ([9fe3982](https://github.com/athensresearch/athens/commit/9fe39825a45c335c9a81a153fea7bebf08d28267))
* css prop should go in sx ([6426355](https://github.com/athensresearch/athens/commit/6426355a5d745b15940cd057d05d8cbbd37dbce9))
* don't add new pages to daily pages ([67745a4](https://github.com/athensresearch/athens/commit/67745a43705c33768e40b6829e3c9a244820722c))
* don't import athens.utils in electron ([c8578ce](https://github.com/athensresearch/athens/commit/c8578ce577ed205240ed39824266620e60a45992))
* embeds only as broken as on release ([ae5dbae](https://github.com/athensresearch/athens/commit/ae5dbae1758d39ae348f7bb1a8df04818b5d6e26))
* flip page open buttons for daily pages ([74cadcc](https://github.com/athensresearch/athens/commit/74cadccc0016bebd915c335166e954d7b5942944))
* force consistent typography on block text and textarea ([e432025](https://github.com/athensresearch/athens/commit/e432025b17b9442aee415043f44557cdac09f2c2))
* inline menu closes on click outside ([870250b](https://github.com/athensresearch/athens/commit/870250b858f1f718cf63ca2e3a8c59d781879d23))
* linked refs in block work ([e6707fb](https://github.com/athensresearch/athens/commit/e6707fbb2e75b7c59784066e05ef3ba854af4616))
* mark/highlight colors are global ([81750cd](https://github.com/athensresearch/athens/commit/81750cd6bd08b94537a0b383a6a8f8df33071738))
* misc minor layout issues ([9bdf174](https://github.com/athensresearch/athens/commit/9bdf1749cdd6c4535341dc90ee9eddd4ecea5079))
* more types of links included in block interaction passthrough ([84522d9](https://github.com/athensresearch/athens/commit/84522d9e12e55da941e71b76d79bb7e6ef323b4e))
* nested links clickable again ([270c05a](https://github.com/athensresearch/athens/commit/270c05a3c3d741bed35f4fe655ebcbde94285f96))
* no nil uids on daily pages ([7cecc4f](https://github.com/athensresearch/athens/commit/7cecc4febf146190bf4cc1d7409e17361992b347))
* node page button shouldn't squish ([eb02b0a](https://github.com/athensresearch/athens/commit/eb02b0a024677e598644d2553f969a4d97e4ca77))
* open page in sidebar shouldn't open graph ([6afcc68](https://github.com/athensresearch/athens/commit/6afcc682f051ed8a7e1d0a26ddd4ede11ff0febb))
* pass more tests ([49bb181](https://github.com/athensresearch/athens/commit/49bb1813421cea53131863bf09e089d8bd3a46e6))
* passing more tests ([5bdede1](https://github.com/athensresearch/athens/commit/5bdede134f376eab564d3d3acac248b032699b71))
* perf and key error on daily notes ([c31cbfc](https://github.com/athensresearch/athens/commit/c31cbfc75193e1149943853e64991db98652b3c5))
* proper icon size in table header ([1bb3350](https://github.com/athensresearch/athens/commit/1bb3350bd5d6f110e71353236952ceecbccbc903))
* proper indentation for blocks in embeds ([8bfff9d](https://github.com/athensresearch/athens/commit/8bfff9df339d7bc1a9adb502862c76fbad25a721))
* proper toast message on permalink action ([a68de83](https://github.com/athensresearch/athens/commit/a68de8397c8b42f2f2999ce255ca86046ea0d93f))
* reduce excess spacing around daily notes ([0e02491](https://github.com/athensresearch/athens/commit/0e02491ff14b51b354304aed96e66ade339a31aa))
* remove :first-child warning ([fedbec0](https://github.com/athensresearch/athens/commit/fedbec025eee7972d2b1e78eb5213fddd3d96f59))
* remove content area until editing ([c3ec4a9](https://github.com/athensresearch/athens/commit/c3ec4a94db04da35c3cdd1cd01f546ec2c63f215))
* remove some println ([04dd227](https://github.com/athensresearch/athens/commit/04dd227d2e37d2e11b97623f669f5f46b997094e))
* sidebar items state in db ([39f8365](https://github.com/athensresearch/athens/commit/39f8365339f4329323d5543b5415ee44aec961a0))
* solve another cause of collapsed checkboxes ([4502d52](https://github.com/athensresearch/athens/commit/4502d52dabda8becf74b65a2903bbe99d6b30509))
* solve cause of extra title wrapping ([0ce0063](https://github.com/athensresearch/athens/commit/0ce00632f81ccb7493e17d83a70e2c3f2cf3e1af))
* solve some cases of improper buttons in page header ([b2e350b](https://github.com/athensresearch/athens/commit/b2e350bdf912e51753a288b4eed5bf32068e2012))
* throttle dispatches on-scroll ([f560575](https://github.com/athensresearch/athens/commit/f560575bcbbd8c7d646c09abb31968d748bcfd8f))
* use a debounce instead of throttle for scroll ([e16f5c4](https://github.com/athensresearch/athens/commit/e16f5c465aca6b029b238e94f0a27f4489a15531))
* use correct property for menu title spcing ([c716d37](https://github.com/athensresearch/athens/commit/c716d37fa5d93f0d7874dec98f82c51760844fd1))
* use correct property for menu title spcing ([0ece2ec](https://github.com/athensresearch/athens/commit/0ece2ec816cb3663412bb80ccb4f629b1db8952a))
* use theme on loading screen ([f57a3c4](https://github.com/athensresearch/athens/commit/f57a3c4a10ede0b4f32860f9ac9ec8fc670c0c56))


### Performance

* early returns for menus in blocks ([38bda25](https://github.com/athensresearch/athens/commit/38bda25ab291c8298b59a42217bd7125daaee0fd))
* only include autocompletes on active block ([ee29db5](https://github.com/athensresearch/athens/commit/ee29db51fab5d20d024fb7cb751accf05524f882))
* remove unused css ([506ff8c](https://github.com/athensresearch/athens/commit/506ff8c611d37d410042cf91b6f6bccc1da72899))
* use system fonts for speed ([e4c26eb](https://github.com/athensresearch/athens/commit/e4c26eb5b8795c9df3bd287ac35abcddd72e91f6))


### Refactors

* blocks use new inline refs component ([5c238ab](https://github.com/athensresearch/athens/commit/5c238ab6da968b8ad5c9ab02456306879f63b42c))
* chakra window icons ([8ab8636](https://github.com/athensresearch/athens/commit/8ab86367b581890bfc816cb7c00a3c4b73de5eb9))
* dedicated space for presence on blocks ([3348dc5](https://github.com/athensresearch/athens/commit/3348dc5e8bd95cf212f7e2d28ea672f52be732d1))
* functioning block menu ([a57e7d5](https://github.com/athensresearch/athens/commit/a57e7d5d133148b1f46285502751eb094118ea73))
* make sure every menu and popover is lazy ([b281dda](https://github.com/athensresearch/athens/commit/b281dda46598ab03744372cdcd710bed7b647847))
* midflight reducing getcaretpos ([6a28dcc](https://github.com/athensresearch/athens/commit/6a28dccd86d0ab746d17efb5c5f9defa37310438))
* minor cleanup ([4f42fe4](https://github.com/athensresearch/athens/commit/4f42fe48a440fcca58c0f14d8c75032c14118bbb))
* move block components out of redundant comps folder ([f049f60](https://github.com/athensresearch/athens/commit/f049f607b30d1be14e3d8ea86af9d435c0ab078d))
* move reference components to new file ([79502a7](https://github.com/athensresearch/athens/commit/79502a72394b5c5b45f0134ed82336b3510c3c2f))
* new file for sidebar components ([c1686b1](https://github.com/athensresearch/athens/commit/c1686b16c0b4acb6740c155acb54fe65ee751752))
* no mui in graph ([2fd482b](https://github.com/athensresearch/athens/commit/2fd482b7a071a92f1881e348c18d0410f798dd74))
* only one title component ([e37150e](https://github.com/athensresearch/athens/commit/e37150e391b0a8640dd0654b451fab5d6f25c8ad))
* replace and remove material-ui ([95dc4bb](https://github.com/athensresearch/athens/commit/95dc4bb75a0d66ace8f9983d9ae099149fca8e43))
* replace material icons on node page ([cc1bfd5](https://github.com/athensresearch/athens/commit/cc1bfd578417d01f7d2c353ff172c03f157066b4))
* retire stylefy and garden ([2184c10](https://github.com/athensresearch/athens/commit/2184c10cdafe26350a9d89bf5f1f5edf7a61b223))
* rework node page titles ([82e1e97](https://github.com/athensresearch/athens/commit/82e1e971a3b1918f1ef21fb1b5a4e150fa8ccef1))
* reworking linked ref styles ([84b0ff0](https://github.com/athensresearch/athens/commit/84b0ff0b6f1d09d5c98626d5edcaa8fcb5fa3211))
* undo class changes to textarea ([1e7c2b4](https://github.com/athensresearch/athens/commit/1e7c2b446edf217cb5716cac883e9ccd7f0d33f8))
* use correct block container styles ([1cd833f](https://github.com/athensresearch/athens/commit/1cd833f98597e915d441391aa78cd7aefa938f3b))
* use correct block container styles ([20e1cbe](https://github.com/athensresearch/athens/commit/20e1cbe127374516f8e53e94372adb652254882c))


* cleanup ([9fedb20](https://github.com/athensresearch/athens/commit/9fedb20391b2b2864864ad3ae14b1bd8ee7dfffb))
* fix lint issues ([059d421](https://github.com/athensresearch/athens/commit/059d421ae7677df6d21e68044a6e94d71d836562))
* fix lint issues ([e880ef5](https://github.com/athensresearch/athens/commit/e880ef5b7fbe710cc0f9369292f0055d8edb4b2e))
* happy little comment around fragile styles ([4f4cb42](https://github.com/athensresearch/athens/commit/4f4cb421b0825566d324da70278f5ab2dae3a095))
* lint fixes ([259622a](https://github.com/athensresearch/athens/commit/259622af6ad83042b0c0b1e65373bc4fd7597d19))
* lint fixes ([43b2b0b](https://github.com/athensresearch/athens/commit/43b2b0b34ad4f6591b01902d6ae92f343fd9e56d))
* lint fixes ([0be358d](https://github.com/athensresearch/athens/commit/0be358d38c15b448d507bdb7390a6b2936080641))
* lint fixes ([fef4597](https://github.com/athensresearch/athens/commit/fef459763f468cdf341007e736de0ca6c44343c5))
* linting ([7bc4e09](https://github.com/athensresearch/athens/commit/7bc4e099b6a1574fe7d00081274edd9c0e8aa200))
* linting ([c941068](https://github.com/athensresearch/athens/commit/c9410683d96292f1579ee69ea805a0051568e0f2))
* linting fixes ([6dc3771](https://github.com/athensresearch/athens/commit/6dc37710a98c4b6b2f000965799044a2fcfb38a6))
* minor cleanup ([b5d4197](https://github.com/athensresearch/athens/commit/b5d41970fd8561aec73dea4991a13d18a7cd96a2))
* remove console logs ([41f107d](https://github.com/athensresearch/athens/commit/41f107dee03cce831ab442372b36f6f634d6fcce))
* remove outdated comment ([e4fca23](https://github.com/athensresearch/athens/commit/e4fca23e48145df8de185f0342c471ba672d5131))
* remove unused bindin ([f93e275](https://github.com/athensresearch/athens/commit/f93e275c547b7362970138ba44f98af260f33a6b))
* update e2e page selectors ([6dc8d07](https://github.com/athensresearch/athens/commit/6dc8d07a4f1459d58eba7ad868f1c5de6364113d))
* update e2e to new copy ref text ([558af98](https://github.com/athensresearch/athens/commit/558af98fa0f3c34ec952cc11e40739636797fea5))

## [2.0.0-beta.29](https://github.com/athensresearch/athens/compare/v2.0.0-beta.28...v2.0.0-beta.29) (2022-04-13)


### Refactors

* don't use where-triple filter if there's no since-order ([3c96236](https://github.com/athensresearch/athens/commit/3c9623611889f90e3533596edce31af20deabaa0))

## [2.0.0-test](https://github.com/athensresearch/athens/compare/v2.0.0-beta.27...v2.0.0-test) (2022-04-08)


### Bug Fixes

* use scalable ordering for events ([cce15ef](https://github.com/athensresearch/athens/commit/cce15ef4bb797126a18c12d911149e3e59f82785))


* fix ([39cedd6](https://github.com/athensresearch/athens/commit/39cedd6a5447702912da2bbbb90fd6883d41059a))
* update clojure ([a86ecfe](https://github.com/athensresearch/athens/commit/a86ecfeaf9f24bfcefefd758a9ecdb0a1034a8ae))

## [2.0.0-beta.28](https://github.com/athensresearch/athens/compare/v2.0.0-beta.27...v2.0.0-beta.28) (2022-04-12)


### Features

* add a slash command to insert own name link ([ad53214](https://github.com/athensresearch/athens/commit/ad532140ef62e25c8cf3a4eff6da93c8060cef0b))
* Page link creation reporting also from page titles. ([bc3a614](https://github.com/athensresearch/athens/commit/bc3a6142d3f4a7976555139dafd6e1546d4fba4e))
* support copying permalink ([7abf100](https://github.com/athensresearch/athens/commit/7abf100b35b37ffc22819848b06bdd509993fba2))
* support loading a url on first boot on web client ([f94cfab](https://github.com/athensresearch/athens/commit/f94cfab0524217a932c9e467717b6b533849e39e))


### Bug Fixes

* /me shouldn't add a space at the end ([4ab8adb](https://github.com/athensresearch/athens/commit/4ab8adb388e62e2cda5365c2a7a68053091b87d6))
* don't remove block if there's nothing to paste ([e2fd834](https://github.com/athensresearch/athens/commit/e2fd834794780854a76dbbd2d3530d893b387a63))
* don't show permalink button on electron ([52a969f](https://github.com/athensresearch/athens/commit/52a969ff9006b90aff17569705a4408ac205806c))
* ensure router starts after boot ([cfaff36](https://github.com/athensresearch/athens/commit/cfaff3668bde92c10165744a3111218b814c9302))
* focus on first block after paste ([6691e0a](https://github.com/athensresearch/athens/commit/6691e0ab2dd35ff088910bfefcc289570565ca79))
* only navigate at the end of the boot sequence ([4de8cf2](https://github.com/athensresearch/athens/commit/4de8cf25d4f447294793097b6098f87f7ce99b6c))
* seq is the right fn to check if not empty ([faf493f](https://github.com/athensresearch/athens/commit/faf493fd8ddf9cb5640a6010aab999e6c9e2c3a6))


### Refactors

* use contains-op? to filter op list ([b180ecf](https://github.com/athensresearch/athens/commit/b180ecf61fb390406600c2dd1acb0308f3b8d2e1))

## [2.0.0-beta.27](https://github.com/athensresearch/athens/compare/v2.0.0-beta.25...v2.0.0-beta.27) (2022-04-07)


### Features

* Don't report PII ([d230732](https://github.com/athensresearch/athens/commit/d2307328c35c8c0770dfb7ed89ea379926406978))
* migrate events without a uid ([98ab9df](https://github.com/athensresearch/athens/commit/98ab9dfe16556b293120a53f064c2365d85e46b8))
* migrate to efficient event log filtering ([420a128](https://github.com/athensresearch/athens/commit/420a1286627e22a9d978f936001a7c6e10443566))
* support event log migrations ([47fcabe](https://github.com/athensresearch/athens/commit/47fcabe2371ec21e641058ea88e5e6b04ba23f41))


### Bug Fixes

* allow remote dbs in web athens ([094192e](https://github.com/athensresearch/athens/commit/094192e5d23a821f0f04005e707310c082016f4e))
* bugs alex found ([677fb6e](https://github.com/athensresearch/athens/commit/677fb6ea085b4ed1d254d2e41b765b68a559c75e))
* current version is 0 if none is present ([bb8ef68](https://github.com/athensresearch/athens/commit/bb8ef68b886efe07f32ccfe5ce60de20c5c934c3))
* db-dump also ignored from limit on received ([a91df3c](https://github.com/athensresearch/athens/commit/a91df3cd60c05d132b008cf3fff21c1eca0dc483))
* event-log/events now received id as kw arg ([25543ff](https://github.com/athensresearch/athens/commit/25543ff4a23df89b86e13ae83cd70adb98c72c1a))
* exclude db dump from size limit ([7b28172](https://github.com/athensresearch/athens/commit/7b28172aaf764ea98b214ffe910eaafb211d8bfe))
* Feature block link correct counting ([#2111](https://github.com/athensresearch/athens/issues/2111)) ([f1807a2](https://github.com/athensresearch/athens/commit/f1807a2b35879b281c22e46b52dd8ba86996b7fc))
* fix args on migrate call site ([58ae736](https://github.com/athensresearch/athens/commit/58ae736581c59805305cc8f611af55f59c7d18e0))
* fix wording on some limit messages ([7f55150](https://github.com/athensresearch/athens/commit/7f55150b1ec395740bb859250fd0c2799ad7113e))
* get-current-version should return 0 on err ([a94fc64](https://github.com/athensresearch/athens/commit/a94fc648ce5d70d3eec78ba3f36e5b3a84dcb4d7))
* minor migration logging ([d716ae7](https://github.com/athensresearch/athens/commit/d716ae7d49581d9b766780091422c79c9ab1ff5e))
* query page size for migrations should be 100 ([7dadf54](https://github.com/athensresearch/athens/commit/7dadf546142095474e107884d573672d748040c5))
* remove leftover print ([84624c3](https://github.com/athensresearch/athens/commit/84624c3cf1b217706f381448fde3076cb12b49bb))
* set a 1MB event limit ([81198f4](https://github.com/athensresearch/athens/commit/81198f408119df7fa288fe0a1d042ba8bf8e0be1))
* show remote db page in db modal when not electron ([477ecd1](https://github.com/athensresearch/athens/commit/477ecd1b28377127415d6d0fe6449d8f3557945d))
* transit usage was very bork in clj ([92210e2](https://github.com/athensresearch/athens/commit/92210e2d260700828bc849e1324fe98c7fe930c9))
* use long instead of bigint for event log ([20ee4b4](https://github.com/athensresearch/athens/commit/20ee4b482e260425db8ffd491c6a73bd7c8e2a6a))


### Refactors

* add athens.self-hosted.fluree.utils ([19bf9d3](https://github.com/athensresearch/athens/commit/19bf9d338a41794eb588c12f9c6fca7467e58d20))
* move migrator into own ns ([0f97365](https://github.com/athensresearch/athens/commit/0f97365e97f422669f61c2b176646309f97541fd))


* ignore unused test helper ([146f459](https://github.com/athensresearch/athens/commit/146f459bbd8b5b3100c6dd88a7d07a49afb477b1))
* run fluree tests manually for now ([bb033c8](https://github.com/athensresearch/athens/commit/bb033c8ae1c4585ab03d36e21e530f70cc5c8172))
* update to clojure 11 ([8ac293e](https://github.com/athensresearch/athens/commit/8ac293ea4511e54c9400081f4704d2d688638833))

## [2.0.0-beta.26](https://github.com/athensresearch/athens/compare/v2.0.0-beta.25...v2.0.0-beta.26) (2022-04-06)


### Features

* migrate events without a uid ([98ab9df](https://github.com/athensresearch/athens/commit/98ab9dfe16556b293120a53f064c2365d85e46b8))
* migrate to efficient event log filtering ([420a128](https://github.com/athensresearch/athens/commit/420a1286627e22a9d978f936001a7c6e10443566))
* support event log migrations ([47fcabe](https://github.com/athensresearch/athens/commit/47fcabe2371ec21e641058ea88e5e6b04ba23f41))


### Bug Fixes

* allow remote dbs in web athens ([094192e](https://github.com/athensresearch/athens/commit/094192e5d23a821f0f04005e707310c082016f4e))
* bugs alex found ([677fb6e](https://github.com/athensresearch/athens/commit/677fb6ea085b4ed1d254d2e41b765b68a559c75e))
* current version is 0 if none is present ([bb8ef68](https://github.com/athensresearch/athens/commit/bb8ef68b886efe07f32ccfe5ce60de20c5c934c3))
* db-dump also ignored from limit on received ([a91df3c](https://github.com/athensresearch/athens/commit/a91df3cd60c05d132b008cf3fff21c1eca0dc483))
* event-log/events now received id as kw arg ([25543ff](https://github.com/athensresearch/athens/commit/25543ff4a23df89b86e13ae83cd70adb98c72c1a))
* exclude db dump from size limit ([7b28172](https://github.com/athensresearch/athens/commit/7b28172aaf764ea98b214ffe910eaafb211d8bfe))
* Feature block link correct counting ([#2111](https://github.com/athensresearch/athens/issues/2111)) ([f1807a2](https://github.com/athensresearch/athens/commit/f1807a2b35879b281c22e46b52dd8ba86996b7fc))
* fix args on migrate call site ([58ae736](https://github.com/athensresearch/athens/commit/58ae736581c59805305cc8f611af55f59c7d18e0))
* fix wording on some limit messages ([7f55150](https://github.com/athensresearch/athens/commit/7f55150b1ec395740bb859250fd0c2799ad7113e))
* get-current-version should return 0 on err ([a94fc64](https://github.com/athensresearch/athens/commit/a94fc648ce5d70d3eec78ba3f36e5b3a84dcb4d7))
* minor migration logging ([d716ae7](https://github.com/athensresearch/athens/commit/d716ae7d49581d9b766780091422c79c9ab1ff5e))
* query page size for migrations should be 100 ([7dadf54](https://github.com/athensresearch/athens/commit/7dadf546142095474e107884d573672d748040c5))
* remove leftover print ([84624c3](https://github.com/athensresearch/athens/commit/84624c3cf1b217706f381448fde3076cb12b49bb))
* set a 1MB event limit ([81198f4](https://github.com/athensresearch/athens/commit/81198f408119df7fa288fe0a1d042ba8bf8e0be1))
* show remote db page in db modal when not electron ([477ecd1](https://github.com/athensresearch/athens/commit/477ecd1b28377127415d6d0fe6449d8f3557945d))
* transit usage was very bork in clj ([92210e2](https://github.com/athensresearch/athens/commit/92210e2d260700828bc849e1324fe98c7fe930c9))
* use long instead of bigint for event log ([20ee4b4](https://github.com/athensresearch/athens/commit/20ee4b482e260425db8ffd491c6a73bd7c8e2a6a))


### Refactors

* add athens.self-hosted.fluree.utils ([19bf9d3](https://github.com/athensresearch/athens/commit/19bf9d338a41794eb588c12f9c6fca7467e58d20))
* move migrator into own ns ([0f97365](https://github.com/athensresearch/athens/commit/0f97365e97f422669f61c2b176646309f97541fd))


* ignore unused test helper ([146f459](https://github.com/athensresearch/athens/commit/146f459bbd8b5b3100c6dd88a7d07a49afb477b1))
* run fluree tests manually for now ([bb033c8](https://github.com/athensresearch/athens/commit/bb033c8ae1c4585ab03d36e21e530f70cc5c8172))
* update to clojure 11 ([8ac293e](https://github.com/athensresearch/athens/commit/8ac293ea4511e54c9400081f4704d2d688638833))

## [2.0.0-beta.25](https://github.com/athensresearch/athens/compare/v2.0.0-beta.24...v2.0.0-beta.25) (2022-04-04)


### Features

* Block/Page Creation Monitoring ([6c8a6fb](https://github.com/athensresearch/athens/commit/6c8a6fb84d755323c0e0d1cbf61a249001e63aab))
* Block/Page Creation Tracking ([32fa5b5](https://github.com/athensresearch/athens/commit/32fa5b564ab053e526695e4a24f359b30cbd61e9))
* Block/Page Creation Tracking ([bfa8611](https://github.com/athensresearch/athens/commit/bfa86115851384272619b5589819a61560d26572))
* Feature Usage Monitoring no autocapture no more ([#2107](https://github.com/athensresearch/athens/issues/2107)) ([6beeae3](https://github.com/athensresearch/athens/commit/6beeae31be83fa06f82b00492291ad34dca4d5ad))
* improved table ([d18c249](https://github.com/athensresearch/athens/commit/d18c2498339753222db4403aba6a9ff585c8205f))
* improved windwo dragging ([bd50ec2](https://github.com/athensresearch/athens/commit/bd50ec229260b4a33a0343cdbf4811aa87df9354))
* Link Creation Reporting ([722046f](https://github.com/athensresearch/athens/commit/722046f580e92d0751e478fc88e1456281111d93))
* Page Create Tracking ([54fe0d3](https://github.com/athensresearch/athens/commit/54fe0d391d3a60175975a80172678f70ad222727))
* Page Creation Reporting ([0866af6](https://github.com/athensresearch/athens/commit/0866af62c00b1b026db5f7a6b8083e9c1da38385))
* Page Creation Reporting ([540b31b](https://github.com/athensresearch/athens/commit/540b31bf375c13bb618382885361d476aa383df2))
* Page Creation Reporting ([b5aec96](https://github.com/athensresearch/athens/commit/b5aec96c4b4ac40c7f5040d5ceaec601d9b24fbc))
* Page Creation Reporting ([7abb1c1](https://github.com/athensresearch/athens/commit/7abb1c1e26cbc4f864908f9ed0882de02f3b14fa))
* Page Creation Reporting ([2591234](https://github.com/athensresearch/athens/commit/2591234fb08a02c82e855c1aacada03dbadf9017))
* Page Creation Reporting ([004494a](https://github.com/athensresearch/athens/commit/004494a19470453760e2d22f6b59a38a74ccb7a2))
* Page/Block Creation Reporting ([0aa51a3](https://github.com/athensresearch/athens/commit/0aa51a3163e2a1676d2800bd880eb1c6ca413488))
* progress ([aa7730a](https://github.com/athensresearch/athens/commit/aa7730a8bff8824be7d0058af67852b53b1cb105))
* progress ([0ff1313](https://github.com/athensresearch/athens/commit/0ff131302e6ebbdc4a1ffa7715b057c760a88a24))
* responsive secondary toolbar menu ([8482045](https://github.com/athensresearch/athens/commit/8482045a2e3e615d27735cd2ef808122be0e7594))
* structural-diff of Atomic Graph Ops ([a18efcd](https://github.com/athensresearch/athens/commit/a18efcd50a6df9f0d9fac5fd632439b127f80b80))
* toast now reacts to theme color mode ([b3feb98](https://github.com/athensresearch/athens/commit/b3feb98f63313ad36d1ceb22a5024e4338c503a9))


### Bug Fixes

* add missing change ([a9a9e5a](https://github.com/athensresearch/athens/commit/a9a9e5a1f49c0321e6520ce604440a15c0ac3479))
* add santized block uid to anhor ([d81213a](https://github.com/athensresearch/athens/commit/d81213a9cbb5872df6ac7bbe6ec249e182e8ed9c))
* better borders within athena ([63a4faf](https://github.com/athensresearch/athens/commit/63a4faf622f993ecb8d86bb103960425e5525fa2))
* brighter athena in light mode ([311a9f0](https://github.com/athensresearch/athens/commit/311a9f0855ea49fa6c6a4b9103abda186166a2fd))
* can close sidebar items again ([b0ed26c](https://github.com/athensresearch/athens/commit/b0ed26cd7b7cd0b1c3c7d5ca02e63c86992b65b3))
* can edit node page titles again ([903f040](https://github.com/athensresearch/athens/commit/903f040e50f5efe4fb807cb65bf749f0c22c1361))
* can edit title on block page ([06b2fe7](https://github.com/athensresearch/athens/commit/06b2fe7fc3b923b6927e183acb6a84984df26aff))
* don't break the theme ([4557c6e](https://github.com/athensresearch/athens/commit/4557c6ecf1299318a0780223348d14575db21372))
* don't downlevel generators in libraries ([f46decc](https://github.com/athensresearch/athens/commit/f46decc0ed72191308bd70e1059b341b9a553204))
* fix cursor insertion on first interaction ([68f5507](https://github.com/athensresearch/athens/commit/68f55075a78ffbf017e7e1ef0960f2ff523484b4))
* left sidebar should be reactive ([48e0b2b](https://github.com/athensresearch/athens/commit/48e0b2ba011d1a0bc7ee5426bbf26b695416412e))
* misc block interaction issues ([82da8bc](https://github.com/athensresearch/athens/commit/82da8bc3b92b70e7c9027e02bb3ef03ddf375e5c))
* misc fixes ([d0fc6db](https://github.com/athensresearch/athens/commit/d0fc6dba1365dd352be7d572f408ede68ec3e080))
* more elements interactable within blocks ([7e85d23](https://github.com/athensresearch/athens/commit/7e85d23deefd611f0ebf248db8519373c10fb595))
* more tests passing ([f881023](https://github.com/athensresearch/athens/commit/f881023deb7dba3a1c5495eec4a0510b83f5d3a0))
* no isEditing error ([c7d8501](https://github.com/athensresearch/athens/commit/c7d85018e7d2698d25110a7ca488fe2ceaf33bd9))
* partially working inline search menu ([92bc797](https://github.com/athensresearch/athens/commit/92bc797777dc85d18546f5d6729439a90bee782f))
* remove unused binding ([5a8e297](https://github.com/athensresearch/athens/commit/5a8e29772d1c565c859a54a8eba06b3eaafa3a10))
* right sidebar items open by default ([5fcaf9a](https://github.com/athensresearch/athens/commit/5fcaf9af241e0cffdf059db08a7370367640194a))
* sidebar items open by default ([5fbde80](https://github.com/athensresearch/athens/commit/5fbde802a9219ad8c7ba0d78336bd1e501cbaa98))
* solve minor issues with toolbar and all-pages ([876de3d](https://github.com/athensresearch/athens/commit/876de3d344317599d88f746e6a6cafae524fa574))
* some athena tests passing ([0421032](https://github.com/athensresearch/athens/commit/04210326adc43d83f6b04cc5832c489df9d89cb6))
* style ([010f9ce](https://github.com/athensresearch/athens/commit/010f9ceb6d135034c970b4d283c4e4aea30157cd))
* style ([f1084cc](https://github.com/athensresearch/athens/commit/f1084cc32cbfe718f76749d1b4e067bde66e7682))
* titlebar border transitions nicely ([d38f459](https://github.com/athensresearch/athens/commit/d38f45953aec958e7194ad8d6a26346be4d57f89))
* use correct border color in help sections ([3cf971d](https://github.com/athensresearch/athens/commit/3cf971d0114b0c37f43e9b619c6fd982fff0a43f))
* use correct case for Icons.tsx ([593ebb9](https://github.com/athensresearch/athens/commit/593ebb9fdd7b82324dd9d29311705f9c48499301))
* use SSR friendly selector to remove console error ([9378f72](https://github.com/athensresearch/athens/commit/9378f727781c5bf892091b505266c993a6a351a3))
* used wrong source ([1bcff3c](https://github.com/athensresearch/athens/commit/1bcff3c8526d0a6065eab250d4def232d9bcdbb2))
* working inline search menu ([8233f99](https://github.com/athensresearch/athens/commit/8233f99252c6739bc340d7f4e9a3d9e607fd2218))


### Refactors

* `:block/save` cleanup ([67d7251](https://github.com/athensresearch/athens/commit/67d725170798aed477516436bb102be33a485232))
* block in new components for autocomplete searc ([ce6aeef](https://github.com/athensresearch/athens/commit/ce6aeef0f01abcf730185782da72df2805b73df1))
* cleaner use of error boundary ([060b366](https://github.com/athensresearch/athens/commit/060b366c8f09b93643446ada80a9cf8dffbc2e86))
* completed slash menu ([9ad61f7](https://github.com/athensresearch/athens/commit/9ad61f768570c57eb6d63d0113fee8ff02f54e8f))
* remove styled-components ([486e44a](https://github.com/athensresearch/athens/commit/486e44ac6ebe33768317f54baaf830ba475669be))
* replace most uses of stylefy ([241e052](https://github.com/athensresearch/athens/commit/241e0526eab1cd474505e6318b60d4792092701a))
* retire most storybook components ([f0f3a52](https://github.com/athensresearch/athens/commit/f0f3a52459237079a03c059a7c855f2d85fb6b2a))
* structure parser generating text representation ([b18d119](https://github.com/athensresearch/athens/commit/b18d119c86948bef9a44aaf551a178bbb8d60671))
* use better slash menu ([569e62e](https://github.com/athensresearch/athens/commit/569e62e666349e20b7cc6bd2e0d63587c1b8bda8))
* use chakra for block content element ([1e9a440](https://github.com/athensresearch/athens/commit/1e9a440d4fcc039ecce088915d9cb9c28e745ed4))


* another test passing ([cb636c2](https://github.com/athensresearch/athens/commit/cb636c2ccdc1fbdab445941523c730efe94bd4ba))
* clean up toggle ([7367508](https://github.com/athensresearch/athens/commit/736750874b52df53cec95117e8025003791a1633))
* cleanup ([fd5aeb5](https://github.com/athensresearch/athens/commit/fd5aeb545b13d1a9423d9322b24b84502f7c35d4))
* cleanup ([c600a51](https://github.com/athensresearch/athens/commit/c600a511fc4243b9e41fcf1efc0fb0db5ec8cb41))
* cleanup lint ([99b7fc8](https://github.com/athensresearch/athens/commit/99b7fc877376c9f996f43995a60612ca7554b492))
* cleanup minor redundancies ([9205337](https://github.com/athensresearch/athens/commit/9205337402428229cd975d517c2d82ae1f0a41f6))
* cleanup unused ([73f0b6c](https://github.com/athensresearch/athens/commit/73f0b6c44ef01429e9aa6d29ff32182e5b40214d))
* fix ([91a27f8](https://github.com/athensresearch/athens/commit/91a27f8d49c2fab44bd63282b7d3598d11c80d5f))
* formatting cleanup ([80cdcc1](https://github.com/athensresearch/athens/commit/80cdcc11b30617d05ce8b39e0c056fb316720e58))
* lint ([42e299d](https://github.com/athensresearch/athens/commit/42e299d0534c3a41525557c891b5dc39be872f14))
* lint ([ebbdf5d](https://github.com/athensresearch/athens/commit/ebbdf5dfb80655db5c8ef0a55711945754bd2ed5))
* lint ([d7780cb](https://github.com/athensresearch/athens/commit/d7780cb583c7a91ecf0a403ee30da54ae7e25470))
* lint ([a2cbce8](https://github.com/athensresearch/athens/commit/a2cbce8d702d0dbaf22869b1f771307ba0aa6e7a))
* linting fixes ([c3f42a8](https://github.com/athensresearch/athens/commit/c3f42a80d2806defc0f054234c494aebf557845f))
* more cleanup ([c5de678](https://github.com/athensresearch/athens/commit/c5de6787eca2fc74e0765d4f62d29ab2e5defc60))
* more tests passing ([d445d5b](https://github.com/athensresearch/athens/commit/d445d5bfb00a6ee4906365937719f583542efbfd))
* remove unused ([da7e86c](https://github.com/athensresearch/athens/commit/da7e86c813a913de8988c749b108f9c500f99adf))
* remove unused lint rules ([033b73b](https://github.com/athensresearch/athens/commit/033b73b34d32eec68fe98743bf68c8e17c2a147c))
* style cleanup ([008011b](https://github.com/athensresearch/athens/commit/008011b9c0f62e1cf66852b912ca0dd3861c152a))
* unbreak storybook ([b36184e](https://github.com/athensresearch/athens/commit/b36184e93db11e097002df1449b1e0798773a043))
* update e2e page title selectors ([b59e61d](https://github.com/athensresearch/athens/commit/b59e61def708b11192d5691628de31e72ed6e639))
* use same babel preset on main as on app ([8012176](https://github.com/athensresearch/athens/commit/8012176e2989d4f49e0e57fb656e9dbb701c3b56))

## [2.0.0-beta.24](https://github.com/athensresearch/athens/compare/v2.0.0-beta.23...v2.0.0-beta.24) (2022-03-17)

## [2.0.0-beta.23](https://github.com/athensresearch/athens/compare/v2.0.0-beta.22...v2.0.0-beta.23) (2022-03-15)


### Bug Fixes

* docker path is /srv/ not /src/ ([#2083](https://github.com/athensresearch/athens/issues/2083)) ([bfb3fde](https://github.com/athensresearch/athens/commit/bfb3fdef2a55539c10ae2508853ab2077ca8e9a7))

## [2.0.0-beta.22](https://github.com/athensresearch/athens/compare/v2.0.0-beta.21...v2.0.0-beta.22) (2022-03-15)


### Bug Fixes

* default config should use docker values ([73e5224](https://github.com/athensresearch/athens/commit/73e5224d4a88291b4e670af3dcc81e6f6b3dfea6))

## [2.0.0-beta.21](https://github.com/athensresearch/athens/compare/v2.0.0-beta.20...v2.0.0-beta.21) (2022-03-15)


### Features

* add :datascript :persist-base-path to server config ([712759d](https://github.com/athensresearch/athens/commit/712759dabe37e4f5a6a61ebc1733e594021c65db))
* add athens.self-hosted.web.persistence ns ([5b7c664](https://github.com/athensresearch/athens/commit/5b7c6643d45aaa3fab2c0727c282efed65ed58f6))
* allow cli load to resume from last event ([e82a71b](https://github.com/athensresearch/athens/commit/e82a71b6d6d2f98e92caf04c3cdbcf993b7b25db))
* block-ref's breadcrumb in tooltip ([109610e](https://github.com/athensresearch/athens/commit/109610e048d574e7734156ef1ea3cc733df08daa))
* incremental snapshotting on load ([3a69a27](https://github.com/athensresearch/athens/commit/3a69a27dcdac623cf971fdf69ba3b2a85d07d132))
* persist server datascript db ([7d2fd6b](https://github.com/athensresearch/athens/commit/7d2fd6b4ddbb8984b0176d26cab98043a83f09ac))


### Bug Fixes

* cljs throws js stuff ([2c99f48](https://github.com/athensresearch/athens/commit/2c99f4852f3a60184b709278cb022deec9040936))
* event-id should be a uuid ([858642c](https://github.com/athensresearch/athens/commit/858642c7812d4e2cf5daa26f65cff5d0ff2341a3))
* throw when ref-block does not have parent ([5bbafa9](https://github.com/athensresearch/athens/commit/5bbafa924649ed21b9c692b8864a6bfdf76d1b72))
* time logging should be in double ([e04e553](https://github.com/athensresearch/athens/commit/e04e5533990f3d47417c73885ffae4bc10860e0b))
* workaround slow fluree filter ([4be370d](https://github.com/athensresearch/athens/commit/4be370d52932e1570a67c927fb6abcd9a37a38b5))


### Refactors

* make most persistence ns fns private ([4e420ea](https://github.com/athensresearch/athens/commit/4e420ea7eeb37fa8ad8a311a98a6055ff3bd1283))
* move stuff around after thinking more ([b518f8d](https://github.com/athensresearch/athens/commit/b518f8df909cf46d7f8b87490811efd03099acb9))
* Remove unused namespace reference ([34c7596](https://github.com/athensresearch/athens/commit/34c7596f570eca2b7955bf4f66ac99a9e329a03c))
* use /datascript/persist instead of just /persist/ ([f62f829](https://github.com/athensresearch/athens/commit/f62f829f2cf9347446571ab3c9594443237d2ed3))


* refactor and test position->uid+parent ([48786cd](https://github.com/athensresearch/athens/commit/48786cd69df368796f88caa070428de1f8074181))

## [2.0.0-beta.20](https://github.com/athensresearch/athens/compare/v2.0.0-beta.19...v2.0.0-beta.20) (2022-03-11)


### Bug Fixes

* HOC perf mon to always show forwarded comp. ([719eb7b](https://github.com/athensresearch/athens/commit/719eb7b50ccb83798dad71ba6f53a4f5924726d8))


* cljstyle fix ([e86e0d7](https://github.com/athensresearch/athens/commit/e86e0d71af2c0cb89fa0900773c05178bb4a3eb1))

## [2.0.0-beta.19](https://github.com/athensresearch/athens/compare/v2.0.0-beta.18...v2.0.0-beta.19) (2022-03-10)


### Features

* Feature monitoring: right-sidebar usage ([f92d19b](https://github.com/athensresearch/athens/commit/f92d19bb0388d1f9dbc6e61cbd9fa479531afb13))


### Bug Fixes

* Allow to start with empty line. ([1095dc5](https://github.com/athensresearch/athens/commit/1095dc5bc9df23b23b507d098d8d5ea3fbfe530c))


* testing new behavior ([22361aa](https://github.com/athensresearch/athens/commit/22361aa33c57cd0bd36a4a307ceb690109924582))

## [2.0.0-beta.18](https://github.com/athensresearch/athens/compare/v2.0.0-beta.17...v2.0.0-beta.18) (2022-03-03)


### Features

* directly show children on inline refs ([00b39e4](https://github.com/athensresearch/athens/commit/00b39e4dfd2960dedb4ddc9f45e218538e519021))
* don't warn on exit while editing ([d6911fc](https://github.com/athensresearch/athens/commit/d6911fc9807ab5049dcaeb0451fa0cde00b47eaf))


### Bug Fixes

* also unlink `:node/title` contents when unlinking ([2e1e45b](https://github.com/athensresearch/athens/commit/2e1e45ba2280f2ceade98928b88b2484f982802e))
* Case of nested page links ([491e2e8](https://github.com/athensresearch/athens/commit/491e2e8b678aff7d7dece7d351498b3e9f567159))
* show more than 1000 links in all page listing ([e538bdb](https://github.com/athensresearch/athens/commit/e538bdb440e865f7f8c5e5a2986e6efa759e2df1))


### Refactors

* dead code is dead code. ([4866c57](https://github.com/athensresearch/athens/commit/4866c57556dc5c3cebe3b6eb56b226fd15473353))
* Improved readability, better naming. ([3b4788b](https://github.com/athensresearch/athens/commit/3b4788b7be3d6340e11f00e375e0b85f3150f732))

## [2.0.0-beta.17](https://github.com/athensresearch/athens/compare/v2.0.0-beta.16...v2.0.0-beta.17) (2022-02-28)


### Bug Fixes

* handle loading states on reconnect in a sane way ([f6eee39](https://github.com/athensresearch/athens/commit/f6eee3901645b91ed77727119091bdef06830cfd))
* query needs db value, not conn ([e8b6d0e](https://github.com/athensresearch/athens/commit/e8b6d0e7e2c247e2735bee4ce2abba27db0dbed9))
* revert visibility change to fn ([4b81e11](https://github.com/athensresearch/athens/commit/4b81e118a4e6dbb8da316a0ba4c764d2187f5507))


* add more block context menu tests ([acc58e8](https://github.com/athensresearch/athens/commit/acc58e86cca33d3cf809b98914554031a3603ff9))
* fix click out expectations ([0d48659](https://github.com/athensresearch/athens/commit/0d48659e9e5088907ddecd494c531fc26a6f6e52))

## [2.0.0-beta.16](https://github.com/athensresearch/athens/compare/v2.0.0-beta.15...v2.0.0-beta.16) (2022-02-23)


### Features

* add deftraced sentry macro ([1d567b7](https://github.com/athensresearch/athens/commit/1d567b7ad76b3fa2d4804825e90948461d127d16))
* HOC for perf monitoring ([be66ca8](https://github.com/athensresearch/athens/commit/be66ca8c9d71368a409d45c18c2ffd10c42585f6))
* HOC not nesting incorrectly. ([3114cc1](https://github.com/athensresearch/athens/commit/3114cc1ca10a6d7001cda66df8050787c5f7e9bb))
* macro for sentry wrapping ([ad60923](https://github.com/athensresearch/athens/commit/ad6092359d14a649f8af186b962533d92aab2ca2))
* perf monitoring of `:boot/desktop` ([381e914](https://github.com/athensresearch/athens/commit/381e9149e28bddab1d0be0ae447eda57abfac557))
* rendering performance monitoring. ([64a497d](https://github.com/athensresearch/athens/commit/64a497d75becfbcaf866a30161acc5cc66183317))
* router perf monitoring ([b90f9c4](https://github.com/athensresearch/athens/commit/b90f9c4448d3c0ed1578bf5b301be7a6715dac9b))
* Sentry perf monitoring edition async-flow ([cd0d0d9](https://github.com/athensresearch/athens/commit/cd0d0d959ff7a029fe4fc3f53d4d49942534779a))
* use the same undo for local and remote ([0dadf6d](https://github.com/athensresearch/athens/commit/0dadf6d1d4016e2b85b90c5ae92663a3fed2cbc1))


### Bug Fixes

* add missing type hint ([ead4788](https://github.com/athensresearch/athens/commit/ead478859b9ebaa22add4924cd57c335f84781cd))
* allow detect chromium via user agent ([f1e51cf](https://github.com/athensresearch/athens/commit/f1e51cf6857f971a95ccf1d59f10afaee8651a8f))
* also cover cmd+q and activate states in mac ([2a520a7](https://github.com/athensresearch/athens/commit/2a520a7995ce1d10cf83f35203353225c93b9d33))
* also disable block-uid-nil-eater for clj ([fbdfcee](https://github.com/athensresearch/athens/commit/fbdfcee027e731b0b4b059253b6b83e77e37ad00))
* also navigate on delete if uid matches ([47f11c4](https://github.com/athensresearch/athens/commit/47f11c4c5224279d7685a82669c7c43282c6492e))
* always use athens undo/redo ([8ed0d0a](https://github.com/athensresearch/athens/commit/8ed0d0aa16f2bf787fbc36929e68c21c2759dd53))
* athena navigation test should wait for boot ([3bd0504](https://github.com/athensresearch/athens/commit/3bd0504876396f4a52f38bb5530b39b41de9f078))
* athena tests should cleanup page ([399a08b](https://github.com/athensresearch/athens/commit/399a08bfe232c17aa581901b45641fc309a117e7))
* block ref render should be reactive ([d9dc5da](https://github.com/athensresearch/athens/commit/d9dc5dafd6d1805f94976688acc114c58a99d04e))
* boot sequence and db-dump async flows ([7f2429e](https://github.com/athensresearch/athens/commit/7f2429e42f337317a895deb359371f3adb7f6fcb))
* check if tx is running, don't assume ([d0ba723](https://github.com/athensresearch/athens/commit/d0ba723f2944a82a6211adb4027908beb552993e))
* clicking inline ref count should show them ([1a6297d](https://github.com/athensresearch/athens/commit/1a6297de027641d72d7e6a42eb30f578763409ad))
* daily notes should update reactively ([2f4d8a9](https://github.com/athensresearch/athens/commit/2f4d8a927dbb0ae3e34800dbb5aee3bd01eac429))
* defntrace should stringify name ([ca04841](https://github.com/athensresearch/athens/commit/ca04841200d3014e7069c4dbb614ac801f006926))
* defntrace should work in clj ([d339806](https://github.com/athensresearch/athens/commit/d339806b10e2a56bd61c75a213580c48e583ae4b))
* don't `div` just fragment ([e1039c3](https://github.com/athensresearch/athens/commit/e1039c345228c19586c5a1fc7c196fc4922aa331))
* don't health check datoms db-dump ([ce79565](https://github.com/athensresearch/athens/commit/ce795658802435eddf1f5dd8381cdb445b9464f3))
* don't health-check empty dbs ([89668e9](https://github.com/athensresearch/athens/commit/89668e9208728c0791bb17cfd4636e60e4237ad8))
* don't remove used helper fn ([841572b](https://github.com/athensresearch/athens/commit/841572b68c434fdc803f16abca2c7f10a4fddb35))
* don't report all console debugs to Sentry. ([4b0bc13](https://github.com/athensresearch/athens/commit/4b0bc13e74026a69f33af8b231b30feb019e933c))
* get-node-document still needs to return children strs ([d19c776](https://github.com/athensresearch/athens/commit/d19c776e0041439998eb5d3ce18ab564353fcf2f))
* guard pulls in common-db ([45683a2](https://github.com/athensresearch/athens/commit/45683a274e8add1d1494bbb9a753df8bcb032805))
* initialize reactive watchers on startup ([#2037](https://github.com/athensresearch/athens/issues/2037)) ([bd0441c](https://github.com/athensresearch/athens/commit/bd0441cc519b5c3980589c013784911a7041a7c8))
* make copy block tree great (again?) ([fa2d3e6](https://github.com/athensresearch/athens/commit/fa2d3e65020c73d8e4f1dd815170763cf23de8f1))
* match posh pull behaviour for refactored fns ([65d3c00](https://github.com/athensresearch/athens/commit/65d3c001763623e2ef73a786dfb8400931652158))
* must check if page was removed before removing it ([48d26b8](https://github.com/athensresearch/athens/commit/48d26b80fa0db2ac76acf1cb0dfff19a1ba30fca))
* navigate to page from athena when result is for a block ([b6ce6cf](https://github.com/athensresearch/athens/commit/b6ce6cfa3bcced42b0f69944c304c569e227a0e3))
* no more warnings for no reason. ([239bab7](https://github.com/athensresearch/athens/commit/239bab777b0d2615846a78d5eee8e0ec41e99b04))
* not pushing sentry span here anymore ([df60f26](https://github.com/athensresearch/athens/commit/df60f269bbb5a9ecbb8ceaf00a3e2095005386be))
* remove timeout from saveLastBlockAndEnter ([7568345](https://github.com/athensresearch/athens/commit/75683456924a4298a72e62cac58d62389c90d9f6))
* review items ([5a7b6b3](https://github.com/athensresearch/athens/commit/5a7b6b381550fc1138cdcb84c4dc4ce698d9990a))
* set loading while reconnecting ([1771d21](https://github.com/athensresearch/athens/commit/1771d21d4507b1ec238e0d79f9719af62f6b3c05))
* textarea should not be rendered when not editing ([a4f11e2](https://github.com/athensresearch/athens/commit/a4f11e2fa8a20ab202d4f8f9dbbecc2c44f970e5))
* this interceptor was promoting span to auto-tx ([1951e68](https://github.com/athensresearch/athens/commit/1951e6895de100c77234827f6c3b42a1cd7792fb))
* update existing cached dbs to have http-url ([074dc7e](https://github.com/athensresearch/athens/commit/074dc7ec2673d17d8ae266f784bd1bc034529bd3))
* update test should focus on a single update ([87ec730](https://github.com/athensresearch/athens/commit/87ec7304dea086caf473711f007b2540b1b902b2))
* use async-flow for local loading db too ([9577575](https://github.com/athensresearch/athens/commit/9577575f00379f47ebb573f1c27484b9063f9acf))


### Work in Progress

* Interceptors ([bedb901](https://github.com/athensresearch/athens/commit/bedb9015855d198889f44565ca2a703af3298190))
* start wrapper around Sentry perf monitoring ([65daccb](https://github.com/athensresearch/athens/commit/65daccb2b1a50f7db5ea462d747e9ed8425e6d58))


### Refactors

* add a few e2e utils ([75d3f3d](https://github.com/athensresearch/athens/commit/75d3f3dff628e2d2592cab2a94dfda383a53a7da))
* add inputInAthena helper ([c3a831a](https://github.com/athensresearch/athens/commit/c3a831a4fe5c8ecd8cc0aa17464cff12b3d04dc9))
* blocks pull their own data ([f64539e](https://github.com/athensresearch/athens/commit/f64539ecef940f3ce1ed0cfb4a6b5630a6ecbd15))
* disable posh during reset-conn ([8215194](https://github.com/athensresearch/athens/commit/821519445a052dc934f8a7af1c71cc2ab1006740))
* ensure title is visible after page creation ([0da7251](https://github.com/athensresearch/athens/commit/0da7251fb6bd327fb77050253a924a91ab888e6c))
* factor out async flow and sentry tx ([f65f8e6](https://github.com/athensresearch/athens/commit/f65f8e6085a9a96e082d69120d904d2b00cd2b8a))
* finish async flow just through resolve-transact-forward ([949665d](https://github.com/athensresearch/athens/commit/949665d3c47fada3ce48e32967a1858e79f829a7))
* fold :remote/forward-event into :resolve-transact-forward ([df62d52](https://github.com/athensresearch/athens/commit/df62d52ddcd15a3cf0a5309f5fb4075a2c309fd8))
* get-block is not used reactively ([dd24335](https://github.com/athensresearch/athens/commit/dd24335d94219fe2bbd376a62e9951719a02686c))
* isolate and optimise get-block-document ([1d0112d](https://github.com/athensresearch/athens/commit/1d0112df5a50c7c6090303fd737d7a5df49159b3))
* isolate and optimize get-linked-references ([a14861c](https://github.com/athensresearch/athens/commit/a14861cf05cecc60680820e2eb1148bcad9d63a8))
* isolate and optimize get-node-document ([2d4dbf0](https://github.com/athensresearch/athens/commit/2d4dbf0af831590c882a0423c45fff9e4ac6687e))
* isolate and optimize get-parents-recursively ([099c574](https://github.com/athensresearch/athens/commit/099c574e2626b9b7969d4d73e143c1fb22326b4d))
* move block page linked refs into own comp ([30d6c5d](https://github.com/athensresearch/athens/commit/30d6c5d9f7020313f5c6ceffd3ddca768e1d7d0c))
* move block page parents to own comp ([ef76bb3](https://github.com/athensresearch/athens/commit/ef76bb3ee7a3d1e94ca31a83254ccf6c052dd35d))
* prefer editing/is-editing over editing/uid ([ae37edd](https://github.com/athensresearch/athens/commit/ae37edd84ec3a04ce9b3807986fb4a6ea6025235))
* remove a few leftover uses of posh ([2119b13](https://github.com/athensresearch/athens/commit/2119b133c894f13c789eeb2f9625a7886cb9e853))
* remove block-uid-nil-eater from txs ([59c2395](https://github.com/athensresearch/athens/commit/59c23958730b4dcf51cb34171eb9b37adf39a327))
* remove old single player undo ([7f51c40](https://github.com/athensresearch/athens/commit/7f51c404a8952aa00339adc317451aa1894e8d19))
* rename deftrace to defntraced ([5a08a2b](https://github.com/athensresearch/athens/commit/5a08a2b3e51bbff990efa0505393812c4c7a9d47))
* resolve https on db creation ([f8f9f22](https://github.com/athensresearch/athens/commit/f8f9f220561cd99eaa7ba199dbbcd249c02ef1fa))
* resolve https on db creation ([5538e48](https://github.com/athensresearch/athens/commit/5538e4870a428674bded40d83dab3c8e1c10124c))
* tighten posh use ([1917b7e](https://github.com/athensresearch/athens/commit/1917b7e7d0563785c05d587560a5ec4c40e77c7c))
* use new Sentry tracking macro ([fae6cc8](https://github.com/athensresearch/athens/commit/fae6cc8cbfa53649ad131a979d31101fe7a947e1))
* wrap body in deftraced ([27ed22d](https://github.com/athensresearch/athens/commit/27ed22daa1188b704b725778f24fac2c45185eb7))


* add a base electron test script ([0a3f4ba](https://github.com/athensresearch/athens/commit/0a3f4ba6ecc212725f0fa12ff5265502c96de77b))
* add copy refs test ([74faa9a](https://github.com/athensresearch/athens/commit/74faa9a4186557b063dac378339dcf58266be10d))
* add undo e2e ([ac66d00](https://github.com/athensresearch/athens/commit/ac66d00f59eca76e6409b0abd8fd3df217599569))
* cache the build for e2e ([72cfe9c](https://github.com/athensresearch/athens/commit/72cfe9c4986a9a38f9f8cc3631eecd5d41e64c78))
* cleanup log/info ([94e93d2](https://github.com/athensresearch/athens/commit/94e93d2f80ffacaa38d3f56aa913c243a91754e1))
* fail e2e faster ([1f10d69](https://github.com/athensresearch/athens/commit/1f10d6979fceba8583aee9d15e413c11aa618d26))
* fix ([f34d414](https://github.com/athensresearch/athens/commit/f34d414ac883c5d27ee04a566d1a0da562b4dbdc))
* fix lint error ([9c788f8](https://github.com/athensresearch/athens/commit/9c788f81f669caf2ae6ce4420d2e96db2a74d3ea))
* fixes ([ddcb4ea](https://github.com/athensresearch/athens/commit/ddcb4ea5c83152794a8b7673462e07f66606aaad))
* remove unused fn ([04ff24c](https://github.com/athensresearch/athens/commit/04ff24c96fbea30b345cb1f45235bab5f549ede9))
* remove unused ns ([73b6005](https://github.com/athensresearch/athens/commit/73b6005f7897adcdf31cf612c60c45f0f3b0dcd4))
* remove unused require ([8238259](https://github.com/athensresearch/athens/commit/823825922c5cadf9d31e9d05d3f77eee186418c9))
* removed unused fn ([9f4517b](https://github.com/athensresearch/athens/commit/9f4517bf915e725588bad227ad8539fa58bb98d5))
* rename Sentry's TX and span so we know where it's coming from. ([dcb3522](https://github.com/athensresearch/athens/commit/dcb352277903df809484ee523330ec8610f5c66e))
* shorthands and environment names ([9dc0667](https://github.com/athensresearch/athens/commit/9dc0667353230c51fdcaebd9017e0cb7c5f31fbf))
* use default reporters for e2e ([63c43ea](https://github.com/athensresearch/athens/commit/63c43eacd057c389b803fac6498f43727004ab4b))
* use multiple workers on web e2e ([43a128d](https://github.com/athensresearch/athens/commit/43a128d25c6f137d8ace0c73d6c57bea13aa6a25))
* use primarily web build for e2e ([73821c0](https://github.com/athensresearch/athens/commit/73821c0ee943ad7a9bdf2cebea616353682bf4f7))

## [2.0.0-beta.15](https://github.com/athensresearch/athens/compare/v2.0.0-beta.14...v2.0.0-beta.15) (2022-02-02)


### Bug Fixes

* a few bad electron specific invocations ([08bee39](https://github.com/athensresearch/athens/commit/08bee39822f09e2bc39731c8000b94632890ef14))
* allow any domain to health check ([7f9e653](https://github.com/athensresearch/athens/commit/7f9e653c4e2aae01ffe2c06bfd4311dd4ce7a0c7))
* clicking on a breadcrumb should it as the new parent view ([1bb6c0b](https://github.com/athensresearch/athens/commit/1bb6c0b31d7ca4b27fa78537a1e7ecba28640015))
* open/close inline refs should reset breadcrumbs state ([5042d1e](https://github.com/athensresearch/athens/commit/5042d1e0c95781512c668dcb9c8590114efc3657))
* reader features should be under compiler-options ([6186245](https://github.com/athensresearch/athens/commit/618624526d0f253d3d873e6d9c59b556eef7de39))
* remove stray print ([c6c458a](https://github.com/athensresearch/athens/commit/c6c458a43849ed40c0ca023348ec61d0a827ed0d))


### Refactors

* centralize electron requires, provide good errors ([4c540b4](https://github.com/athensresearch/athens/commit/4c540b4a76f52612a4f83c4f8b78b57f7e7d39fe))
* unify web and desktop boot ([75e66d8](https://github.com/athensresearch/athens/commit/75e66d83b47972d2227127b109814ceb04fc2c40))
* use reader conditionals for electron code ([dabbf59](https://github.com/athensresearch/athens/commit/dabbf59fdd153e0cec8554298139fc949b577c7a))


* add note about local docker builds on M1 ([fef4f65](https://github.com/athensresearch/athens/commit/fef4f6530d394808c68d33f6f19312c6168c9687))
* deploy v2 browser app on release ([d2e71be](https://github.com/athensresearch/athens/commit/d2e71be5db0587d5b06a52b55e4e4e5da366082c))
* deploy web client to vercel ([d954288](https://github.com/athensresearch/athens/commit/d954288f8575d110b4e920e7f4155286f2f1f274))
* fix ([176af6f](https://github.com/athensresearch/athens/commit/176af6f49a0cc3af9317f04661751d6bba5e005e))
* fix release-web tag check ([c6e7919](https://github.com/athensresearch/athens/commit/c6e7919717e5338fde8a24e3da41272bf14041fe))
* fix vercel cp ([9f7cf43](https://github.com/athensresearch/athens/commit/9f7cf43d9c6e1d7859614d7fa44f4e179ca82214))
* remove unused file ([5902e9a](https://github.com/athensresearch/athens/commit/5902e9a444e7d419ba4ef11a4bdf7d21339cee51))

## [2.0.0-beta.14](https://github.com/athensresearch/athens/compare/v2.0.0-beta.13...v2.0.0-beta.14) (2022-01-31)


### Features

* show references inline ([#2006](https://github.com/athensresearch/athens/issues/2006)) ([bf568d8](https://github.com/athensresearch/athens/commit/bf568d8188607a43639a7019b48ccf274a4be818))


### Bug Fixes

* include :block/{new,save,remove} in the reorder exception ([ffac7f7](https://github.com/athensresearch/athens/commit/ffac7f7388da837e1e6261cee6f67ed3e41931b2))
* redo-middle-move-composite should be comparing both pages ([9984e63](https://github.com/athensresearch/athens/commit/9984e6323288ffdf66a86fb9d10b5f699cc1f4bf))


* add undo move/remove failure scenarios ([7551a7d](https://github.com/athensresearch/athens/commit/7551a7d383f15223c530732c54075c141cad9c1e))


### Refactors

* binding should reflect operation ([ab36105](https://github.com/athensresearch/athens/commit/ab36105bf9844ed23aee835ed3b78cde9164a23e))

## [2.0.0-beta.13](https://github.com/athensresearch/athens/compare/v2.0.0-beta.12...v2.0.0-beta.13) (2022-01-24)


### Bug Fixes

* **indent:** if sibling block is closed, open ([a43a71c](https://github.com/athensresearch/athens/commit/a43a71cb50b1f106ad588254e34c03db9579d8d4))
* pressing up and the previous block has a closed child ([d2b795f](https://github.com/athensresearch/athens/commit/d2b795ff65b886847bf02ff62462aee0481e5ad0))
* small paste bugs ([#1991](https://github.com/athensresearch/athens/issues/1991)) ([18d76e4](https://github.com/athensresearch/athens/commit/18d76e4f11426e596e4d8c0473cf4a1965068d3d))


### Enhancements

* **help-popup:** Use more general term for search shortcut ([312b1ce](https://github.com/athensresearch/athens/commit/312b1cea1dff1d90a311a12fb25be51b9281e302))


* **deps:** bump engine.io from 4.1.1 to 4.1.2 ([e4cdb87](https://github.com/athensresearch/athens/commit/e4cdb876dc441bd712721ae2117bbc99a827234c))
* **deps:** bump follow-redirects from 1.14.1 to 1.14.7 ([d11e39c](https://github.com/athensresearch/athens/commit/d11e39ca8a27139560812b1f35af056c5f0e34e1))
* **deps:** bump log4js from 6.3.0 to 6.4.1 ([a4153bb](https://github.com/athensresearch/athens/commit/a4153bb89815e32484718c797e283f69f01bbb69))
* **deps:** bump node-fetch from 2.6.1 to 2.6.7 ([f257800](https://github.com/athensresearch/athens/commit/f2578005779b2f2262c6e718b102c0004944d23c))
* **deps:** bump trim-off-newlines from 1.0.1 to 1.0.3 ([fa7f247](https://github.com/athensresearch/athens/commit/fa7f247a1320677dd215fe2ee57c27f61e305437))
* remove marked dependency ([#1997](https://github.com/athensresearch/athens/issues/1997)) ([4558bc2](https://github.com/athensresearch/athens/commit/4558bc20039ce1b00e86bff974d82e4af6686efe))

## [2.0.0-beta.12](https://github.com/athensresearch/athens/compare/v2.0.0-beta.11...v2.0.0-beta.12) (2022-01-20)


### Features

* add :page/merge undo ([dce25c5](https://github.com/athensresearch/athens/commit/dce25c5bd24a20be1a34479ac91926d4d1dc1dd6))
* add order DSL ([5dd40dc](https://github.com/athensresearch/athens/commit/5dd40dc96353c37673bbcefd5dfc617a8287c030))
* build-paste-op returns a flat list of atomic ops ([15f54dc](https://github.com/athensresearch/athens/commit/15f54dccf64d5646b407aa78cb0de55f037bbce4))
* build-undo-event with support for composite ([90390d8](https://github.com/athensresearch/athens/commit/90390d886ded4fb6f3911a16f8a19685ebe07aed))
* group block save at the end of paste-op ([dc92885](https://github.com/athensresearch/athens/commit/dc92885c96088d1cfc6ddb29c2d5404d30010f85))
* redo testing of `:block/new` and `:block/remove` ([006d5dc](https://github.com/athensresearch/athens/commit/006d5dc32e2d39e3f00d5d1d4ad85bb8c12616ec))
* restore shortcut on undo page/remove and page/merge ([7471f90](https://github.com/athensresearch/athens/commit/7471f905569a4489cb48f4064c659e6b1a3a8bc1))
* support :block/remove in undo ([d105f6d](https://github.com/athensresearch/athens/commit/d105f6d3171bcb63f9db535fd3f4f0faa024f276))
* support undo/redo for lan party ([a0682d6](https://github.com/athensresearch/athens/commit/a0682d6dcfa328a3762772baa736f9c87a1f481f))
* undo `:block/new` operation with initial tests. ([dabd498](https://github.com/athensresearch/athens/commit/dabd4982663ad9317b4cad9877cdd4bb0170cf02))
* Undo `:page/rename` ([a4f63bd](https://github.com/athensresearch/athens/commit/a4f63bdda96d661a07c62d3d7bfb03614e804ffb))
* undo composite ([2bb5ff3](https://github.com/athensresearch/athens/commit/2bb5ff3090c5e047c6763e437e6cabd499e024db))
* undo composite improvements ([f381702](https://github.com/athensresearch/athens/commit/f381702a0182adf52b214ea4226662becee62eed))
* undo for block/open ([a4385d2](https://github.com/athensresearch/athens/commit/a4385d2db1f065764cd01d8b641a8df2ad35e398))
* undo resolver for :block/save ([e3c643e](https://github.com/athensresearch/athens/commit/e3c643e248f92eb05e8a8cc8cbf4fd82f0689941))


### Bug Fixes

* don't return repr with uid for title or open true ([99ac570](https://github.com/athensresearch/athens/commit/99ac570f92a63c3f20d006dded4ff70cef36b3a8))
* more places that need to account for :block/open? default value ([9f3234f](https://github.com/athensresearch/athens/commit/9f3234fde49f34446a6a77a1477e85f44a16c43e))
* resolver for composite ops was not matching vector return format ([38d8429](https://github.com/athensresearch/athens/commit/38d842959e6f3eb0bd65f402c6f50fbf5b043d2f))
* support undoing contiguous moves ([acaef19](https://github.com/athensresearch/athens/commit/acaef19128a899a79223aa4645f81602555d0628))
* undo-op should receive current and op dbs ([33d0b78](https://github.com/athensresearch/athens/commit/33d0b78bed10d7b6d46500cc5228a6c987f402c1))
* use atomic op in undo test ([0572702](https://github.com/athensresearch/athens/commit/0572702cca07792cb00cb431bb18725c4f293b43))


### Work in Progress

* refactor functions; stuck on why indices aren't updating ([4b601cf](https://github.com/athensresearch/athens/commit/4b601cf8704f5b96a2789081d5f80a03e9b15caa))


### Documentation

* add docstrings to athens.common-events.resolver.order ([60bc879](https://github.com/athensresearch/athens/commit/60bc879ccb2e196b418f62c7640ce92db929d897))
* add note about impl source ([0367287](https://github.com/athensresearch/athens/commit/036728747fb79c4bca013480556d0cbbc3416e13))


### Refactors

* add common undo ops to fixture ns ([d7632de](https://github.com/athensresearch/athens/commit/d7632de0bb3600c23a958bf91c622c126ff88d28))
* add get-position helper ([f2c4aaf](https://github.com/athensresearch/athens/commit/f2c4aafc5e0ae9f89ce4d54b2de81ab992181d91))
* cleanup for review ([238a1b3](https://github.com/athensresearch/athens/commit/238a1b3bf50697ac694a96f3b2e5f87bea9199d6))
* don't warn on compat-position ([f3ff90b](https://github.com/athensresearch/athens/commit/f3ff90b62386a81c03321af6e5108dc8e46391fb))
* review items from jeff ([1351d58](https://github.com/athensresearch/athens/commit/1351d589b4209fdd840bf906c79264a72a456211))
* undo resolver returns vector of ops ([07af7cb](https://github.com/athensresearch/athens/commit/07af7cb66c341249e6efed20c500c5d1d9aaef34))
* use backrefs instead of _refs ([2a71bd6](https://github.com/athensresearch/athens/commit/2a71bd6007a475a353fc04e8fa57e3bba1357e28))
* use kw as fn ([e14d6d6](https://github.com/athensresearch/athens/commit/e14d6d663ba4a347b5ca910a07f344f1fbce6516))
* use order in block/move ([b00399e](https://github.com/athensresearch/athens/commit/b00399e20878194313ceaf56f9c1b8f1ed578416))
* use order in block/new ([be4af63](https://github.com/athensresearch/athens/commit/be4af63e15ae7ed618c363b5943269abfd918716))
* use order in block/remove ([72aeded](https://github.com/athensresearch/athens/commit/72aeded89f5a8542c73b3ea8229d014c0b696cee))
* use order in page/merge ([3993184](https://github.com/athensresearch/athens/commit/39931841d376efdb75f4ed5a2ae49a056c0c369b))
* use order in shortcut/* ([8c49cc7](https://github.com/athensresearch/athens/commit/8c49cc7d772779301b55cf04a75d6dfde4081ecb))


* add compat-position tests ([79333a8](https://github.com/athensresearch/athens/commit/79333a8206c2b36d3f4b1a57d3121e0b93abfee9))
* add tests for get-internal-representation ([6496ab5](https://github.com/athensresearch/athens/commit/6496ab57130017368ee68ea831e435b3d18ffe1c))
* carve ([eef7ce3](https://github.com/athensresearch/athens/commit/eef7ce3240095fb02799d7b7405f3dbc9b6f7018))
* comment not needed anymore ([8f3ff9f](https://github.com/athensresearch/athens/commit/8f3ff9f77e26fe25494af349fe669a8c45ddec1d))
* Commented out redo tests until `:block/remove` is reversable ([562fb38](https://github.com/athensresearch/athens/commit/562fb380d6c5399fa9945a69a8e38d4c57b60ba4))
* enable composite-of-composites-undo test ([f14f1bb](https://github.com/athensresearch/athens/commit/f14f1bb8735548cced2e16ca73ad1af19a1f58c1))
* more test cases. ([335e10e](https://github.com/athensresearch/athens/commit/335e10ef875a1d7606c3502bbe98391a49f3de92))
* style happy now ([c86cdb7](https://github.com/athensresearch/athens/commit/c86cdb7719bf14b1d5d97b7529ee0335e08d8122))

## [2.0.0-beta.11](https://github.com/athensresearch/athens/compare/v2.0.0-beta.10...v2.0.0-beta.11) (2022-01-19)


### Bug Fixes

* offsetTop error from navigating to dailynotes ([debbdca](https://github.com/athensresearch/athens/commit/debbdcae078c1aec418c3c42188ef43d209bf7eb))


### Enhancements

* **db-picker:** allow users to remove dbs from list ([7ad8c27](https://github.com/athensresearch/athens/commit/7ad8c27090943382768e14987b1e7fab466a2d63))
* **presence:** move inline avatars to the right ([9fa2d23](https://github.com/athensresearch/athens/commit/9fa2d230b88eadd16e0c8e3b247f0ebde547592d))

## [2.0.0-beta.10](https://github.com/athensresearch/athens/compare/v2.0.0-beta.9...v2.0.0-beta.10) (2022-01-13)


### Enhancements

* stop using restore-navigation until more reliable ([81f0d93](https://github.com/athensresearch/athens/commit/81f0d93eb931e307bfc5a98c2329f08ac83d633d))

## [2.0.0-beta.9](https://github.com/athensresearch/athens/compare/v2.0.0-beta.8...v2.0.0-beta.9) (2022-01-10)


### Bug Fixes

* `Meta+k` didn't work on Linux ([8dca24c](https://github.com/athensresearch/athens/commit/8dca24ce113eb748dab7feae5ce85b61535a4e9b))
* delete and merge not preserving children ([ed0ab98](https://github.com/athensresearch/athens/commit/ed0ab9853574ed29e70281e109d0f4734eb3d830))


* e2e utils and a cleaner tests for "delete doesn't merge" ([123ed7f](https://github.com/athensresearch/athens/commit/123ed7f79a4d4d456e7a1629aea00e6a876ec5fe))
* rename test from template to meaningful name ([47bf1e7](https://github.com/athensresearch/athens/commit/47bf1e75d34c6546f0ea27d9577223456fec9bd4))
* supposingly failing e2e test. ([c7c323e](https://github.com/athensresearch/athens/commit/c7c323eff39e2fdb08dd07b68d3dea2fc20f2119))


### Documentation

* add ADR for undo/redo ([1bc46ee](https://github.com/athensresearch/athens/commit/1bc46eeec70e5489325522f6ecf0d3a513da0017))

## [2.0.0-beta.8](https://github.com/athensresearch/athens/compare/v2.0.0-beta.7...v2.0.0-beta.8) (2022-01-05)


### Enhancements

* add help manual for anchor text links ([1b1eb85](https://github.com/athensresearch/athens/commit/1b1eb85bb086d19a98f1ceb18906cba929e82c0b))


* don't save db to disk on e2e ([6a8a515](https://github.com/athensresearch/athens/commit/6a8a515e6d4bc8ddd07de84b10103b374c197387))
* update nvmrc to use 16 (active LTS) ([ea93367](https://github.com/athensresearch/athens/commit/ea93367bbeb02f7b7ccda5a4a8b9027342313113))

## [2.0.0-beta.7](https://github.com/athensresearch/athens/compare/v2.0.0-beta.6...v2.0.0-beta.7) (2022-01-05)


### Features

* add new e2e test script ([741df9b](https://github.com/athensresearch/athens/commit/741df9b89005e310da1194ad52c462465680639b))


### Bug Fixes

* don't flicker old state on save ([14bf8f7](https://github.com/athensresearch/athens/commit/14bf8f7d77e5e887ffb9188624af23e6dcb035fe))
* idling for 2s on a block with changes will save it ([21c6a8b](https://github.com/athensresearch/athens/commit/21c6a8b7a337738e76cb8629e24efe4c4ef0ca07))


### Refactors

* rename basic spec file ([a86c4b5](https://github.com/athensresearch/athens/commit/a86c4b56c6955555949911dd28fd9e716a6207fc))

## [2.0.0-beta.6](https://github.com/athensresearch/athens/compare/v2.0.0-beta.5...v2.0.0-beta.6) (2022-01-03)


### Features

* render page link and block ref aliases. ([4dca7db](https://github.com/athensresearch/athens/commit/4dca7db460a698d374320d5f5e18a1cc35ff0ed5))
* resolve-transact! can also transact without middleware ([963a9bd](https://github.com/athensresearch/athens/commit/963a9bd8d0b915e5fc10d234be49debe1ebf9202))
* resolve-transact! returns the transacted datoms ([0f7fcd1](https://github.com/athensresearch/athens/commit/0f7fcd18fcf429389d49c003b8fe226f40160caf))
* titled page links & block refs. ([b01158c](https://github.com/athensresearch/athens/commit/b01158c0d90c14a056654d28f4b7491431a139f3))
* use undo instead of reset for rollback ([686f82c](https://github.com/athensresearch/athens/commit/686f82cad09a6409879f94143f405f0e4deb4b58))


### Bug Fixes

* another uuid conversion ([44e7afc](https://github.com/athensresearch/athens/commit/44e7afcc74c429a8ade0cb1c8130fcdcb42eada6))
* apply-new-server-event to remote/update-optimistic-state everywhere ([9827d81](https://github.com/athensresearch/athens/commit/9827d81f5163e5b3b4213093556d6f91b06d2048))
* atomically update optimistic state ([f56b7de](https://github.com/athensresearch/athens/commit/f56b7de26a2ff496e392cbacbc498a677494dec2))
* dedicated fn to convert uuid to str ([0ad22b2](https://github.com/athensresearch/athens/commit/0ad22b2dae5f45d3ce98ff7cdc9973400f112dd3))
* don't save tx-data info for forwarded events ([f47f577](https://github.com/athensresearch/athens/commit/f47f57719d203ed392c8e2e086a59e3fa9896c83))
* optimistic operations need to be atomic ([e4194ee](https://github.com/athensresearch/athens/commit/e4194ee4686f382a16512d57cac1964a7384b076))
* pin fluree ledger to 1.0.0-beta17 ([cfdfe01](https://github.com/athensresearch/athens/commit/cfdfe01652f67ad175ce42a109310a5284a96d3e))
* reverse the buttons on the closing warning ([87bb442](https://github.com/athensresearch/athens/commit/87bb442342a379ff7a93766330a09f1311dcd4fc))
* rollback should transact the rollback-tx, not the original again ([5ac431e](https://github.com/athensresearch/athens/commit/5ac431e8c47d3184eea8edb9c6ce5cb26aebeb85))
* rollback warning was not comparing the right things ([839c7fe](https://github.com/athensresearch/athens/commit/839c7fe9be0a8eb28d0d47c6e44740f74f05587e))
* show mismatched ids in rollback warning ([40334f5](https://github.com/athensresearch/athens/commit/40334f504e97a3ef06b4b9bc7346168203c728bd))
* youtube embeds. ([a224795](https://github.com/athensresearch/athens/commit/a224795ae4544f2bbdc77b1fecd4a6b57fef24fd))


### Work in Progress

* debug on ci ([22b07e7](https://github.com/athensresearch/athens/commit/22b07e7b9e274244bdb29d973c2f39cf4fff65b0))


* add client e2e tests ([9c1397f](https://github.com/athensresearch/athens/commit/9c1397f7caa3ffb7008ddcc8a8c27fd165e02cb0))
* less logging ([a3249ca](https://github.com/athensresearch/athens/commit/a3249ca812126f0397ece8cf24f5fdb861d37953))
* update datascript ([4742a57](https://github.com/athensresearch/athens/commit/4742a57d4bfcaaf800c7180f3f35873daa54b04f))
* workaround playwright electron close on CI ([eed40ee](https://github.com/athensresearch/athens/commit/eed40ee014c3ad86d358215ca7d213a44152c636))


### Refactors

* add debug logs to optimistic rollback ([1e9f685](https://github.com/athensresearch/athens/commit/1e9f6859bfa3fb551a0793d209d5a9ae7e6539d1))
* add log line before blocking fluree call ([878be13](https://github.com/athensresearch/athens/commit/878be13c8cfcbbf4161efd65cf833d4339e384d6))
* add note about renaming event-sync ([db0bf63](https://github.com/athensresearch/athens/commit/db0bf63ac4c619b51a0b5f3f64696112ecca87f7))

## [2.0.0-beta.5](https://github.com/athensresearch/athens/compare/v2.0.0-beta.4...v2.0.0-beta.5) (2021-12-16)


### Features

* add log recovery to athens cli ([705b735](https://github.com/athensresearch/athens/commit/705b73592545cf2aa1e307f480b7ae631d42d938))
* add recovery fns to athens.self-hosted.event-log ([34bc91e](https://github.com/athensresearch/athens/commit/34bc91e29d8dfccc81c06c976ec6c7f9a3150826))
* deploy notebooks to vercel ([45c7f59](https://github.com/athensresearch/athens/commit/45c7f59a0b3ae62230ef06f59b0540013a59a52a))
* use backoff during load process ([fb9f968](https://github.com/athensresearch/athens/commit/fb9f968a5084701ec776c2182ba3ad8767a76fbe))


### Bug Fixes

* cli:compile build script had an extra single quote ([d1b2af1](https://github.com/athensresearch/athens/commit/d1b2af111b77d5a0ccef84072e4a980dea5a8206))
* cli:uberjar needs to take in the compiled classes ([3bca14e](https://github.com/athensresearch/athens/commit/3bca14e29a3182dcf56e5c2b5e13cad3224a8920))
* current-route for pages now uses title ([2b8dd64](https://github.com/athensresearch/athens/commit/2b8dd64fae2c17f3401a057d2ac3f7d3a4dc89ac))
* deleting the page on the right sidebar ([446851a](https://github.com/athensresearch/athens/commit/446851ab3d7f3dec7cbe9dcbde52fe203cf11c18)), closes [#1350](https://github.com/athensresearch/athens/issues/1350)
* don't track side effects from mapped fns ([0c34e4d](https://github.com/athensresearch/athens/commit/0c34e4d5c3f5b09d8ed3521f87041112f3f2338d))
* Fix the issue with merging blocks using the delete key ([ffadff4](https://github.com/athensresearch/athens/commit/ffadff408f36a5cb4ff4da73fea877ab6ec68422))
* re-order conditions and add comments ([a6a29c3](https://github.com/athensresearch/athens/commit/a6a29c309b53d5009e537a10265b3c7ef9aea3d5))
* structure parser not allowing empty page links ([9c998bb](https://github.com/athensresearch/athens/commit/9c998bbb07b58f0fe67977cdf15d9f048af994cf))
* support updating on block remove and merge. ([5677ac6](https://github.com/athensresearch/athens/commit/5677ac6fe269971083cf56f22ada1e7589e9c2fd))
* take into account local edit state when merge-delete. ([edb8fb0](https://github.com/athensresearch/athens/commit/edb8fb0b2e6aea5df7de7366925199478c6cf297))
* variable naming ([8917815](https://github.com/athensresearch/athens/commit/8917815bf08c07afba37771cc1bc7c9978e956d4))


### Refactors

* move rename and test lazy-cat-while ([4d67342](https://github.com/athensresearch/athens/commit/4d67342e076c7e30b322ca927721742c70a6ec4d))


* add script to clean fluree data ([7ad0374](https://github.com/athensresearch/athens/commit/7ad0374c5376eafbf76ac10fdf4d347d5f800a86))
* **deps-dev:** bump electron from 12.0.9 to 12.1.0 ([aabbedc](https://github.com/athensresearch/athens/commit/aabbedc3d683be0e0daa5e88f7195077049c152d))
* **deps:** bump nth-check from 2.0.0 to 2.0.1 ([3951102](https://github.com/athensresearch/athens/commit/39511023a7c1a82c8655f2dc0c6820aef6463c6d))
* **deps:** bump tmpl from 1.0.4 to 1.0.5 ([e400529](https://github.com/athensresearch/athens/commit/e400529fe2ed2cd302195d963d702713d395bfdb))
* don't install :npm-deps from clojure deps. ([2262f9f](https://github.com/athensresearch/athens/commit/2262f9f9856cbfd82a340ac70007df0ad6fe8feb)), closes [/github.com/thheller/shadow-cljs/issues/800#issuecomment-725716087](https://github.com/athensresearch//github.com/thheller/shadow-cljs/issues/800/issues/issuecomment-725716087)
* install dependencies and build on vercel ([#1901](https://github.com/athensresearch/athens/issues/1901)) ([b69e21b](https://github.com/athensresearch/athens/commit/b69e21b094e4bda06fe6a6b8329a038dd654c68c))
* remove unused ns ([7db4c5e](https://github.com/athensresearch/athens/commit/7db4c5e88570a590c64614a5e67eb3db018295d1))
* rename fluree clean to wipe ([37f58aa](https://github.com/athensresearch/athens/commit/37f58aa96ee63ff02e33301fcc7c50bad0c0f8a3))
* update fluree/db deps ([19fcbca](https://github.com/athensresearch/athens/commit/19fcbcae7115acc19f008cd17b1ae4fac2ae9732))
* use checkout@v2 ([1b8f6bb](https://github.com/athensresearch/athens/commit/1b8f6bb9cbfbdf5ba3b851ea72c57d593b90c43a))
* use latest electro v12.* ([90c3455](https://github.com/athensresearch/athens/commit/90c3455db29b30fc86274ba21ecfcde7393dc612))

## [2.0.0-beta.4](https://github.com/athensresearch/athens/compare/v2.0.0-beta.3...v2.0.0-beta.4) (2021-12-09)


### Features

* templates ([07733fc](https://github.com/athensresearch/athens/commit/07733fc42d9011633b064f4f538628cd266d0bff))


### Bug Fixes

* don't add classes to default paths ([39cb81d](https://github.com/athensresearch/athens/commit/39cb81d17ead2745ede257afcc6c6bb41b66af68))
* handle big blocks ([599cf6a](https://github.com/athensresearch/athens/commit/599cf6a72c205287d161a4ee726f82856f8a1f71))
* make clerk start ([d8cfd8c](https://github.com/athensresearch/athens/commit/d8cfd8cc6fb9d7743db9ac365d09797574f6c011))
* make it save and exit. ([9c840b4](https://github.com/athensresearch/athens/commit/9c840b476d39d5eb54f04dcf6b6b850250d185b8))


### Documentation

* add rollback optimizations ADR proposal ([bbe75cb](https://github.com/athensresearch/athens/commit/bbe75cb680ab6711e44444dcf5478b1af791e785))


### Work in Progress

* Structure parser ([e1cac48](https://github.com/athensresearch/athens/commit/e1cac48438f5934b0d06c067609fac6f2aebf99c))
* structure parser with typed-refs (just for `embed`) ([3525637](https://github.com/athensresearch/athens/commit/3525637e7d46eb5ab547135c11513cdeafd1d8f5))


* ignore the notebooks build file, but watch others there ([b0197ed](https://github.com/athensresearch/athens/commit/b0197ed82d10381f86c319dc5a79490ad1c2526d))
* style happy ([27490e4](https://github.com/athensresearch/athens/commit/27490e4edf225a3c5dcba0e539a2f6a5b5185c48))
* support clerk notebooks ([7253a49](https://github.com/athensresearch/athens/commit/7253a4925ec9f0619fe6971ab5532aedd78220ae))
* use uberdeps to create parametrized executable uberjars ([e4bcce6](https://github.com/athensresearch/athens/commit/e4bcce653c707462ad3ec347c376e059aa9160d1))


### Refactors

* move macro to utils. ([3f91ffb](https://github.com/athensresearch/athens/commit/3f91ffb42dd058390a925915d526cc6a6913af91))

## [2.0.0-beta.3](https://github.com/athensresearch/athens/compare/v2.0.0-beta.2...v2.0.0-beta.3) (2021-11-29)


### Bug Fixes

* remove nils from inline presence too ([bdb8937](https://github.com/athensresearch/athens/commit/bdb8937dbff54273fdc957aa1f4fb681ca331db0))

## [2.0.0-beta.2](https://github.com/athensresearch/athens/compare/v2.0.0-beta.1...v2.0.0-beta.2) (2021-11-26)


### Bug Fixes

* use same semantics for event replay as processing. ([c301a38](https://github.com/athensresearch/athens/commit/c301a388a527e01f4d1c986a61cbeefb4eb5213b))


### Refactors

* rename duplicate ADR 10 to 16 ([feff0c3](https://github.com/athensresearch/athens/commit/feff0c3be7a6f69861edf06c2a4d16bf4baf6d60))


### Documentation

* expand and rename event log adr ([23797ab](https://github.com/athensresearch/athens/commit/23797ab04611aed0bee2b56e1729b94c1f2c7414))

## [2.0.0-beta.1](https://github.com/athensresearch/athens/compare/v2.0.0-beta.1...v2.0.0-beta.1) (2021-11-25)


### Bug Fixes

* run health check on client as well ([9161df0](https://github.com/athensresearch/athens/commit/9161df0814b5373452d285addfea881cc72f79cd))


### Documentation

* updates for 2.0.0-beta launch ([#1868](https://github.com/athensresearch/athens/issues/1868)) ([87b1e82](https://github.com/athensresearch/athens/commit/87b1e82df539ccf1c756481b67b36f273d5ca243))

## [1.0.0-alpha.rtc.46](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.45...v1.0.0-alpha.rtc.46) (2021-11-21)


### Bug Fixes

* this docker images was to skinny ([a209a28](https://github.com/athensresearch/athens/commit/a209a28c3e0a091a70e1ac18f7cb0c39b0fbc13e))

## [1.0.0-alpha.rtc.45](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.44...v1.0.0-alpha.rtc.45) (2021-11-21)


* added comment on why we need QEMU. ([a742df4](https://github.com/athensresearch/athens/commit/a742df4c691a04d04f8987b16745c55db495f6bb))
* linux-arm64 target supported and smaller images ([ccb51a7](https://github.com/athensresearch/athens/commit/ccb51a7b6e1a99665e741d8869fb415c05b55c57))

## [1.0.0-alpha.rtc.44](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.43...v1.0.0-alpha.rtc.44) (2021-11-19)


### Features

* handle errors and reconnect on add-event! ([e582c70](https://github.com/athensresearch/athens/commit/e582c70688e5c3fa01df9d445fb979c9d9214d3a))


### Bug Fixes

* don't print whole remote-db config ([25a5cd2](https://github.com/athensresearch/athens/commit/25a5cd294526b0bbb4f9274cf414091861aff4ec))
* save event to log inside the same lock as db transaction ([746a2ae](https://github.com/athensresearch/athens/commit/746a2ae945014cee6050d7b82aebfb58f3e1d60d))
* timeout on health check ([c1bc1f4](https://github.com/athensresearch/athens/commit/c1bc1f4ce06a555340d03f358f0f4ec2b9bebc60))
* use new fluree config in defrecord ([9dc5796](https://github.com/athensresearch/athens/commit/9dc57961d63628bba4a764acfbc8d07552eebf37))


* add fluree dev script ([f370c44](https://github.com/athensresearch/athens/commit/f370c44b436a5257ce301d822b816c44ddd3d023))

## [1.0.0-alpha.rtc.43](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.42...v1.0.0-alpha.rtc.43) (2021-11-18)

## [1.0.0-alpha.rtc.42](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.41...v1.0.0-alpha.rtc.42) (2021-11-18)


### Bug Fixes

* re-enable copy paste ([5936131](https://github.com/athensresearch/athens/commit/5936131bd40531cbeeab37cd899bf50722b20495))

## [1.0.0-alpha.rtc.41](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.40...v1.0.0-alpha.rtc.41) (2021-11-18)


### Performance

* don't reset conn if they are the same ([01b9b03](https://github.com/athensresearch/athens/commit/01b9b03e65695acfb28c3056111dc3911b2b073b))

## [1.0.0](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.38...v1.0.0) (2021-11-12)


* disallow auto-update to pre-release versions ([af82684](https://github.com/athensresearch/athens/commit/af826845364be2d8cf05618cc06e6fdd86f0e634))
* remove in-app update setting ([3ed9ee1](https://github.com/athensresearch/athens/commit/3ed9ee1a940209a40d5cc865a6809e8c721c98f0)), closes [/github.com/athensresearch/athens/pull/1803#discussion_r745655213](https://github.com/athensresearch//github.com/athensresearch/athens/pull/1803/issues/discussion_r745655213)

## [1.0.0-alpha.rtc.40](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.39...v1.0.0-alpha.rtc.40) (2021-11-18)


### Features

* add icon example to button sotry ([85f3e3d](https://github.com/athensresearch/athens/commit/85f3e3d9e6457a780c715170ce71004ddaf0045d))
* fill out welcome components ([62919c8](https://github.com/athensresearch/athens/commit/62919c8a79653b3a7dcd7fe819fe121830ab6121))


### Bug Fixes

* :block/remove should change titles too ([71e6751](https://github.com/athensresearch/athens/commit/71e6751fd842aa923b1af45f1e3dd7e012671dc2))
* better logging. ([5fedd23](https://github.com/athensresearch/athens/commit/5fedd2363b6bcd61b837e8c2500b0c92be0a77b3))
* **dbicon:** proper margins for db icon in toolbar ([5b46e28](https://github.com/athensresearch/athens/commit/5b46e28f8658309dc3cc4ba4e297c2ed6fdb53e1))
* welcome buttons properly styled ([75f1f90](https://github.com/athensresearch/athens/commit/75f1f90ea2ef44b4281e680a1a1f61cd31dfd256))
* **welcome:** consistent language for database ([aac0798](https://github.com/athensresearch/athens/commit/aac0798e4adf21396dc9796a0d5ed215706e75af))


### Refactors

* **button:** reimplement button content spacing ([6f73fdc](https://github.com/athensresearch/athens/commit/6f73fdc2403bd3a5bd823bfddc4d3833dee842e6))
* remove semantic events support from server ([7e2ad7d](https://github.com/athensresearch/athens/commit/7e2ad7de07517e61b185cb0689e6b3bdb567cafb))


### Work in Progress

* cleaner logging ([759270a](https://github.com/athensresearch/athens/commit/759270a6537736f63c5c6ac42e088006415d3a39))
* logging cleanup round 1. ([e2cbdea](https://github.com/athensresearch/athens/commit/e2cbdeab023a4025524e5ae43a2320e3643d2f0a))
* things and stuff removed ([7c282e8](https://github.com/athensresearch/athens/commit/7c282e8f2c90f76d644a1e6d31ac57a5b72a9bb4))


* add source maps and pseudo names to electron build ([9bc5c26](https://github.com/athensresearch/athens/commit/9bc5c266950d3ebf989c5155250b0e22b5bb81fd))
* log size of memory-log when replaying. ([ac63e87](https://github.com/athensresearch/athens/commit/ac63e87cff0ef860a34b72b1cd6daef29c94d619))
* style happier ([ff728aa](https://github.com/athensresearch/athens/commit/ff728aafa5c681d9d29936c67c46f6ac853be594))
* yarn clean should remove all .js and .js.map, and be crossplatform ([067f64c](https://github.com/athensresearch/athens/commit/067f64c056af4742c58aa008b8dd8cd07410dcbb))

## [1.0.0-alpha.rtc.39](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.38...v1.0.0-alpha.rtc.39) (2021-11-16)


### Features

* **help-popup:** Add help popup with content. ([178eb09](https://github.com/athensresearch/athens/commit/178eb0954f15ad6a66ab7fa4c293d75017b70cad))
* **help-popup:** Add tooltip to help icon ([c3e1d0b](https://github.com/athensresearch/athens/commit/c3e1d0b5885466c77550e3739d8ce8f5b2f32bd4))
* **help-popup:** Comment out help links until we have ([fcc6c4c](https://github.com/athensresearch/athens/commit/fcc6c4cf416ffb491a5800c0a9edae9c1e0d609d))
* **help-popup:** Fix a few shortcuts ([c96ace5](https://github.com/athensresearch/athens/commit/c96ace58d50f46fa5eb0386d12c86c7edea0f3a9))
* **help-popup:** Fix border mismatches ([17b1f45](https://github.com/athensresearch/athens/commit/17b1f45cffcfa777b7b1d7b1e2880b9d7cbafe26))
* **help-popup:** Fix closing of modal using escape. ([1e6c8f8](https://github.com/athensresearch/athens/commit/1e6c8f8e41dcabffe1a08fab5830e813c44ec605))
* **help-popup:** Fix copy and remove  a shortcut that ([c706c89](https://github.com/athensresearch/athens/commit/c706c89fc4c9ba5761fb9ec44085f4a0a696d4dd))
* **help-popup:** Fix issue with scroll-bar and move ([5b27673](https://github.com/athensresearch/athens/commit/5b27673f207d2458cce81c7e57aa4299cb39a9af))
* **help-popup:** Fix scroll behaviour ([3f77421](https://github.com/athensresearch/athens/commit/3f774210affadf7b01a2ec8fb672708f1b4122bf))
* **help-popup:** Fix spacing ([6924941](https://github.com/athensresearch/athens/commit/6924941a5a41a90564b7c69f0226f6913325ddf4))
* **help-popup:** Remove "code block" from Help. ([55042ae](https://github.com/athensresearch/athens/commit/55042ae560a143a3bf7049cc03601644158619d0))
* notify user of page being removed from beneath. ([bbb0bd9](https://github.com/athensresearch/athens/commit/bbb0bd98f12fc301afe064ec497b15d2d3c9fea6))
* use athens protocol for initial events in local ([1d3ba34](https://github.com/athensresearch/athens/commit/1d3ba34029444b0fe5c5f9f2db4d74d5f9d55f0c))


### Bug Fixes

* all the warnings and errors in help POPUP ([be59fe2](https://github.com/athensresearch/athens/commit/be59fe26984e1573949805c5094649b71076af21))
* better current page title detection ([2c10096](https://github.com/athensresearch/athens/commit/2c1009612ccf3f9f28a5d492502d55a6b6f41f78))
* don't delete dbs from disk in db-picker ([0ce8eee](https://github.com/athensresearch/athens/commit/0ce8eee4c104b62745ee885a8cff282ad92d120a))
* editing/target should delegate to editing/uid ([179bc64](https://github.com/athensresearch/athens/commit/179bc64a9b38ae6264c1beca83fa03a3eb9a2a60))
* embeds show presence as well as the original block ([679c1e1](https://github.com/athensresearch/athens/commit/679c1e1cf13abdb0cfd44e386d571e6ebbfe4ee9))
* enable `shortcut/move` atomic op on server. ([33ab6b7](https://github.com/athensresearch/athens/commit/33ab6b7940a566d728fd9cf7cd4e6d73549846bf))
* go to default db after connection failure ([d503b28](https://github.com/athensresearch/athens/commit/d503b287f43daee6cc83040a7f40cc6676ee5a3f))
* group-title is now nested in a vector ([76d2b05](https://github.com/athensresearch/athens/commit/76d2b054b842d74eddc39c3f005e4d60af986b0a))
* health-check server before connecting to websocket ([8bd701c](https://github.com/athensresearch/athens/commit/8bd701cf999f220953623e67975af513432bf880))
* locally determine old string for do nothing check ([e996564](https://github.com/athensresearch/athens/commit/e996564a3f70b450420737e4a6957c06f2d5a163))
* remove block/order from IR ([ae50ec5](https://github.com/athensresearch/athens/commit/ae50ec5321387a49fce1f765efad59ecefec91dc))
* we this argument `page/title` not `name` anymore ([fabe834](https://github.com/athensresearch/athens/commit/fabe8349e3d57ebf7d608806d832a24e3464300e))


### Work in Progress

* help-popup ([ec3c304](https://github.com/athensresearch/athens/commit/ec3c30425225d8558d531cecedbb6d251ea1dbb3))


### Documentation

* add digitalocean instruction and better permissions instructions ([8f1a793](https://github.com/athensresearch/athens/commit/8f1a793e954c49c0cae2cb341b28ce2cc6e28f25))
* Fix links to Athens Research Blog ([#1666](https://github.com/athensresearch/athens/issues/1666)) ([f7fccce](https://github.com/athensresearch/athens/commit/f7fccce389eab664a3a815d1627b976ad2e5a7d0))


* auto update all builds except on v2 ([fa7d7c9](https://github.com/athensresearch/athens/commit/fa7d7c97d60442f0631ace1bae196f8c15f92949))
* style, lint, carve ([d686d5e](https://github.com/athensresearch/athens/commit/d686d5ead9e38f49eafe51bd2104dd27b326503e))


### Refactors

* reduce log noise on non-verbose settings ([9ddf93e](https://github.com/athensresearch/athens/commit/9ddf93e84812adf7f62238adb4557a78c2b03220))
* review athens protocol ([dd64548](https://github.com/athensresearch/athens/commit/dd6454891976dbfa7d7fba6dfdba68ab9629322f))
* review internal representation names ([909ad57](https://github.com/athensresearch/athens/commit/909ad57364872c892b59498a05f2b06838a7e310))
* style happy ([e8135ed](https://github.com/athensresearch/athens/commit/e8135ed111dc457a9b4712dae770d819b1fcb310))
* style happy. ([b0cf925](https://github.com/athensresearch/athens/commit/b0cf925037abfe272504b43120cef3c60623e319))
* use button tooltip instead ([a2c36b7](https://github.com/athensresearch/athens/commit/a2c36b776a3fbd11fcee19623a3d9f8902defcd0))

## [1.0.0-alpha.rtc.38](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.37...v1.0.0-alpha.rtc.38) (2021-11-11)

## [1.0.0-alpha.rtc.37](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.36...v1.0.0-alpha.rtc.37) (2021-11-11)

## [1.0.0-alpha.rtc.36](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.35...v1.0.0-alpha.rtc.36) (2021-11-11)


### Bug Fixes

* shortcut op is new, not add ([13ccb04](https://github.com/athensresearch/athens/commit/13ccb042b47609d007b4c73df0d26c575cc67e47))

## [1.0.0-alpha.rtc.35](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.34...v1.0.0-alpha.rtc.35) (2021-11-11)


### Bug Fixes

* re-enable `:page/merge` ([a5a3b76](https://github.com/athensresearch/athens/commit/a5a3b76fd9d8d18ce63710ea712f03eb89a25dc9))

## [1.0.0-alpha.rtc.34](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.33...v1.0.0-alpha.rtc.34) (2021-11-11)


### Features

* `:page/delete` is atomic. ([1b00805](https://github.com/athensresearch/athens/commit/1b00805022eb5e4ad71c5ea6af07762fec7af460))
* `:page/merge` is atomic, so atomic. ([8f09340](https://github.com/athensresearch/athens/commit/8f09340214633f48f8e090991e65f6324219e157))
* add schema for shortcut atomic operations ([95d8d40](https://github.com/athensresearch/athens/commit/95d8d40f3e04601694b912a719ea82fee7e565d1))
* atomic `:page/rename` ([1a41e07](https://github.com/athensresearch/athens/commit/1a41e072819c1d8a83d45da5cc0692a6431a8366))


### Bug Fixes

* `:block/indent` positioning ([13d7f20](https://github.com/athensresearch/athens/commit/13d7f2087caa20beb9efb18edbb9eaa5a5fa4def))
* `:page/merge` throwing exceptions in `orderkeeper` no more. ([bf82419](https://github.com/athensresearch/athens/commit/bf82419731e3fecfed90b07e9e0a3b5e722b96f5))
* enable atomic `:page/rename` on protocol. ([1cb5a0f](https://github.com/athensresearch/athens/commit/1cb5a0f6bd2c4de43ba9f1818e47c7bca3b6e6df))
* make `:page/remove` idempotent. ([e67806b](https://github.com/athensresearch/athens/commit/e67806b7da51450375912ee21304a7e21a504d34))
* these tests should not expect exceptions anymore. ([a123490](https://github.com/athensresearch/athens/commit/a123490d1ca78cb78643edea5399add467b8a5ee))


### Refactors

* use name instead of title to identify pages in protocol ([9bc0e95](https://github.com/athensresearch/athens/commit/9bc0e95a425fc027fa31bb59640ff5babdb8f3ed))
* use startsWith boolean output as string ([c7cb73e](https://github.com/athensresearch/athens/commit/c7cb73eafe9704e659a9fdebe2dea0c26b0b7dc4))


* gh pages, auto-updates only for v1.* ([f07ea50](https://github.com/athensresearch/athens/commit/f07ea503f89febb57180d6964adb77ff56b13d7b))
* remove in-app update setting ([bdd15a8](https://github.com/athensresearch/athens/commit/bdd15a85e5eebf50f2221ab8c28ba0d747179d70)), closes [/github.com/athensresearch/athens/pull/1803#discussion_r745655213](https://github.com/athensresearch//github.com/athensresearch/athens/pull/1803/issues/discussion_r745655213)
* use if condition on composite action ([4d708e5](https://github.com/athensresearch/athens/commit/4d708e53f05d72cb01efd29ac9d5a2d9496913aa))

## [1.0.0-alpha.rtc.33](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.32...v1.0.0-alpha.rtc.33) (2021-11-08)


### Bug Fixes

* page navigation by title including nested cases. ([7e37201](https://github.com/athensresearch/athens/commit/7e3720102b1aa016faf73fd48d08a2343415fa60))
* use right bindings in :block/save event ([e301070](https://github.com/athensresearch/athens/commit/e301070ba0e956cf7a0e8659b7839eb75c82d1e4))


### Refactors

* remove unused require. ([76a9db6](https://github.com/athensresearch/athens/commit/76a9db686885bffdeaf5deefd94a9b7ce496b54f))

## [1.0.0-alpha.rtc.32](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.31...v1.0.0-alpha.rtc.32) (2021-11-08)


### Bug Fixes

* cleaner `page/page-by-title` ([a446a58](https://github.com/athensresearch/athens/commit/a446a5873c69efc7b29b9d204e43497340c050c0))
* don't dispatch nil, reframe does not accept it. ([72a8272](https://github.com/athensresearch/athens/commit/72a8272d2faa0dbe3f6334b943e3168b8fa63479))
* don't infinite loop during `:block/remove` ([e9b5180](https://github.com/athensresearch/athens/commit/e9b518098fcfff146fa0b31d3e34af3447168db0))
* fix wrong refactor ([5dfa574](https://github.com/athensresearch/athens/commit/5dfa5744de9d543d89a836b74b2f25f5be489d81))
* make nested page links work again. ([6e1f0e1](https://github.com/athensresearch/athens/commit/6e1f0e182e56d77a0252e003b2ac57a6717e9fc8))
* style/carve happy ([aa54c78](https://github.com/athensresearch/athens/commit/aa54c78534dd42bc591b1694de5cebc83c9df0b0))
* update own presence optimistically ([94ab5fb](https://github.com/athensresearch/athens/commit/94ab5fb4f1d8670a80b522555e017161fbfe73db))
* use compat-position for child and bump up ([308356c](https://github.com/athensresearch/athens/commit/308356c327728bc017e069881235387ad6460635))


### Enhancements

* more informant 404 page. ([880133d](https://github.com/athensresearch/athens/commit/880133dfb97d8b256f324227455bfe7263516b2e))
* navigate to pages by page title, not uid. ([66e3bf2](https://github.com/athensresearch/athens/commit/66e3bf2ba89013ec70805c5330575e0845710573))


* bump fluree ([d749d88](https://github.com/athensresearch/athens/commit/d749d88a427981a1c1792a61bd7671f1ef374966))
* separate arm64 ci build ([46da69c](https://github.com/athensresearch/athens/commit/46da69cab4255ba90a54089c5e3a420065c2ad2a))
* this would infinite loop, but isn't anymore. ([dc36450](https://github.com/athensresearch/athens/commit/dc36450832451019fe253c9451d477f5eea5671b))


### Refactors

* remove last-tx ([cd56dde](https://github.com/athensresearch/athens/commit/cd56dde8643e4c92b8535ee367c43db84d745db4))
* remove old-string from :block/save event ([9cea456](https://github.com/athensresearch/athens/commit/9cea456de456589aa36bb4df85954804678aedde))
* remove response-accepted schema ([e9267c9](https://github.com/athensresearch/athens/commit/e9267c9a9ba11aa6f3660e49075751bc04afcc05))

## [1.0.0-alpha.rtc.31](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.30...v1.0.0-alpha.rtc.31) (2021-11-04)


### Features

* log resolve-transact! total time ([bda3449](https://github.com/athensresearch/athens/commit/bda34494c1b9fa9a4dff99866b7fdfbe6207bbb6))
* remove page-uid, absolute block order use in protocol ([682c24d](https://github.com/athensresearch/athens/commit/682c24d717f19ebbf0b5a7a3c75b82f38af63aa7))


### Bug Fixes

* `:block/move` allowed to go places. ([bf2a17e](https://github.com/athensresearch/athens/commit/bf2a17efdaf2a4e85898a4b531273af4c443b2ea))
* block internal state `drag-target` compatible with block move relative positioning. ([e8832e9](https://github.com/athensresearch/athens/commit/e8832e9ac42b3d8f246fadbdf9c29d58d54ebd58))
* clear session list on reconnect ([114cdbc](https://github.com/athensresearch/athens/commit/114cdbcb941002b8d2c785d8867ef01528884003))
* improve compat-position warning ([a529de4](https://github.com/athensresearch/athens/commit/a529de4ab2efd5544369565f45a3e762ccb48220))
* improve logging for paste ([e337145](https://github.com/athensresearch/athens/commit/e33714548391060b67ce9b03b44fac89412fe83c))
* last block on an empty page should not be 1 ([2322689](https://github.com/athensresearch/athens/commit/2322689995c06a94a078a5b27cfdaf93de6bde68))
* position ref-uid is string ([6f3a898](https://github.com/athensresearch/athens/commit/6f3a898a60da16dc28e57ca277eaa424f2e03508))
* style ([6f71e7a](https://github.com/athensresearch/athens/commit/6f71e7a0ffee0abf5f09f78a650d5a1ca0879446))
* style fix. ([08dbf26](https://github.com/athensresearch/athens/commit/08dbf26d95475ca24ad298d2a8c3f8eaa486251a))


### Refactors

* faster page lookups ([74d7ecc](https://github.com/athensresearch/athens/commit/74d7ecc6745dfe546bb13c5b1e3dbf7b0ec6ca58))


* all ([9159f48](https://github.com/athensresearch/athens/commit/9159f489e11f95a60226093eeed38c3b41e93cf4))

## [1.0.0-alpha.rtc.30](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.29...v1.0.0-alpha.rtc.30) (2021-11-02)


### Bug Fixes

* :block/move to new parent. ([c9da42b](https://github.com/athensresearch/athens/commit/c9da42bf3a049aa8b98b9122002813caeb2108a0))
* take same-page/up into account when computing new-block-order ([ccac6dd](https://github.com/athensresearch/athens/commit/ccac6dd4918bda9b811d93d8e87e632238973874))


### Work in Progress

* 1st drop event moved to `:block/move` ([a0de566](https://github.com/athensresearch/athens/commit/a0de566607cf380b5f8c7564705e33ec85644556))
* cleanup of drop blocks ([ed8dc57](https://github.com/athensresearch/athens/commit/ed8dc57c2f882b53d5696432b15e2d44033b43e4))
* cleanup, x-mas came early this year. ([3366d4b](https://github.com/athensresearch/athens/commit/3366d4bd2b75356a38852c4c9e98ebd159bc5e0e))
* drop diff parent using `:block/move` ([a07ef16](https://github.com/athensresearch/athens/commit/a07ef168ee706e67b472b7439ec36ffe2a2e76b1))
* drop same parent using `:block/move` ([3ce1174](https://github.com/athensresearch/athens/commit/3ce1174397e76bc76703f5c62fc11295f2daf4ba))
* drop semantic events cleanup. ([1a22f1b](https://github.com/athensresearch/athens/commit/1a22f1b2dd2fb8ed53ce18d51734f827cecc8292))
* drop-multi different source parents cases using `:block/move`. ([de0c9a8](https://github.com/athensresearch/athens/commit/de0c9a833714cac0b68c384ef4100c408be31adf))
* drop-multi/same-all using block/move-chain ([bf310bc](https://github.com/athensresearch/athens/commit/bf310bc5260f64d7e758fee57dff22520834b988))
* failing tests. ([7472553](https://github.com/athensresearch/athens/commit/7472553867dc6a955cba882d65f960683bfe79df))
* introduced `:block/link` re-frame event to drop links to blocks. ([b600f4c](https://github.com/athensresearch/athens/commit/b600f4c12bbfa395a47137eb5298435d09387b47))
* last drop-multi migrated to `:block/move` ([4407534](https://github.com/athensresearch/athens/commit/4407534b5bfb955b6c4d4394c4acd1aadb4154d4))
* moving blocks like a boss, well not really just yet. ([b9a05ed](https://github.com/athensresearch/athens/commit/b9a05edc5f261988c8862a87f72afe5c9b196b7c))
* removed dead coda around drop events. ([60fcdbf](https://github.com/athensresearch/athens/commit/60fcdbf5f13291f700e1693856894e507bb7a0a5))
* simple drop multi using chain of `:block/move` ([7226bd5](https://github.com/athensresearch/athens/commit/7226bd52f40d6c1d53204bf5c9a3be28ccd25c4e))

## [1.0.0-alpha.rtc.29](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.28...v1.0.0-alpha.rtc.29) (2021-10-28)


### Bug Fixes

* bring local ledger up to date during ensure-ledger! ([a15ecea](https://github.com/athensresearch/athens/commit/a15ecea8e207d7b835d848de739870b97c798cde)), closes [/github.com/fluree/db/issues/126#issuecomment-953903963](https://github.com/athensresearch//github.com/fluree/db/issues/126/issues/issuecomment-953903963)
* don't include a link to Welcome in mini-datoms ([00ba659](https://github.com/athensresearch/athens/commit/00ba6594f49c2f800ec0242268b9b6c2fd91fdc3))
* load theme earlier in the boot sequence ([5297d0c](https://github.com/athensresearch/athens/commit/5297d0cec193db3e5beadaecda2a8b92f25fb9ff))
* use initial datoms, but without any page links ([e1e23d6](https://github.com/athensresearch/athens/commit/e1e23d6e3675489125c0d243e81efb85ae5c0a0e))
* workaround for fluree tx limit ([45b6877](https://github.com/athensresearch/athens/commit/45b6877554eff88a01db9334f6036f8b90e0989e))
* workaround query delay ([0ae6ca8](https://github.com/athensresearch/athens/commit/0ae6ca83fc9e56a0966c74463859582fa0ea20d0))


* don't build dmg zip ([92ca6b4](https://github.com/athensresearch/athens/commit/92ca6b41e604d5c038c247430f7270b69fb2c8ba))

## [1.0.0-alpha.rtc.28](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.27...v1.0.0-alpha.rtc.28) (2021-10-27)


### Features

* store user color between sessions ([994acf0](https://github.com/athensresearch/athens/commit/994acf0bb096e6feeb3930e4c52b8bdf4cfa4f48))


### Bug Fixes

* break shape-parent-query loop if there's no parent ([bc2d170](https://github.com/athensresearch/athens/commit/bc2d170f8d06e0050ca04e299bdb3c2407f808d8))
* fix inline presence, go to user page ([37a5507](https://github.com/athensresearch/athens/commit/37a550789b17a5c2d20b52a05fb541074e0447cc))
* throw on recreating page with different uid, mismatched daily page title/uid ([7cce9eb](https://github.com/athensresearch/athens/commit/7cce9eb7f952ae647b0757686ac57d7c00f8812b))
* use daily note uid for daily note title on page create ([e26b917](https://github.com/athensresearch/athens/commit/e26b91745c87b1ffa2ee67bad17f20c88b4ceb00))
* use daily notes uid for new pages on :block/save ([5fce285](https://github.com/athensresearch/athens/commit/5fce285eb05716fa2dbb60fb415edb222691d469))
* use resolve-transact! in all locations, mark :transact event for removal ([d11125b](https://github.com/athensresearch/athens/commit/d11125baf527e6770a112c9d939194dccc0cc307))


### Refactors

* move date utils into cljc ns ([615ae86](https://github.com/athensresearch/athens/commit/615ae86e2e4c1948f2ef5ab60dc91a516bdaa72d))
* use date-to-day to simplify logic ([6853a5c](https://github.com/athensresearch/athens/commit/6853a5c68e28718e176690ee497810063ee4a33e))


### Work in Progress

* cleaned requires. ([c171845](https://github.com/athensresearch/athens/commit/c171845f523388f1edf1db9edf88d5ed5f489848))
* fixing wrong resolution and fallout ([c7c156b](https://github.com/athensresearch/athens/commit/c7c156bf0bfc35cdbf41f98cc1933e63e08c9962))
* Marked locations to make new atomic transactions be possible. ([d2e4580](https://github.com/athensresearch/athens/commit/d2e4580034b9ddebf894d6a3edf99c2c0627792b))
* Moving to 1 transaction per each atomic graph op. ([51225fe](https://github.com/athensresearch/athens/commit/51225fe9dae733f5ac443ed215e0eefb78309753))


* add disabled test for missing block new ref ([e750d53](https://github.com/athensresearch/athens/commit/e750d53a51535317a5b6646b31de64e79ef38a95))
* add server:wipe script ([f87d458](https://github.com/athensresearch/athens/commit/f87d4581b4263f58f3759f60fe63d93052b6bc40))
* enable `:block/new` test that checks for existence of rel block. ([fe7b8a7](https://github.com/athensresearch/athens/commit/fe7b8a744bd992210f1b88c3c8874e1914863858))
* pin ua-parser-js to an uncompromised version ([2d18345](https://github.com/athensresearch/athens/commit/2d1834542a9ad6608d5612036d01ec92e8d2283b))
* style happy. ([a3c2163](https://github.com/athensresearch/athens/commit/a3c2163487373b2806de663d28244944db3a35e6))
* update v1-to-v2 test ([ee45e99](https://github.com/athensresearch/athens/commit/ee45e99d9d5c16c44dc8ae3c662088136f656846))

## [1.0.0-alpha.rtc.27](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.26...v1.0.0-alpha.rtc.27) (2021-10-21)


### Bug Fixes

* don't fire db events on navigated while still loading ([a8122b6](https://github.com/athensresearch/athens/commit/a8122b62f0d5d9fe86d738957ddb79641cad7593))
* let boot control loading status ([2957bca](https://github.com/athensresearch/athens/commit/2957bcae4b3a484e243655b47fd472c74292b144))
* remove selected db on connection failure ([ef025e4](https://github.com/athensresearch/athens/commit/ef025e4eb343c5b6b5f340122e5ca3c1cdf9f47f))
* use single exit point on reset-conn for async-flow ([b04a9b7](https://github.com/athensresearch/athens/commit/b04a9b75094aab7e2882795ca85796b284c3d22f))


### Refactors

* remove unused get-db events ([3f6fcc3](https://github.com/athensresearch/athens/commit/3f6fcc32bf49e39a22d6712befd180b1e1e36373))


* style, lint, carve ([a669ffc](https://github.com/athensresearch/athens/commit/a669ffcbd5e9e53da5aeb2a1a3e87069f0bb86a9))

## [1.0.0-alpha.rtc.26](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.25...v1.0.0-alpha.rtc.26) (2021-10-20)


### Documentation

* ADR for Atomic Graph Operations and Transacting. ([23022fb](https://github.com/athensresearch/athens/commit/23022fbe05f81e676f4e4edfc91046c1fb92e14e))


* update fluree ledger to 1.0 ([1db7a24](https://github.com/athensresearch/athens/commit/1db7a241a520bfd757b965060c19e0f19b0e4e2d))

## [1.0.0-alpha.rtc.25](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.24...v1.0.0-alpha.rtc.25) (2021-10-20)


### Features

* `:block/remove` delete also subtree ([9a82163](https://github.com/athensresearch/athens/commit/9a821631bb0003cb67b4316db74bd40252714b87))
* `:block/remove` taking care of block refs too. ([b343572](https://github.com/athensresearch/athens/commit/b343572766df6284293096202129609a8ad364a9))
* allow leaving even with unsynced changes ([31f107d](https://github.com/athensresearch/athens/commit/31f107d7a3771244ee994d9178e71c88c1eb024a))
* basic delete works on atomics ([3fb4036](https://github.com/athensresearch/athens/commit/3fb4036ef21c8b8129e29e9441b0c322cc9ed5e1))
* pass page title on to avatar/presence details ([240a9e7](https://github.com/athensresearch/athens/commit/240a9e7b2dccf89329201250aa06f62efdd47433))


### Bug Fixes

* disconnect rtc client when deleting db ([c9a6621](https://github.com/athensresearch/athens/commit/c9a6621f626fc0e6c0504a87f8759d6c499b6f56))
* don't move back cursor when there's no expansion ([98cfb24](https://github.com/athensresearch/athens/commit/98cfb24cd1bb2e95932b249282714748e2488b45))
* map user to person in inline presence ([f7fb663](https://github.com/athensresearch/athens/commit/f7fb66306c6c11cdaca90d864115c8ec57eda4ea))
* print uuid as string ([f38121a](https://github.com/athensresearch/athens/commit/f38121a6b68309f98cbea3927b824f7f660f0e22))
* rollback-tx-snapshot atomically ([bcb334b](https://github.com/athensresearch/athens/commit/bcb334bb3c4773d79b0840936a945b9227eb7d43))
* use db-picker instead of client connection to determine if db is remote ([29e8d5b](https://github.com/athensresearch/athens/commit/29e8d5b1e5f22e4686351050987bd85b7e00effa))
* use right key for block/uid on initial presence ([c140340](https://github.com/athensresearch/athens/commit/c140340ee85acf43036b486c6869ad95fb0b7a14))
* user editing log should be debug level ([1c0b775](https://github.com/athensresearch/athens/commit/1c0b7750b34878a960effdd041be0de82e0fe040))


* commit deps added by fluree to shadow-cljs builds ([d567fca](https://github.com/athensresearch/athens/commit/d567fcad3fbdd21f6bcb48e339cba99cd6ce317d))
* fix ([fe9df20](https://github.com/athensresearch/athens/commit/fe9df20871a645109e3cb2e81cbec97bed297eb2))
* re-enable cljs tests, except the one that needs full web build fix ([79da717](https://github.com/athensresearch/athens/commit/79da7177560250faca8aa165ae3a03493a9362ef))
* remove unused ns ([cef5bc7](https://github.com/athensresearch/athens/commit/cef5bc78e4bfdf370b575eac4ef9d44a84f30fe4))
* run more tests in cljs ([6a0b035](https://github.com/athensresearch/athens/commit/6a0b035cb911cfcf1890b1eca61564b9f09d5d25))
* style happy ([ea39d0e](https://github.com/athensresearch/athens/commit/ea39d0e211a6234645c743702ad75f86a627a687))
* style job should run style, not lint ([bdeae79](https://github.com/athensresearch/athens/commit/bdeae79f52e929669ff9ce3d4de72f20f316925e))
* these tests need to pass ([d0981c9](https://github.com/athensresearch/athens/commit/d0981c97950deac1f4e38415da98163bc938091e))
* use atomic `:block/remove` to remove blocks. ([9425330](https://github.com/athensresearch/athens/commit/9425330c366029b44cbeee9ff535ce34be7b72fa))
* user docker compose server settings as default ([5b9e3da](https://github.com/athensresearch/athens/commit/5b9e3da9ae34460081e5d36a92a1b8f4f10a5adf))

## [1.0.0-alpha.rtc.24](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.23...v1.0.0-alpha.rtc.24) (2021-10-14)


### Bug Fixes

* fetch all events on startup ([42e21d6](https://github.com/athensresearch/athens/commit/42e21d6344ec0f7394499766f314720e18ba6956))

## [1.0.0-alpha.rtc.23](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.22...v1.0.0-alpha.rtc.23) (2021-10-13)


* more generous docker health checks ([eb1eba3](https://github.com/athensresearch/athens/commit/eb1eba30640d444b588b5161ebc10c50c740d3a3))

## [1.0.0-alpha.rtc.22](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.21...v1.0.0-alpha.rtc.22) (2021-10-13)


### Bug Fixes

* use updated schema ns ([e27860d](https://github.com/athensresearch/athens/commit/e27860dd1f3596b01ff8e7d9e67eff19081f6095))


* add health checks to docker compose, fix fluree server address ([7fa21b2](https://github.com/athensresearch/athens/commit/7fa21b20ea24e6cb16b6879bb1b5e1982d881049))
* fixit ([79e9aa1](https://github.com/athensresearch/athens/commit/79e9aa1cbafafa6ae1514fd9d3a57e595acd141e))

## [1.0.0-alpha.rtc.21](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.20...v1.0.0-alpha.rtc.21) (2021-10-13)


* increase max memory for athens server ([44e9672](https://github.com/athensresearch/athens/commit/44e9672004561fa5c10fb493b453ba644ad23ba3))

## [1.0.0-alpha.rtc.20](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.19...v1.0.0-alpha.rtc.20) (2021-10-13)


### Bug Fixes

* include default.config.edn in uberjar, only use config.edn in dev ([8a5ef89](https://github.com/athensresearch/athens/commit/8a5ef89208144a3620949874b1e68ed29fbabf05))
* remove cljstyle workaround ([fce927e](https://github.com/athensresearch/athens/commit/fce927e99d2591c497352a83351c427510ecbdce))

## [1.0.0-alpha.rtc.19](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.18...v1.0.0-alpha.rtc.19) (2021-10-13)


### Features

* use fluree as an event-log with a datascript db ([e558ba0](https://github.com/athensresearch/athens/commit/e558ba044acab4134be16e31fa9d25d412e496f0))


### Bug Fixes

* datascript comp transacts using web transact fn ([d1e00f3](https://github.com/athensresearch/athens/commit/d1e00f3bccefb65e2969284c4b1c0f2550dfd455))


* don't use prefix for docker services ([6de4ff8](https://github.com/athensresearch/athens/commit/6de4ff87016ed18eaad856bf111c955188196c5c))
* include test in default source paths ([8cf2012](https://github.com/athensresearch/athens/commit/8cf2012f2510f3263839441bfe2ea3cd71ca83a2))
* use immutable tag for fluree docker ref ([6013ae0](https://github.com/athensresearch/athens/commit/6013ae0e972a490f4f75b06420ebaa753f0889ea))

## [1.0.0-alpha.rtc.18](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.17...v1.0.0-alpha.rtc.18) (2021-10-12)


### Features

* `:page/new` can be composite op. ([d5635a7](https://github.com/athensresearch/athens/commit/d5635a7b1195b414b803420c9bd4b1eece22da35))
* reject optimistically transacted events ([fc7b4be](https://github.com/athensresearch/athens/commit/fc7b4be7356891592dfc4f500c916529e67eb823))


### Bug Fixes

* :git/sha issue and ordered was released. ([e6237d8](https://github.com/athensresearch/athens/commit/e6237d8b56daf13365624d901f5f38eabec66fc8))
* cljs log converts args to js ([7d2b29f](https://github.com/athensresearch/athens/commit/7d2b29fef5490e87c456c2c764a6e70e5705bff8))
* update carve to fix report bug ([fb36b0c](https://github.com/athensresearch/athens/commit/fb36b0cb2c9c951d1396b0d2b1f71f31aea98f28))


### Refactors

* remove unused old event tracking subs ([118e61d](https://github.com/athensresearch/athens/commit/118e61d94a9eb2c135e9ab7e6d8e2119849f61d0))
* yarn server runs server, yarn server:uberjar builds uberjar ([1a44b48](https://github.com/athensresearch/athens/commit/1a44b4836bf67cb2e28f4bfb8727520bec7a9423))


* concurrency compatible `:block/new-v2` op with tests. ([caabff8](https://github.com/athensresearch/athens/commit/caabff840c2c0b43cac26e979424f300240352f9))
* move from lein to clj+deps.edn ([af01e2a](https://github.com/athensresearch/athens/commit/af01e2a0d0921ac740d19b7583ff904ded2f7bc3))

## [1.0.0-alpha.rtc.17](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.16...v1.0.0-alpha.rtc.17) (2021-10-07)


* cache built app ([42032f0](https://github.com/athensresearch/athens/commit/42032f0ae214729cfb276e80bab992312e459e62))

## [1.0.0-alpha.rtc.16](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.15...v1.0.0-alpha.rtc.16) (2021-10-07)


### Features

* add basic eventsync impl ([8e986d8](https://github.com/athensresearch/athens/commit/8e986d8d80d00b0dbf65888aaf9091f59d6e5915))
* add description of EventSync ([46ffb69](https://github.com/athensresearch/athens/commit/46ffb6902a33f586c74c00bdcc52fee2707e779a))
* block-uid nil eater part 2. ([219eea0](https://github.com/athensresearch/athens/commit/219eea0377ceae6e3b0375f4c8be3183681abd7b))
* DB Consistency check/fix on startup and some logging. ([54de653](https://github.com/athensresearch/athens/commit/54de6537c5131c682d4fc6387d0d91f203b7b320))
* first event-sync implementation ([8ab0f48](https://github.com/athensresearch/athens/commit/8ab0f483c21bf1302fa01db26ce457fb3b5ee178))
* **notification:** block in notification component ([cc14bb9](https://github.com/athensresearch/athens/commit/cc14bb90987b081905d9c3c105bb0a3607abaa54))
* **notifications:** add notifications component ([f22a957](https://github.com/athensresearch/athens/commit/f22a95701493c619ccaacbd59eff8fc2139be518))
* process operations optimistically in RTC ([f1adb2b](https://github.com/athensresearch/athens/commit/f1adb2b6058b16a6f2602be4f7e3a2f7089cd1a0))
* show db sync state as events left to sync ([a546e20](https://github.com/athensresearch/athens/commit/a546e20f2120d05b39e83d327bce868dd4cb341e))
* **toast:** basic toast fn in place ([6bf841a](https://github.com/athensresearch/athens/commit/6bf841aee5a91e1e93884c3923e893c67daa26e2))
* Uniform logging for CLJ & CLJS. ([497e237](https://github.com/athensresearch/athens/commit/497e237c74d2e4b8f04efc99717fc8e4f40a86ee))
* use electron-window-state to persist window size and position ([30f3522](https://github.com/athensresearch/athens/commit/30f3522b5d49085cfea1ce096e266dabf7ae310c))


### Bug Fixes

* add missing folder to repo ([d57ffb9](https://github.com/athensresearch/athens/commit/d57ffb9173b63b8afcbdd6fc2755cc3c15b878d9))
* **button:** properly unrequire props ([f5e7fca](https://github.com/athensresearch/athens/commit/f5e7fcad13d445fe1eb5d5fb1b5a0f4b0a93b2ce))
* check selected-db, not client state for write-db ([ac95df8](https://github.com/athensresearch/athens/commit/ac95df83cdcde6583d80823bd1e990e1b42ddaab))
* **db:** use correct default zoom level ([6896d5a](https://github.com/athensresearch/athens/commit/6896d5a4bf3081b5ea9aac2a0aac4d67df95f40c))
* fix keyword typo ([09af693](https://github.com/athensresearch/athens/commit/09af6931fc6e5f0f13d10fc9549dce57f1eb2336))
* log but don't error out on stale strings ([c9be401](https://github.com/athensresearch/athens/commit/c9be4013716c005996dcfdd73afc9cfcb3acbf98))
* log correct `event-id`. ([8f5240c](https://github.com/athensresearch/athens/commit/8f5240c8b3a0d58a7a2be7b157b3976a0cdb6d7f))
* match block uid correctly ([032199e](https://github.com/athensresearch/athens/commit/032199e9aac7b4aecfcf764610646ea48ea008cf))
* **presence:** presence menu appears over toolbar ([d290f5c](https://github.com/athensresearch/athens/commit/d290f5c344a1bbae5545241b9b2d58dc61a62934))
* remove unused ns require ([2a65a4f](https://github.com/athensresearch/athens/commit/2a65a4fe6070320feb39b77ebe58f629c1ab2cd8))
* removed `:remote/db-id` as we ain't using it no mo. ([736c968](https://github.com/athensresearch/athens/commit/736c96805244317bc31810ea87cfbcebcc863a82))
* removed unused test ([b40d29b](https://github.com/athensresearch/athens/commit/b40d29bf73695eeed7a851b91e2daa02e31ecfc0))
* small fixes from the demo meeting ([7fa4ece](https://github.com/athensresearch/athens/commit/7fa4ece1c1e85ba4e4d420ebca199eeaca5c7bc6))
* sync on empty memory log from post-op db ([f6e73c5](https://github.com/athensresearch/athens/commit/f6e73c50b624128db31bde0f53845d6aafc80879))
* this is not how one transacts. ([d6564a4](https://github.com/athensresearch/athens/commit/d6564a41c6abe282e90065d59f44a556f58b5c8b))
* **toolbar:** unzoom toolbar ([7eef057](https://github.com/athensresearch/athens/commit/7eef05744c560dd8d08100e530e3d1516c72efa3))
* transact needs vector. ([f177902](https://github.com/athensresearch/athens/commit/f177902513faf7bd90fffb157fa2922e14fb642d))
* typo ([16db7d5](https://github.com/athensresearch/athens/commit/16db7d5136fad89a1ef32a536512d86846234d60))
* use right key for page lookup ([911d78e](https://github.com/athensresearch/athens/commit/911d78e8421b30d7b1e258d7a3ccfb9f6a0dbf4e))
* we actually don't need `:db/id` on `:block/children` ([f1cf26d](https://github.com/athensresearch/athens/commit/f1cf26d60c08e32c9649372e0c89b333659706a7))


* `dev/datahike-conn` useful also outside `dev` ns. ([b26173b](https://github.com/athensresearch/athens/commit/b26173bef6e62b1a887c94f667acb22fa255e7e2))
* `remote.cljs` also uses `common.log`. ([6760532](https://github.com/athensresearch/athens/commit/6760532794df357b0336c37339d9bd37467eea74))
* actual memory requirements. ([bdd6ad8](https://github.com/athensresearch/athens/commit/bdd6ad8449f2eb6d896c220f3337cdf7770bce9b))
* add tests for event-sync ([868bdf2](https://github.com/athensresearch/athens/commit/868bdf23cf7c77e2f231d860e037c643c6bb67e0))
* all the checks. ([573edb0](https://github.com/athensresearch/athens/commit/573edb0f2cb87805706c96917329e43cc8adb7e7))
* by default use same path for DB as dockerized config. ([4ef2119](https://github.com/athensresearch/athens/commit/4ef21196da078e90ac5e86f10bd22a941267559f))
* commented out broken tests. ([c1f08df](https://github.com/athensresearch/athens/commit/c1f08dfef1ed7f1c2b98cc7c25e0093713b00f67))
* fixit ([e6828a2](https://github.com/athensresearch/athens/commit/e6828a2df06f864822bc508778e5c7efde89d4bc))
* fixit ([c225a48](https://github.com/athensresearch/athens/commit/c225a48803f428c9e6b05e5570e197507457a929))
* logging cleanup. ([bff79fc](https://github.com/athensresearch/athens/commit/bff79fcb0145a62702fd38386552ab1df497be94))
* pprint failed transactions. ([0c3343f](https://github.com/athensresearch/athens/commit/0c3343fffe3f4ed391a5fe69a067253e935a4cf9))
* remove unneeded commas ([aaa007c](https://github.com/athensresearch/athens/commit/aaa007cfb9fdb8b1e91f21411c042bb0e4b0f97c))
* talk `:block/uid` to me. ([25587ae](https://github.com/athensresearch/athens/commit/25587aeb972bcbd4c09ed1ccdf1411d26a4fa6da))
* use deps.edn with lein ([df9528f](https://github.com/athensresearch/athens/commit/df9528f14e19a396a8237c25712967e790536312))
* use sha for org.flatland/ordered ([27cebb0](https://github.com/athensresearch/athens/commit/27cebb00eb0a4e9e3c6b00928b359d1d2c424da0))


### Refactors

* follow map-first seq-last clojure convention ([0d1b8a2](https://github.com/athensresearch/athens/commit/0d1b8a24f69dd0eb6f697ce0fe44e3fb0bdae0a6))
* **notification:** simplify buttons ([d07a20a](https://github.com/athensresearch/athens/commit/d07a20a53785d0dfadf1c5ba52611a3e29b76369))
* **notification:** simplify buttons ([25ccd84](https://github.com/athensresearch/athens/commit/25ccd843774fe2909b2bde3f0784b91bb8df5e0b))
* **presence:** move layout containment to html ([d532cd9](https://github.com/athensresearch/athens/commit/d532cd98cec7081fc56f7a99d593f8060e6aa2b1))
* use log/debug in optimistic events ([8856cc2](https://github.com/athensresearch/athens/commit/8856cc27475e9571bd670f0cc6f43c78cbe54a69))

## [1.0.0-beta.98](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.8...v1.0.0-beta.98) (2021-09-27)

## [1.0.0-beta.97](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.6...v1.0.0-beta.97) (2021-09-21)


### Bug Fixes

* Correct JS constructor name. ([bfbc940](https://github.com/athensresearch/athens/commit/bfbc94020a16793be7cd6f65ae78a08ab2f2abe3))
* Just errors and crashes sent to Sentry. ([7c48179](https://github.com/athensresearch/athens/commit/7c48179ec67d59104f3823a7f1d4f1df36845c78))
* Sort linked references by date descending. ([95dbd60](https://github.com/athensresearch/athens/commit/95dbd60dac93ce524c5fe64ee6b2336ef8ce2940))

## [1.0.0-beta.96](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.2...v1.0.0-beta.96) (2021-09-02)


### Bug Fixes

* Selection behaviour fixed (ported to main). ([2cc7877](https://github.com/athensresearch/athens/commit/2cc787759ada642f6faccc7c8ccace75b99348ff))

## [1.0.0-beta.93](https://github.com/athensresearch/athens/compare/v1.0.0-beta.92...v1.0.0-beta.93) (2021-08-04)

## [1.0.0-alpha.rtc.15](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.14...v1.0.0-alpha.rtc.15) (2021-09-30)


### Features

* **blocks:** use ts block anchor ([a9733f2](https://github.com/athensresearch/athens/commit/a9733f21aac700b6f64a3d3215bdc3de76d4dd66))
* **button:** buttons have styled focus ring ([6f07ad3](https://github.com/athensresearch/athens/commit/6f07ad3905a53783e13d5ad27df76b66ad39d69d))


### Bug Fixes

* **anchor:** can drag blocks again ([19106a9](https://github.com/athensresearch/athens/commit/19106a94bcb5d2b36582390ef13d7b3fa5804718))
* don't send editing nil events ([49259cf](https://github.com/athensresearch/athens/commit/49259cf6206a43d0f50c7442b5c5867c1c731b63))
* presence for home page should show all users on daily notes ([811eba2](https://github.com/athensresearch/athens/commit/811eba207cf1c0f9770201e1e89196b402e5cd2a))
* **toolbar:** rendering database menu ([b54c470](https://github.com/athensresearch/athens/commit/b54c47078ea384c54e854b7329daefb2d92391b8))


### Refactors

* **blocks:** use ts toggle and bullet ([82b617e](https://github.com/athensresearch/athens/commit/82b617ed49734963f119b43f4218dd444dd0455b))
* **block:** use ts debug details ([ca0eb2a](https://github.com/athensresearch/athens/commit/ca0eb2a8bb535ad4f6daaeaf930caafd15fd8fd0))
* **block:** use ts debug details ([531f096](https://github.com/athensresearch/athens/commit/531f0962ab587930c6a45be757e3848f0ad7d668))
* **toolbar:** use ts toolbar ([b81f765](https://github.com/athensresearch/athens/commit/b81f765869619f607c1f012e9d69861056e84b46))


* add newline ([0d7475b](https://github.com/athensresearch/athens/commit/0d7475b01b66f4f36ac17ce30b26692042856139))
* ignore temp unused fn ([abd84e4](https://github.com/athensresearch/athens/commit/abd84e48e71dce3810249cdfa3552f5a6fbecd7a))
* lint ([349415c](https://github.com/athensresearch/athens/commit/349415c98c236c7fa4eda4f2f28fb00235e796b1))
* lint ([a0048e2](https://github.com/athensresearch/athens/commit/a0048e2bad3e5cce82ad2bffa450a21404534416))
* minor fixes and cleanup ([d58e960](https://github.com/athensresearch/athens/commit/d58e9606b73add52ddd0f89c3c5cd5e0ab0f4191))
* minor fixes and cleanup ([c089e72](https://github.com/athensresearch/athens/commit/c089e7222140b040af8abd11c3cb76f5b53334c3))
* remove unused import ([f5ee071](https://github.com/athensresearch/athens/commit/f5ee07125899dcf0b3ded2a95205c262553c5ea5))

## [1.0.0-alpha.rtc.14](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.13...v1.0.0-alpha.rtc.14) (2021-09-30)


* increase yarn timeout ([1878c50](https://github.com/athensresearch/athens/commit/1878c50a05aa17e785dd768ef61033423dd7316f))

## [1.0.0-alpha.rtc.13](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.12...v1.0.0-alpha.rtc.13) (2021-09-30)


### Bug Fixes

* **presence:** clean up misc styling issues ([80121a3](https://github.com/athensresearch/athens/commit/80121a3a567a71c7cb2dfd15c267e87681a1426d))


* add missing iconoir-dep ([918ab53](https://github.com/athensresearch/athens/commit/918ab53893847bf96e1b972974d9906850c77ab3))

## [1.0.0-alpha.rtc.12](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.11...v1.0.0-alpha.rtc.12) (2021-09-30)


### Bug Fixes

* always have username and color for people ([a97d003](https://github.com/athensresearch/athens/commit/a97d0035402061f10e3a7a90d86370844be016b8))
* don't release docker-compose that attempts to build images ([1a313e2](https://github.com/athensresearch/athens/commit/1a313e219f1da8f8c3f544b33aa9c7e92269b6e4))


* always store athens data by the compose file ([420a7c7](https://github.com/athensresearch/athens/commit/420a7c79024d40140b667c344d17620efbe04dc8))
* don't build server jar on release-electron ([d3daf2a](https://github.com/athensresearch/athens/commit/d3daf2a8cd53be0c53bcf21ad20dba7010ac42d9))
* use data env var for data, logs path ([13a0165](https://github.com/athensresearch/athens/commit/13a016519144295963be2dc41f317e923a3b8b38))

## [1.0.0-alpha.rtc.11](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.10...v1.0.0-alpha.rtc.11) (2021-09-28)


### Bug Fixes

* always provide a color for current user ([f49c24d](https://github.com/athensresearch/athens/commit/f49c24d6e77ab6269459389fda3c21a164d2fb61))
* less logging on backend and more testing. ([84a3de5](https://github.com/athensresearch/athens/commit/84a3de59959c29b1114dca6eabdad2a5e4ec8942))


### Refactors

* **spinner:** use ts spinner ([80b8a87](https://github.com/athensresearch/athens/commit/80b8a872de6501358c151e18a92314c4f76cfc5d))
* **toggle:** use ts toggle ([794b004](https://github.com/athensresearch/athens/commit/794b004e8c8f8d37a0a9d5a216bcb7037cd8c119))


* cljstyle happy ([efbe155](https://github.com/athensresearch/athens/commit/efbe1559ff074353666eb4114fb765e50d7dc701))
* don't limit docker release to main ([086b9a4](https://github.com/athensresearch/athens/commit/086b9a491c1f3de48aa73045da33c54ed326d8ba))
* factor out env setup ([1c2b009](https://github.com/athensresearch/athens/commit/1c2b0097a4db2b494fd46735fb091794dffd9e8c))
* fix style issues ([d763bde](https://github.com/athensresearch/athens/commit/d763bdeff10676359ad0f44a683a29e7e44c65c0))
* fix style issues ([ead68b3](https://github.com/athensresearch/athens/commit/ead68b370e2ab134c1149ad4e9a17fafa5bbccc6))
* fix style issues ([60e2ccd](https://github.com/athensresearch/athens/commit/60e2ccdae45ec09552a4615a076b411ce7471f78))
* release athens,nginx docker image and docker compose ([2ed4f71](https://github.com/athensresearch/athens/commit/2ed4f71fe391b2dd7d62bac61a9e12f3db90ac5f))
* rename release jobs ([8c13d2a](https://github.com/athensresearch/athens/commit/8c13d2ab097741ba0069074110b2e2c87092f76e))
* reuse checkout via anchor ([040836f](https://github.com/athensresearch/athens/commit/040836f230a86e0e7de791f4d0c4613c1ed1cf11))
* use ubuntu-latest throughout ([4c070f7](https://github.com/athensresearch/athens/commit/4c070f7ffe4669470d982e0191f6a4ce77d55f7c))

## [1.0.0-alpha.rtc.10](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.9...v1.0.0-alpha.rtc.10) (2021-09-27)


### Features

* Orderkeeper to keep all your `:block/order` ordered. ([7ac1844](https://github.com/athensresearch/athens/commit/7ac18448b02a600f7dd1ada16277c58209d0cfca))

* Don't log TXs and typo. ([915a818](https://github.com/athensresearch/athens/commit/915a818dd70f0fbf52a4212501af36313348320a))


* carve happy. ([0dba4d4](https://github.com/athensresearch/athens/commit/0dba4d4f12923ee308af2d4a6e90b710cc5bf314))

## [1.0.0-alpha.rtc.9](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.8...v1.0.0-alpha.rtc.9) (2021-09-27)


### Features

* **dialog:** add new dialog component ([15ced38](https://github.com/athensresearch/athens/commit/15ced387e9047568793464131c2c61c9925b108c))



### Bug Fixes

* always focus first child, even on daily notes ([c7b1a90](https://github.com/athensresearch/athens/commit/c7b1a90ad5028b592c2f8c091249c2235b6bc639)), closes [#1669](https://github.com/athensresearch/athens/issues/1669)
* **avatar:** stack accepts style props ([969eb22](https://github.com/athensresearch/athens/commit/969eb22a5880b5fbca3aab114f1b65914764972c))
* pass username, not channel, to goodbye-handler ([a70489c](https://github.com/athensresearch/athens/commit/a70489c91cae41a2d3c11f909d99afa0cde4f47d))
* **presence:** use consistent avatar spacing ([0ad1b16](https://github.com/athensresearch/athens/commit/0ad1b16d80bb86a2e8ccd59f4c8dbc569ba964cc))
* remove unused component ([2a9c489](https://github.com/athensresearch/athens/commit/2a9c48945e8b2b6eabd9b3d506712f8e55e7681d))


### Refactors

* **dialog:** replace page merge alert with dialog ([5fd1951](https://github.com/athensresearch/athens/commit/5fd1951bedd83f48316d91eb24ec74c478ef7b3d))


* remove old alert component ([ac9cc4f](https://github.com/athensresearch/athens/commit/ac9cc4f2bb26d69f205820a520b104799875c597))


## [1.0.0](https://github.com/athensresearch/athens/compare/v1.0.0-beta.98...v1.0.0) (2021-11-12)

## [1.0.0-beta.98](https://github.com/athensresearch/athens/compare/v1.0.0-beta.97...v1.0.0-beta.98) (2021-09-27)

## [1.0.0-beta.97](https://github.com/athensresearch/athens/compare/v1.0.0-beta.96...v1.0.0-beta.97) (2021-09-21)


### Bug Fixes

* Correct JS constructor name. ([bfbc940](https://github.com/athensresearch/athens/commit/bfbc94020a16793be7cd6f65ae78a08ab2f2abe3))
* Just errors and crashes sent to Sentry. ([7c48179](https://github.com/athensresearch/athens/commit/7c48179ec67d59104f3823a7f1d4f1df36845c78))
* Sort linked references by date descending. ([95dbd60](https://github.com/athensresearch/athens/commit/95dbd60dac93ce524c5fe64ee6b2336ef8ce2940))

## [1.0.0-beta.96](https://github.com/athensresearch/athens/compare/v1.0.0-beta.94...v1.0.0-beta.96) (2021-09-02)


## [1.0.0-alpha.rtc.8](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.7...v1.0.0-alpha.rtc.8) (2021-09-23)


### Features

* Password protection ([c4027eb](https://github.com/athensresearch/athens/commit/c4027eb9c93c19b75a177b7706a60b1a8205592a))
* send full presence state on connect ([710cefb](https://github.com/athensresearch/athens/commit/710cefb244c4df6b476bf0e9410dc0ea73df6854))
* set user presence on first child block when navigating ([8001ba5](https://github.com/athensresearch/athens/commit/8001ba50082238c2e25b08858d33863f3de3dc61))


### Bug Fixes

* all pages is slow when a page has 100+ blocks ([e2e3204](https://github.com/athensresearch/athens/commit/e2e3204fc2d5746cd55420ae5bb3bc91ce186663))
* **db menu:** less broken style for db picker menu ([e82825a](https://github.com/athensresearch/athens/commit/e82825a9ccc262f35dd364b9fb07f9ee0567968c))
* review items ([f820cef](https://github.com/athensresearch/athens/commit/f820cef2cbeda1a6f6b500bcdcc6c0389602039e))
* selection issues. ([297df4a](https://github.com/athensresearch/athens/commit/297df4a7d4f66ce2c984748bacc4552ca5ecd7a1))

## [1.0.0-alpha.rtc.7](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.6...v1.0.0-alpha.rtc.7) (2021-09-21)


### Bug Fixes

* Catchup with wrong RTC releases. ([9e48f65](https://github.com/athensresearch/athens/commit/9e48f657a7531aeb72ae43936753ac17070a90e2))
* set body classes ([4a7d3ac](https://github.com/athensresearch/athens/commit/4a7d3aca71be253e0af2d62a84fa6a68128ae6f4)), closes [#1654](https://github.com/athensresearch/athens/issues/1654)

## [1.0.0-alpha.rtc.4](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.3...v1.0.0-alpha.rtc.4) (2021-09-16)


### Features

* add type for connection and host address ([a305db5](https://github.com/athensresearch/athens/commit/a305db52c019d54b6771eae219943b1f1c4c68a2))
* **apptoolbar:** improve apptoolbar state and presence ([242ee95](https://github.com/athensresearch/athens/commit/242ee959cc9123f6b9f821fa61c330e73dc0e50d))
* build proper tsx button component ([107d3dd](https://github.com/athensresearch/athens/commit/107d3dd7557fd48781170d68b8d1c14fa281b4a6))
* **button:** add button variants ([2063569](https://github.com/athensresearch/athens/commit/20635691d483fba55ca5b19d1f12e98e55cacfd2))
* **db icon:** now support emoji and custom colors ([7755cef](https://github.com/athensresearch/athens/commit/7755cefb07ae97963ff3ada8dbdb38ead14fffce))
* **dialog:** dialog should pass through modal props ([fa7f8ec](https://github.com/athensresearch/athens/commit/fa7f8ecfc9338d046ac3f2b4b53a4593fb696aab))
* integrate page level presence ([26d3259](https://github.com/athensresearch/athens/commit/26d3259ca4c420c0757954518af426503fd39d2b))
* move presencedetails with settings from concept to base ([ce8f59e](https://github.com/athensresearch/athens/commit/ce8f59ece3450ede7d6bd0b151dcdd0054616bef))
* **rtc:** show connection status in presence toolbar area ([ee6399d](https://github.com/athensresearch/athens/commit/ee6399dd2cb5d545ca30f4ddb0b9c2b6d58d5fdb))
* **storybook:** add accessibility and color theme addons for devx ([05466eb](https://github.com/athensresearch/athens/commit/05466ebc60e05a6f1b7fe0df8d39ae57804ca384))
* **storybook:** add app overview stories ([e3c1ca4](https://github.com/athensresearch/athens/commit/e3c1ca435f5dd1ff650949631e432e347ef5e581))
* **storybook:** add avatar component ([bf6f341](https://github.com/athensresearch/athens/commit/bf6f34118f95f606e08d06eb3369aef28df5838e))
* **storybook:** add avatar stack component ([d6fcf22](https://github.com/athensresearch/athens/commit/d6fcf22e376c815e8178761c1b37fa4950c55bcf))
* **storybook:** add avatars to block story ([1ea93bd](https://github.com/athensresearch/athens/commit/1ea93bd87c0ddc4958f3d527a4f9b1b896e3c2e2))
* **storybook:** add avatars to block story ([b870251](https://github.com/athensresearch/athens/commit/b8702516eae857bf94f9eb1ffb05bfa7be3c611d))
* **storybook:** add basic breadcrumb component ([0f832a1](https://github.com/athensresearch/athens/commit/0f832a17a0a74d99ded5c4b57024995c2884c1c5))
* **storybook:** add basic dialog component ([1a84673](https://github.com/athensresearch/athens/commit/1a846738ddf10d7412cd7caeef68319d8b69cae3))
* **storybook:** add button hover style ([6b9dd15](https://github.com/athensresearch/athens/commit/6b9dd152a82d602d243bc4f474164acdefa980ca))
* **storybook:** add checkbox component ([c4ba5d8](https://github.com/athensresearch/athens/commit/c4ba5d8ed45112836939bac6e1a528329f2d5855))
* **storybook:** add db menu ([98f99b5](https://github.com/athensresearch/athens/commit/98f99b54a5f7402762d4bef447376aed7a5825bb))
* **storybook:** add embed component ([42431fd](https://github.com/athensresearch/athens/commit/42431fd80b635b92b0da230e23bdb164a284a8cf))
* **storybook:** add left sidebar story ([76566e8](https://github.com/athensresearch/athens/commit/76566e8a1b59248feec84984f1f4c2c4d5cd5855))
* **storybook:** add meter component ([4037363](https://github.com/athensresearch/athens/commit/40373639b40f3768d3467475b7f174edf721d27b))
* **storybook:** add presence item to toolbar ([adfafdb](https://github.com/athensresearch/athens/commit/adfafdb5a7d7f74fb2b83a9ce12c31b8f769fa0c))
* **storybook:** add presence story to page ([d1d63e9](https://github.com/athensresearch/athens/commit/d1d63e92b130b8e308d33072019ca4a6ab5d0879))
* **storybook:** add preview concept ([0984ac7](https://github.com/athensresearch/athens/commit/0984ac77340938e51ee98f3faf2450ae1727a5ce))
* **storybook:** add profile settings dialog ([7c87389](https://github.com/athensresearch/athens/commit/7c87389ee5039795d89ae965eaea57475b6a9e64))
* **storybook:** add profile settings dialog ([3412763](https://github.com/athensresearch/athens/commit/3412763626cbdd453be0fa1adc07bff7bbc8513f))
* **storybook:** add references to pages ([54bc086](https://github.com/athensresearch/athens/commit/54bc086ca66351a6c3e0f53f641fced4343621eb))
* **storybook:** add right sidebar component ([510a365](https://github.com/athensresearch/athens/commit/510a365513545be0b38328513ec4d73277a0eeb0))
* **storybook:** add separate browser and app stories ([6426912](https://github.com/athensresearch/athens/commit/64269126a0adf94f80e4383ee5e24b226bd48bac))
* **storybook:** add simple badge component ([fb32567](https://github.com/athensresearch/athens/commit/fb32567cc1db2db1bbb779332481f228226bc9d8))
* **storybook:** added commandbar component ([3513669](https://github.com/athensresearch/athens/commit/3513669b68ed0e414dd7afbba74d30cb93de7695))
* **storybook:** added left sidebar story ([ae6caf8](https://github.com/athensresearch/athens/commit/ae6caf8bfe1150ff3af5e4ff03210374b63536e7))
* **storybook:** allow buttons to have unset shape and style ([8b1e439](https://github.com/athensresearch/athens/commit/8b1e439594da76238065525e08a60c4a48900159))
* **storybook:** allow buttons to have unset shape and style ([6837b42](https://github.com/athensresearch/athens/commit/6837b42e29b8eca32556fc06e4dee7af6533274f))
* **storybook:** begin to implement blocks in typescript ([eb51890](https://github.com/athensresearch/athens/commit/eb518904da1de4378fc5490a5ffc46ae5113e920))
* **storybook:** block in bidilink component and story ([5adecf8](https://github.com/athensresearch/athens/commit/5adecf8a67582d6f79c288166bba51dd2e6a6300))
* **storybook:** block in early text input component ([1a4147e](https://github.com/athensresearch/athens/commit/1a4147ea240c23f281e50aba84e791f05699ba7f))
* **storybook:** block in email settings ([6f9ca11](https://github.com/athensresearch/athens/commit/6f9ca11b9c2115df56ea81cfa27eb4bb64f9f744))
* **storybook:** block in new dailynotes concept ([2c6bc26](https://github.com/athensresearch/athens/commit/2c6bc265e1e196a32bbf8a0a12df2e11675a24b1))
* **storybook:** block in new dailynotes concept ([d4f09a7](https://github.com/athensresearch/athens/commit/d4f09a78650f2a8a293933c79591227d6c0229c4))
* **storybook:** block in settings page ([894308e](https://github.com/athensresearch/athens/commit/894308e7be188d44bd790b9b0503dbe386f4cf7f))
* **storybook:** block in style guide page ([78ca286](https://github.com/athensresearch/athens/commit/78ca28618aa36b0eebd01033785056b009e851ae))
* **storybook:** blocking in daily notes ([ea4fc47](https://github.com/athensresearch/athens/commit/ea4fc473024ddddd9bd205d3304dceeed5246812))
* **storybook:** blocks don't become editable if not editable ([b2ac479](https://github.com/athensresearch/athens/commit/b2ac479aeffe84dfbaf30a4b4e84de7183bc96ec))
* **storybook:** clean up blocks and export subcomponents ([e778605](https://github.com/athensresearch/athens/commit/e778605c9971189929050efb2b4c5d6a9ff63bde))
* **storybook:** compenents and stories for page content ([f887854](https://github.com/athensresearch/athens/commit/f887854abce960574f4de58ad06c1acff127874d))
* **storybook:** create css vars in js instead of clj ([dca0688](https://github.com/athensresearch/athens/commit/dca06885cc21c086010eacd45c9d3fb8bb492310))
* **storybook:** electron-like wrapper for desktop testing in storybook ([7ada2fd](https://github.com/athensresearch/athens/commit/7ada2fd7435f8380a5027ee481723b4d1e6cf5c9))
* **storybook:** more working block aspects ([3984224](https://github.com/athensresearch/athens/commit/3984224539ca7ce3b42768b7f63cdebcfad3f36a))
* **storybook:** previews can render limited block content ([fad186d](https://github.com/athensresearch/athens/commit/fad186dfd99e498c4f96254cd34b20608fe59f18))
* **storybook:** themed story context ([5c90fe2](https://github.com/athensresearch/athens/commit/5c90fe25ee8ce68ab0c96830082dc30177ea2c7f))
* **storybook:** update storybook build config ([0fd36fe](https://github.com/athensresearch/athens/commit/0fd36fe838c85b30bd4eb1c5a6d2abcd3558984f))
* **storybook:** use badges to indicate component status ([5e49e03](https://github.com/athensresearch/athens/commit/5e49e03dccc95aa745b7cb7a4eba3cc095aa4f2a))
* **storybook:** use context provider for presence ([77f6643](https://github.com/athensresearch/athens/commit/77f6643a9737dfb6475c9c7e2f9acd88647e2184))
* **storybook:** use presence context for page ([a477a84](https://github.com/athensresearch/athens/commit/a477a846be4bc3d4be902dbc1515cae9f70f337c))
* **style:** add serif font var ([184d56f](https://github.com/athensresearch/athens/commit/184d56f97296852772c3af4c32da422ed90ec8fc))
* **welcome, icons:** dbs support emoji and color ([207097c](https://github.com/athensresearch/athens/commit/207097c1cadc60721c8c36ff7e1dcb7d7f84b304))
* **welcome:** block in welcome component ([6da39a9](https://github.com/athensresearch/athens/commit/6da39a9d6f8381b3b423fd57a093c3d25d36477b))
* **welcome:** block in welcome component ([3f3bb38](https://github.com/athensresearch/athens/commit/3f3bb38ec42e7be0be9eeacbe8c1e526e03ee5d9))


### Bug Fixes

* add typeroots back ([9cc2457](https://github.com/athensresearch/athens/commit/9cc2457b9750254a2c9b3b0eb95e832be251c679))
* **breadcrumbs:** use proper attribute for svg stroke properties ([17ae5e6](https://github.com/athensresearch/athens/commit/17ae5e6d63a0d598f05ef1539263b9b6e814cd5d))
* broadcast username update ([28111a2](https://github.com/athensresearch/athens/commit/28111a2b4531e7e303c7ebde1f017e2235a5840b))
* comment unused prop ([5ae417d](https://github.com/athensresearch/athens/commit/5ae417d1dd4d097852b8f9797e68830182c0156e))
* concurrent resolution should not happen. ([698913e](https://github.com/athensresearch/athens/commit/698913ec7dda9330eb891392e323600c5480d9af))
* don't error out when changing name ([65ea223](https://github.com/athensresearch/athens/commit/65ea223db60f7330a3cfc0eab60e91e79c250785))
* don't exclude root stories ([10dc517](https://github.com/athensresearch/athens/commit/10dc5171e0ef96d2f5bada932e87ac1825e0d797))
* Don't send tx data with db-dump. ([8610251](https://github.com/athensresearch/athens/commit/8610251c5ce8fb5a7a1fb7e5d1078d9053422c93))
* don't show babel warning durings storybook build ([cdb30ab](https://github.com/athensresearch/athens/commit/cdb30ab92a059fad3cf1e1fe3506387565e15311))
* further babel warning fixes ([7086789](https://github.com/athensresearch/athens/commit/7086789893e6c5b606299026c2053c9bb6edc562))
* **overlay:** don't specify overlay flex direction ([f9c751c](https://github.com/athensresearch/athens/commit/f9c751cec391a6479df47c2235762c321ed3c9b0))
* remove duplicate require ([f0ec1df](https://github.com/athensresearch/athens/commit/f0ec1df950ea862736641543e40023f6a62ad562))
* remove duplicated button file ([ee20473](https://github.com/athensresearch/athens/commit/ee20473c4e7b2eb188db6e6b4103513943ea7266))
* silence postcss deprecation warning ([1900e10](https://github.com/athensresearch/athens/commit/1900e1017ef34728b17ddff8b6e15e5e14a1a79a)), closes [/github.com/storybookjs/storybook/issues/14440#issuecomment-814326123](https://github.com/athensresearch//github.com/storybookjs/storybook/issues/14440/issues/issuecomment-814326123)
* src paths for comps should match src/js depth ([fcb39ec](https://github.com/athensresearch/athens/commit/fcb39ec700cd90997335e487752a2940567fdf0f))
* **storybook:** add missing config ([c522c5d](https://github.com/athensresearch/athens/commit/c522c5d6ce3f0e1c9bedafd82e0054c887478e54))
* **storybook:** add missing mock data ([52fe409](https://github.com/athensresearch/athens/commit/52fe409687b815637e37fae5385c84a46f86531c))
* **storybook:** add missing rename changes ([1351e0c](https://github.com/athensresearch/athens/commit/1351e0c885df9724d5cb5e6ca9b11c0404103846))
* **storybook:** add missing shadows to block tools ([f5d42e9](https://github.com/athensresearch/athens/commit/f5d42e9ccf95b1bbd9fb550caa3d792dbd01688e))
* **storybook:** add missing spacing in cmd bar button ([0e1e832](https://github.com/athensresearch/athens/commit/0e1e832cd73aa40c76d8e3518dbcf2395240ba00))
* **storybook:** apply base styles to body ([7ed2d4a](https://github.com/athensresearch/athens/commit/7ed2d4a888dd7773a6db893d9c84c8a7c764679e))
* **storybook:** attempt fix for uneditable blocks ([3a8a6af](https://github.com/athensresearch/athens/commit/3a8a6affc312a0c4dcf695706f1f5ea12e5c4acf))
* **storybook:** avatars appear correctly ([f491c6d](https://github.com/athensresearch/athens/commit/f491c6d2e8122072202e5ff084819a50262ab3c6))
* **storybook:** better example linked block text ([02524fc](https://github.com/athensresearch/athens/commit/02524fca29bcabd06c6902b873bd968b59da908e))
* **storybook:** better story background ([896c1ee](https://github.com/athensresearch/athens/commit/896c1ee86e7324d78cc000feabccdccf2893ac92))
* **storybook:** blocks open by default ([26d4f93](https://github.com/athensresearch/athens/commit/26d4f93b1d1145a381b6ad96c8b1991e58517265))
* **storybook:** buttons passthrough classname ([3737c33](https://github.com/athensresearch/athens/commit/3737c335cd0ce08608a36ae4491c11e38edab141))
* **storybook:** clean up button story ([5b29b1a](https://github.com/athensresearch/athens/commit/5b29b1a8b75c74eb218ab715f4c42b1220d6eab4))
* **storybook:** commandbar heading appears above text ([96cf6d6](https://github.com/athensresearch/athens/commit/96cf6d68fe376635f79a00600d4a35783841d013))
* **storybook:** correct minor issues with browser and standalone stories ([d8e27fc](https://github.com/athensresearch/athens/commit/d8e27fcfd2ccf2d730ef780f0a3b5f143af07ebd))
* **storybook:** don't use broken story wrapper ([ae62c45](https://github.com/athensresearch/athens/commit/ae62c45095645f94dacd9e3bcdf23e1f3aab9dd6))
* **storybook:** finally solve theme context issue ([36a7cca](https://github.com/athensresearch/athens/commit/36a7cca0258f29a694e5aab6166dc0cf0c0feccb))
* **storybook:** fix a bunch of button bugs ([4c5eb62](https://github.com/athensresearch/athens/commit/4c5eb62e9293c358db9661eedd848dfcd0df23ce))
* **storybook:** fix a bunch of button bugs ([94d09a9](https://github.com/athensresearch/athens/commit/94d09a903e9a51a691ec73b515d30d0a6e59bd5b))
* **storybook:** fix avatar positioning on blocks ([b9ac583](https://github.com/athensresearch/athens/commit/b9ac5835c775eb8541b3d991aa86d8d018df0612))
* **storybook:** fix block recurse limiter ([a4ffe47](https://github.com/athensresearch/athens/commit/a4ffe4726b92dea2ad1f03db87d291c6ece28f59))
* **storybook:** generated blocks toggle properly ([7af9c8c](https://github.com/athensresearch/athens/commit/7af9c8cb2e036cb18ba627715e4c4c23dec77a44))
* **storybook:** get proper mock data for toolbar ([9784052](https://github.com/athensresearch/athens/commit/9784052a6ec16b1d0a00f64b3378974a6b57dee1))
* **storybook:** get proper mock data for toolbar ([ce4fb9f](https://github.com/athensresearch/athens/commit/ce4fb9ff3b4153e5bcbd15a527b1b2f5dd0d196c))
* **storybook:** hide command bar on click behind ([d56c28b](https://github.com/athensresearch/athens/commit/d56c28bbaf451916be3f0d3468d9fa268d4830d9))
* **storybook:** include missing shadow style ([9109914](https://github.com/athensresearch/athens/commit/91099146f877448ecd1f7c6ecab22845a3f707f9))
* **storybook:** make os stories work again ([617bf5c](https://github.com/athensresearch/athens/commit/617bf5ceaa759fb9a0d684bbac78b2114168f688))
* **storybook:** make window buttons visible in linux ([b4d4c20](https://github.com/athensresearch/athens/commit/b4d4c20d9c5c9b15a085d28e4606b277b997a916))
* **storybook:** minor cleanup ([f8f6f26](https://github.com/athensresearch/athens/commit/f8f6f26d2215506093dc9c7eecb1460d85958adb))
* **storybook:** minor fixes to style generation ([55dc649](https://github.com/athensresearch/athens/commit/55dc6494aa13510c4a6f451332b0a0c6c6c58636))
* **storybook:** minor layout fix ([4c97d4b](https://github.com/athensresearch/athens/commit/4c97d4b3d13da55f3a1f871f6998e885097f5617))
* **storybook:** minor presence cleanup ([5d3a8b2](https://github.com/athensresearch/athens/commit/5d3a8b28c4dc9c934c47ade11ceffedb54deaf1b))
* **storybook:** page menu toggle should be round ([30a24ed](https://github.com/athensresearch/athens/commit/30a24ed4c66124e505e446066c5564af74c7a5d6))
* **storybook:** permute opacities into style map ([9df8ce7](https://github.com/athensresearch/athens/commit/9df8ce7c5c6cece028910a8f5597d285178f257a))
* **storybook:** presence works on standalone story ([a1ed4a9](https://github.com/athensresearch/athens/commit/a1ed4a9723165054c892935a529d3fb990fdb7bd))
* **storybook:** properly align left sidebar footer bits ([6fa1784](https://github.com/athensresearch/athens/commit/6fa1784b801d79706669aff1a2e7119aa2f7d649))
* **storybook:** properly capture focus on presence overlay ([059dfc7](https://github.com/athensresearch/athens/commit/059dfc755451d412043322d9606d5b6b9368fa7a))
* **storybook:** properly place page content ([8f1161d](https://github.com/athensresearch/athens/commit/8f1161daea3b6c6cb0dd58d00988ae295ed558d8))
* **storybook:** relink failing page story ([54d3612](https://github.com/athensresearch/athens/commit/54d3612a43a9e05daddfc451975460184ad88d45))
* **storybook:** remove autocomplete and suggest from command bar ([226d449](https://github.com/athensresearch/athens/commit/226d4495763b42c003688b56a635ebdfdf5ab01f))
* **storybook:** remove duplicate avatar element from block ([695e151](https://github.com/athensresearch/athens/commit/695e151ef9a677b808b364ffc2bbaefbd9c2adea))
* **storybook:** selected block backgrounds no longer stack ([afb3b40](https://github.com/athensresearch/athens/commit/afb3b40eea001748d897c4a663fa659db3f0898c))
* **storybook:** solve issue with plain button ([aca8ba4](https://github.com/athensresearch/athens/commit/aca8ba457de668a9820750a04aab989053214376))
* **storybook:** use propber background for stories ([d66861d](https://github.com/athensresearch/athens/commit/d66861d8a1f163cad086c5afb7c2b79ae6a204e0))
* **storybook:** use updated link color in dark mode too ([cf6e468](https://github.com/athensresearch/athens/commit/cf6e46844b8b0361b4f885153611b83d25b35ce2))
* **storybook:** use user color for block side border ([c1c7163](https://github.com/athensresearch/athens/commit/c1c7163bef86e439323fff779b604079abcc0907))
* **storybook:** various issues with app layout and page spacing ([a9eeb36](https://github.com/athensresearch/athens/commit/a9eeb36b429159f564cde627a358909f2400dc7e))
* **style:** properly support nested theme colors ([c38fc23](https://github.com/athensresearch/athens/commit/c38fc234e40fed5314066c3409b536ded8c69a7c))


### Refactors

* **blocks:** use hooks for added block features ([3470e93](https://github.com/athensresearch/athens/commit/3470e93e7b6287b31c79f988a2c8d5563cb875d7))
* **dbicon:** move icon into components root ([8aee7c7](https://github.com/athensresearch/athens/commit/8aee7c7e975912ff0ca43b936d79f9240d8de7cc))
* **docs:** use decorators for whole-app story wrappers ([f23718f](https://github.com/athensresearch/athens/commit/f23718fa8a4a5a0cda1f6a5da1bc0ed4255806d1))
* move concept components to concept stories header ([f1827c4](https://github.com/athensresearch/athens/commit/f1827c46e5decf8654a83f87597ba52acd4f3305))
* **page, blocks:** clearer component relations ([92afde2](https://github.com/athensresearch/athens/commit/92afde244172476b279a494a3632018a6601dc5d))
* **storybook:** add mock data and app state ([2977b19](https://github.com/athensresearch/athens/commit/2977b194adbf3501419f0777c3c827c0fe930bbd))
* **storybook:** another round of button fixes ([26b36c6](https://github.com/athensresearch/athens/commit/26b36c65be166196e5b7cfa0e56631869c783f7e))
* **storybook:** better fn for block rendering ([1c4d243](https://github.com/athensresearch/athens/commit/1c4d2432f8af5e063ed107725ecd52e3bdd41701))
* **storybook:** better mock data for database menu ([ffa4677](https://github.com/athensresearch/athens/commit/ffa46770b010d9cb32f3c62929dcebe00dd2ef2c))
* **storybook:** better story organization ([5d5bf56](https://github.com/athensresearch/athens/commit/5d5bf56248f5023c3a1c377322c204347eb19e9d))
* **storybook:** blocks render from structured json ([6bcb7d4](https://github.com/athensresearch/athens/commit/6bcb7d489a593d1088519b68af5a35ad9b59ce47))
* **storybook:** button now pure styled-component for easier overrides ([1c7cd4d](https://github.com/athensresearch/athens/commit/1c7cd4d442decd655ed7f6047dcce74843686d34))
* **storybook:** button now pure styled-component for easier overrides ([c12c6d5](https://github.com/athensresearch/athens/commit/c12c6d50637cda7a0a1c857cf400fb3595a93d47))
* **storybook:** improve block tree recursing ([f80f8c6](https://github.com/athensresearch/athens/commit/f80f8c60f4d9cfe232983dd25194bd9e9fa77c37))
* **storybook:** lift db and presence components ([22caefd](https://github.com/athensresearch/athens/commit/22caefda8115dac70c0d3cf0d55f10d5d23d12f0))
* **storybook:** make recurseblocks function more available ([18d4e9d](https://github.com/athensresearch/athens/commit/18d4e9de6b54c086fdf209a68e91180fc387f41a))
* **storybook:** move block data to mockdata file ([1f03eda](https://github.com/athensresearch/athens/commit/1f03edae32a1f5df1c260e912bebde12c6a57c79))
* **storybook:** move maincontent into app components ([ffe254e](https://github.com/athensresearch/athens/commit/ffe254e3f74c4cd5fec046a4e7fbd5254a622bac))
* **storybook:** move most components under concept dir ([5424b00](https://github.com/athensresearch/athens/commit/5424b00e7d0e14f48822979c5cb9b7c4140f430e))
* **storybook:** move storybook state to app hook ([3f2ecd7](https://github.com/athensresearch/athens/commit/3f2ecd7b8628550a5d3f0879b38ab6f106e7e128))
* **storybook:** rebuild avatars ([0fa1e29](https://github.com/athensresearch/athens/commit/0fa1e2945ee88ed3db3bfbf7f56ff1383e32a595))
* **storybook:** refactor block stories ([a0a35e5](https://github.com/athensresearch/athens/commit/a0a35e58248ea5f277d84e8ea11470aec96a4e07))
* **storybook:** reimplement page menu with proper focus and portal ([83beef8](https://github.com/athensresearch/athens/commit/83beef8dbdd886205535efbf4a19903fcbc439d3))
* **storybook:** reorganize link story component ([ecda273](https://github.com/athensresearch/athens/commit/ecda2730b4775e78d3796a3d96e2282b126faa64))
* **storybook:** use hooks for block fns ([8f60c9e](https://github.com/athensresearch/athens/commit/8f60c9e1486c1f2691a103e76bf69411f72249b2))
* **storybook:** use simpler more consistent desktop story wrapper ([0c356e5](https://github.com/athensresearch/athens/commit/0c356e5dafbe35dcccb9c6e3170ffd74dbb14580))
* use new button component ([c9e2ff5](https://github.com/athensresearch/athens/commit/c9e2ff5e2f65241b109db5acf27831102e4b3fb5))
* use root relative imports for components ([454c130](https://github.com/athensresearch/athens/commit/454c1302405261b136c8db7cf356a2869e3a487c))


* add operating system type ([3d377de](https://github.com/athensresearch/athens/commit/3d377de2665d1630dfa46a6358a4e7cfa52fbaef))
* fix import semantics for components ([65cd0e4](https://github.com/athensresearch/athens/commit/65cd0e4b66c27d16a9969a1e2460496eec8e63bd))
* fix misc type issues ([fecd599](https://github.com/athensresearch/athens/commit/fecd599ead0cfbf9362d1f4aba751bce73dc1d83))
* fix spacing ([da4b75a](https://github.com/athensresearch/athens/commit/da4b75a273e45eb83bf2ad1f15458063af6b9beb))
* formatting updates ([a2b006d](https://github.com/athensresearch/athens/commit/a2b006d5d4dbe1927df42dcd58e633c6d52b404e))
* merge from upstream changes ([1bcd729](https://github.com/athensresearch/athens/commit/1bcd729a9a973e444badde05219022af4220c807))
* output babel compiled files in dist/js ([ddff507](https://github.com/athensresearch/athens/commit/ddff507a1b7ed7eaa2268c2fd79798cc53e35dbb))
* **storybook:** clean up block mock data ([3062065](https://github.com/athensresearch/athens/commit/3062065761258b4dec54b31c46c657ae3b5c397f))
* **storybook:** clean up misc warnings ([5d5240d](https://github.com/athensresearch/athens/commit/5d5240d561d063c4099d328c8191eb1da18ffca6))
* **storybook:** clean up page story ([ad26746](https://github.com/athensresearch/athens/commit/ad26746d08f3349687bd69d501038ca9a39396b4))
* **storybook:** clean up preview component code ([935ce58](https://github.com/athensresearch/athens/commit/935ce58d3202c5775e3f6809494b78b69135d156))
* **storybook:** mark components as in-use ([5eb4038](https://github.com/athensresearch/athens/commit/5eb403855e97efb013257be9459d3bdc94715482))
* **storybook:** minor block cleanup ([0072c34](https://github.com/athensresearch/athens/commit/0072c3485019aae9bbacd512ff3f0e978c6ba270))
* **storybook:** minor block mockdata cleanup ([5b82a19](https://github.com/athensresearch/athens/commit/5b82a19b9006a57971a3640028d8756c21f97546))
* **storybook:** minor cleanup of app stories ([fa761c8](https://github.com/athensresearch/athens/commit/fa761c846995e27644f47ad25f7db9412436c887))
* **storybook:** minor utility cleanup ([af89497](https://github.com/athensresearch/athens/commit/af894974e25e1fa5cda9637e37e241ce6cf6d075))
* **storybook:** misc minor story fixes ([5255e98](https://github.com/athensresearch/athens/commit/5255e982eaed6cf7d00231208fab1053d92923ed))
* **storybook:** raise tsconfig root level ([6dc58f3](https://github.com/athensresearch/athens/commit/6dc58f3648492303c52d4645f54eb4c6397edfdd))
* **storybook:** remove manual checkbox style ([4890ce7](https://github.com/athensresearch/athens/commit/4890ce7c9e7c82c9f013b8ad0c306315b178cd12))
* **storybook:** remove unused files ([c9102e8](https://github.com/athensresearch/athens/commit/c9102e8c027ac3327f143341016b6a82b8e7059f))
* **storybook:** remove unused import ([8caedb0](https://github.com/athensresearch/athens/commit/8caedb043809859d495e25bb6377a43e1481069f))
* **storybook:** resolve misc errors and issues ([4fb74d1](https://github.com/athensresearch/athens/commit/4fb74d1cc1f070f84e7d4924e11df3200b2abf52))
* **storybook:** upgrade storybook ([5480037](https://github.com/athensresearch/athens/commit/54800378e1e727dda35b14ce469fd8ef99d06391))
* support @ as root alias in js components ([403e86a](https://github.com/athensresearch/athens/commit/403e86a1f9cb780cfe3ef9c1f71d3ca61d5fe2d1))
* support tsx components and storybook ([59c6629](https://github.com/athensresearch/athens/commit/59c662915e5efebc2df9293a01d5f675dcedacf8))

## [1.0.0-alpha.rtc.3](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.2...v1.0.0-alpha.rtc.3) (2021-09-09)


### Features

* notify client that lan-party is over 😢 ([864582a](https://github.com/athensresearch/athens/commit/864582a639ca06dc1c2a47eb7a6f9076b094af86))
* replace block refs with content ([80f2be0](https://github.com/athensresearch/athens/commit/80f2be03443eb6213c5fe8e4660698a2ce1fc053))
* replace block refs with text on ref delete ([8529546](https://github.com/athensresearch/athens/commit/8529546b012b2f63b00dde02b950e18fd4245334))
* unlink multiple block refs ([57130cd](https://github.com/athensresearch/athens/commit/57130cd1c5b5bc63689cc48dfffef96c17232552))
* use one event handler for conn-status ([6afd0fc](https://github.com/athensresearch/athens/commit/6afd0fcdb25a0d3af42d302448d9fca2f3824a71))


### Bug Fixes

* `docker-compose` setup timing-out. ([50bbc0d](https://github.com/athensresearch/athens/commit/50bbc0daf6a7da24f689f05bf6f6e9799d2f5046))
* block refs replace failing in single uid ([02d913d](https://github.com/athensresearch/athens/commit/02d913dfb34c434d69626df58a78996f5bf9d5df))
* bullet too big ([ffd026d](https://github.com/athensresearch/athens/commit/ffd026de803366bdeee49a3534486e5d912a3b1a))
* bullet too small ([49439d5](https://github.com/athensresearch/athens/commit/49439d52b01060999e5fe56623a37547ff3b7414))
* detect when client is not able to connect to remote. ([1bddf89](https://github.com/athensresearch/athens/commit/1bddf8973aa4aaf2a02738bd64cac88c679784f2))
* error when deleting a block with block ref ([f6cba22](https://github.com/athensresearch/athens/commit/f6cba22ced3f585a09d90101306f805b56042bc7))
* import react-dom instead of using missing global ([70027b9](https://github.com/athensresearch/athens/commit/70027b95188eb0e2c5a5a7ccfdbc20337641e00c)), closes [/github.com/athensresearch/athens/pull/1564#discussion_r704963708](https://github.com/athensresearch//github.com/athensresearch/athens/pull/1564/issues/discussion_r704963708)
* Make backend logs a bit more readable. ([2d79531](https://github.com/athensresearch/athens/commit/2d79531685520afab6848b1c5fdcc6b8f96b62db))
* Remote client freezes when navigating down. ([ab910a1](https://github.com/athensresearch/athens/commit/ab910a1c014f6b271a3742c89774f635c0c31bf2))
* schema of selected-delete ([d135dbd](https://github.com/athensresearch/athens/commit/d135dbdac068f0ec21a92beeafca25de5803dcb0))
* use vector to convert title ([084b168](https://github.com/athensresearch/athens/commit/084b168940a78cb9a7ec5094822fd2b75a44456f))


* add basic stress test ([29ae7d8](https://github.com/athensresearch/athens/commit/29ae7d87ee4837b86900dfe50fb84da69c6d5e95))
* add stress test ([63ed490](https://github.com/athensresearch/athens/commit/63ed4909fcd4b2f5db4efe39a79796f0bf506721))
* Add Vercel previews ([#1643](https://github.com/athensresearch/athens/issues/1643)) ([c860ab4](https://github.com/athensresearch/athens/commit/c860ab465fca94eefe1aa5044c54a58b04bc4c03))
* build components once before starting client watch ([9fa0690](https://github.com/athensresearch/athens/commit/9fa0690a817fd784ecdf628c6209f4cdf7d2b787))
* fix spacing ([dd329df](https://github.com/athensresearch/athens/commit/dd329df9cb61613a06d905d174e6619980df904b))
* linked refs text replacement ([35200af](https://github.com/athensresearch/athens/commit/35200af3e76e8d05ec90705946a84c4eb93eb0c3))
* support tsx components and storybook ([#1564](https://github.com/athensresearch/athens/issues/1564)) ([925f7a4](https://github.com/athensresearch/athens/commit/925f7a4c089d8d64a1082da2a452d5b7dc008ba1))

## [1.0.0-alpha.rtc.2](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.1...v1.0.0-alpha.rtc.2) (2021-08-26)


### Bug Fixes

* A bit more logging while we're debugging. ([a8903b5](https://github.com/athensresearch/athens/commit/a8903b5c2503134fdf6bd80b8e3f6aea12b1baa1))
* Block doesn't render when clicking outside ([12c039a](https://github.com/athensresearch/athens/commit/12c039a9a5623d322ef52f9c7a65d134bc794b29)), closes [#1491](https://github.com/athensresearch/athens/issues/1491)
* **bullets:** make bullets same size across zoom levels ([4b5d877](https://github.com/athensresearch/athens/commit/4b5d8770e26153c598e9f706530c0ec0c635be25))
* Can't delete multiple blocks at once ([c8825e6](https://github.com/athensresearch/athens/commit/c8825e6442311c95509d5bce66681cb257c72f68)), closes [#1516](https://github.com/athensresearch/athens/issues/1516)
* Daily pages creation from Daily Pages. ([287fa74](https://github.com/athensresearch/athens/commit/287fa74cd5420c04815de7004746f2e652df1411))
* Log error when handler doesn't return handle status event. ([f668448](https://github.com/athensresearch/athens/commit/f6684487c9d123971030cac5ca26f78606c4b74e))
* make bullet look closer to previous collapsed bullets ([ee892fc](https://github.com/athensresearch/athens/commit/ee892fcc5f2b14f61dc5d7a8027da09838fd0232))
* nav-daily-notes and daily-note/reset ([765d6fc](https://github.com/athensresearch/athens/commit/765d6fc1ed56feae1cdc5077c2b2410ae7fd71c1))
* Presence broadcast new username when new player introduces. ([6e46576](https://github.com/athensresearch/athens/commit/6e4657657ec5e80993366e85de5e5aecc34ad66e))
* Presence confirm `:presence/editing` & plain keywords for args. ([65ffae2](https://github.com/athensresearch/athens/commit/65ffae25e6523150295a3ef03b2904ac6382ed51))
* problems in the daily page scroll ([8bf3e6b](https://github.com/athensresearch/athens/commit/8bf3e6b60d1497d9c8fb0b6034c968b9fd1cbc48))
* remove unused storage icon ([dcc5e16](https://github.com/athensresearch/athens/commit/dcc5e16fca44ce2478f0652a40e659e0b8842e09))
* Selection behaviour fixed. [#1571](https://github.com/athensresearch/athens/issues/1571) ([a7fd284](https://github.com/athensresearch/athens/commit/a7fd284e019c5d7381a1f98ee57163c989f4c1ee))
* Selection clear was resetting `re-frame` db. ([377e417](https://github.com/athensresearch/athens/commit/377e417a778d7a5c0673dc904b6d243ea69e9560))
* some bullets are not round ([56d608f](https://github.com/athensresearch/athens/commit/56d608f2a74f9dab2798c601dc4495335a930e2e))
* Some bullets are not round ([f583bc5](https://github.com/athensresearch/athens/commit/f583bc565c1784b02d2460315ab841f07f08a0f8))
* Some bullets are not round ([1370fec](https://github.com/athensresearch/athens/commit/1370fec2996f841c2ef15c1cc321e5c428c5e0fe)), closes [#1495](https://github.com/athensresearch/athens/issues/1495)
* update daily page after creation ([e9b140e](https://github.com/athensresearch/athens/commit/e9b140eb3b7b16d2ebea0581f93e0cff2e62564f))


* alignment ([384cad6](https://github.com/athensresearch/athens/commit/384cad62fa53c205878156fd83ed92a996f689a9))
* comment and deploy gh pages to main ([cdac939](https://github.com/athensresearch/athens/commit/cdac9396078f65fb7ed8fe3568659510218aec7d))
* fix ([87c016c](https://github.com/athensresearch/athens/commit/87c016c526119d8fb9bcfb976ef91e5f393b5132))
* only auto-update releases from the main branch ([d98719d](https://github.com/athensresearch/athens/commit/d98719dffab2b5c4e128aeffa2dae62caba11e86))
* only deploy to gh-pages on master ([a29bb6f](https://github.com/athensresearch/athens/commit/a29bb6f776c3be7dd0ddd8c13b47e886ba0a95bd))

## [1.0.0-alpha.rtc.1](https://github.com/athensresearch/athens/compare/v1.0.0-alpha.rtc.0...v1.0.0-alpha.rtc.1) (2021-08-17)


### Bug Fixes

* synced hopefully all the events ([bdf3bf4](https://github.com/athensresearch/athens/commit/bdf3bf4e256c775b32ac8a58d48a54e6b84f2e60)), closes [#1506](https://github.com/athensresearch/athens/issues/1506)


### Refactors

* simplify vector concat -> into ([444bfb9](https://github.com/athensresearch/athens/commit/444bfb9d406d131db6b7e528527077f1c548bb95)), closes [#1498](https://github.com/athensresearch/athens/issues/1498)


* only release jar on ubuntu build ([03b869c](https://github.com/athensresearch/athens/commit/03b869cc44cbef6e32cd33c7eea879426c1f906a))


## [1.0.0-beta.94](https://github.com/athensresearch/athens/compare/v1.0.0-beta.93...v1.0.0-beta.94) (2021-08-04)

## [1.0.0-beta.93](https://github.com/athensresearch/athens/compare/v1.0.0-beta.92...v1.0.0-beta.93) (2021-08-04)

## [1.0.0-beta.92](https://github.com/athensresearch/athens/compare/v1.0.0-beta.91...v1.0.0-beta.92) (2021-08-02)

## [1.0.0-beta.91](https://github.com/athensresearch/athens/compare/v1.0.0-beta.90...v1.0.0-beta.91) (2021-08-02)

## [1.0.0-beta.90](https://github.com/athensresearch/athens/compare/v1.0.0-beta.89...v1.0.0-beta.90) (2021-08-02)


### Bug Fixes

* "open all-pages view" keybinding on Welcome ([#1327](https://github.com/athensresearch/athens/issues/1327)) ([f411c53](https://github.com/athensresearch/athens/commit/f411c53b699e96ca71c52631e652f6dff4fbcf73))
* Selection fixes. ([#1421](https://github.com/athensresearch/athens/issues/1421)) ([c835c79](https://github.com/athensresearch/athens/commit/c835c793bffa79d98d25007b2b525ce514f9f51f)), closes [#1279](https://github.com/athensresearch/athens/issues/1279)


### Enhancements

* **toolbar:** use native operating system toolbar ([#1120](https://github.com/athensresearch/athens/issues/1120)) ([e2c953b](https://github.com/athensresearch/athens/commit/e2c953b5c2bc9257939784499b0755c81b89c89b))

## [1.0.0-beta.89](https://github.com/athensresearch/athens/compare/v1.0.0-beta.88...v1.0.0-beta.89) (2021-06-09)


### Bug Fixes

* when editing long block-page title, title overlaps with child block ([#1291](https://github.com/athensresearch/athens/issues/1291)) ([ed9568b](https://github.com/athensresearch/athens/commit/ed9568bbe326b0e4afcb99c8415f1337425e59c5))
* **#1107:** drop support for *** for thematic-break ([#1203](https://github.com/athensresearch/athens/issues/1203)) ([b1d978e](https://github.com/athensresearch/athens/commit/b1d978e4d2893f0fc5f8612ef4737794c68c73fe)), closes [#1107](https://github.com/athensresearch/athens/issues/1107)
* **#1277:** compare string without dangerous regex injection for slash cmd ([#1287](https://github.com/athensresearch/athens/issues/1287)) ([c1c4c2a](https://github.com/athensresearch/athens/commit/c1c4c2a11cded25017cbba4a9ca15f660cef032f)), closes [#1277](https://github.com/athensresearch/athens/issues/1277) [#1277](https://github.com/athensresearch/athens/issues/1277)
* **click:** Clicking inside a block with hyperlink works on first attempt ([#1236](https://github.com/athensresearch/athens/issues/1236)) ([c619ecf](https://github.com/athensresearch/athens/commit/c619ecfe477cc8b503d504e32daf7e4e5f317f1d))

## [1.0.0-beta.88](https://github.com/athensresearch/athens/compare/v1.0.0-beta.87...v1.0.0-beta.88) (2021-06-06)


### Bug Fixes

* **#1289:** remove autocomplete on backspace of [[ or (( ([eacd2a3](https://github.com/athensresearch/athens/commit/eacd2a322bad0c2901d35f3d1d01e78d5040ca61)), closes [#1289](https://github.com/athensresearch/athens/issues/1289) [#1289](https://github.com/athensresearch/athens/issues/1289)

## [1.0.0-beta.87](https://github.com/athensresearch/athens/compare/v1.0.0-beta.86...v1.0.0-beta.87) (2021-06-06)


### Features

* **athena:** Shift+Click opens in right sidebar, not just Shift+Enter ([#1272](https://github.com/athensresearch/athens/issues/1272)) ([13274dc](https://github.com/athensresearch/athens/commit/13274dc521b7c0e967f75a1b1dd657eab0dcf16c))


### Bug Fixes

* **blocks:** child drop-area-indicator shouldn't squish or be misplaced ([#1264](https://github.com/athensresearch/athens/issues/1264)) ([b04c5a2](https://github.com/athensresearch/athens/commit/b04c5a2eb55438988d697fc1dbf4f6e258966507))


### Enhancements

* **settings:** clarify language around autosave backups ([#1285](https://github.com/athensresearch/athens/issues/1285)) ([de86794](https://github.com/athensresearch/athens/commit/de867941dbd512a56536cd38f4b22b208941f323))


### Documentation

* Create a Contributor Covenant Code of Conduct ([#1210](https://github.com/athensresearch/athens/issues/1210)) ([f5d6a27](https://github.com/athensresearch/athens/commit/f5d6a27eaf692be222e9a264b845854e3654ec54))

## [1.0.0-beta.86](https://github.com/athensresearch/athens/compare/v1.0.0-beta.85...v1.0.0-beta.86) (2021-06-02)


### Bug Fixes

* **selection:** Selection "freeze" ([#1273](https://github.com/athensresearch/athens/issues/1273)) ([b66bfa8](https://github.com/athensresearch/athens/commit/b66bfa84859088940a1d6280f61ced79196162be))


### Performance

* Only run the listener in dev mode ([#1215](https://github.com/athensresearch/athens/issues/1215)) ([b276963](https://github.com/athensresearch/athens/commit/b276963b6096887f68005073bd574619d1623953))


### Documentation

* update one liner in package.json and project.clj ([#1258](https://github.com/athensresearch/athens/issues/1258)) ([1b78701](https://github.com/athensresearch/athens/commit/1b7870161c0ded543ca073cae67af2ec4f6427fd))

## [1.0.0-beta.85](https://github.com/athensresearch/athens/compare/v1.0.0-beta.84...v1.0.0-beta.85) (2021-05-28)


* upgrade yarn deps alongside lein deps, fix demo ([#1246](https://github.com/athensresearch/athens/issues/1246)) ([c1b6195](https://github.com/athensresearch/athens/commit/c1b619519f1e46bd4f5e9dcae6bf86dac9382b77))

## [1.0.0-beta.84](https://github.com/athensresearch/athens/compare/v1.0.0-beta.83...v1.0.0-beta.84) (2021-05-28)


* downgrade re-frame-10x so web version works, comment out deps-check ([#1244](https://github.com/athensresearch/athens/issues/1244)) ([5fd5763](https://github.com/athensresearch/athens/commit/5fd57630c2bc76efba7c73d6b75cb33524935c52))

## [1.0.0-beta.83](https://github.com/athensresearch/athens/compare/v1.0.0-beta.82...v1.0.0-beta.83) (2021-05-28)


### Bug Fixes

* **auto-complete:** pressing [[,(( and enter works better. close [#1214](https://github.com/athensresearch/athens/issues/1214), [#1220](https://github.com/athensresearch/athens/issues/1220) ([#1219](https://github.com/athensresearch/athens/issues/1219)) ([856b282](https://github.com/athensresearch/athens/commit/856b282f00a292b206afa5164a8901a15a3a9203)), closes [#1204](https://github.com/athensresearch/athens/issues/1204)


* update deps and cljstyle fix ([#1224](https://github.com/athensresearch/athens/issues/1224)) ([181ad52](https://github.com/athensresearch/athens/commit/181ad52286d982586a9c1f5016d37553915b6b05))


### Enhancements

* **ui:** Stop content shift when scrollbars appear/disappear ([#1212](https://github.com/athensresearch/athens/issues/1212)) ([9988417](https://github.com/athensresearch/athens/commit/9988417ca3d1298031bfee933366a9969a548910))


### Documentation

* Fix grammar in README ([#1235](https://github.com/athensresearch/athens/issues/1235)) ([e55f3f8](https://github.com/athensresearch/athens/commit/e55f3f82e328ee6b1ca758ac3bd5491f717680c5))


### Performance

* **blocks:** reduce blocks DOM weight ([#1217](https://github.com/athensresearch/athens/issues/1217)) ([7b922a5](https://github.com/athensresearch/athens/commit/7b922a523991fd5f59f69422a34feb47a9a6c758))
* **right-sidebar:** fix memory+time leak with proper GC of sorted-map [#1239](https://github.com/athensresearch/athens/issues/1239) ([#1242](https://github.com/athensresearch/athens/issues/1242)) ([32d66f5](https://github.com/athensresearch/athens/commit/32d66f5e514531a080454dc337cf7535381865fb))

## [1.0.0-beta.82](https://github.com/athensresearch/athens/compare/v1.0.0-beta.81...v1.0.0-beta.82) (2021-05-20)


### Bug Fixes

* **undo:** undo after pair character input ([#1194](https://github.com/athensresearch/athens/issues/1194)) ([b635da0](https://github.com/athensresearch/athens/commit/b635da00d98c296a533510ef4e47fb021800c75b)), closes [#559](https://github.com/athensresearch/athens/issues/559)
* **unlinked-refs:** update unlinked refs when page changes ([#1195](https://github.com/athensresearch/athens/issues/1195)) ([5d7b0fe](https://github.com/athensresearch/athens/commit/5d7b0febca5fb9030378857a32fda08f7d876982))


### Performance

* **search:** faster search for (()), [[]] and ctrl-k ([#1191](https://github.com/athensresearch/athens/issues/1191)) ([5cfcb2a](https://github.com/athensresearch/athens/commit/5cfcb2a60bc0a5079690fcfb8674767d1a458720)), closes [#756](https://github.com/athensresearch/athens/issues/756) [#756](https://github.com/athensresearch/athens/issues/756)

## [1.0.0-beta.81](https://github.com/athensresearch/athens/compare/v1.0.0-beta.80...v1.0.0-beta.81) (2021-05-19)


### Features

* **app-toolbar:** updated filesystem/sync icons ([#1146](https://github.com/athensresearch/athens/issues/1146)) ([e2ba5d7](https://github.com/athensresearch/athens/commit/e2ba5d7f69d8c628df7a86edfd153fb23643a12b))
* **datalog-console:** respond to datalog-console messages in browser ([#1193](https://github.com/athensresearch/athens/issues/1193)) ([3ffb781](https://github.com/athensresearch/athens/commit/3ffb781fbfc44a37156a9290cd49a1e2ff631beb))
* **electron:** set min width and height for electron window ([#1173](https://github.com/athensresearch/athens/issues/1173)) ([f41c028](https://github.com/athensresearch/athens/commit/f41c0289357a06c3d6a5ad538eb0c731457606d5))
* **keybindings:** keybindings for Graph, All Pages, and Settings ([#1192](https://github.com/athensresearch/athens/issues/1192)) ([47efb81](https://github.com/athensresearch/athens/commit/47efb818a02a3e74e6e994337b0e1eb30c83199f))


### Bug Fixes

* **contentarea:** hide multiline text in contentarea ([#1189](https://github.com/athensresearch/athens/issues/1189)) ([dbaa1e5](https://github.com/athensresearch/athens/commit/dbaa1e5138640e2d88a702078e9e1bff408102a7))
* **keybindings:** place caret correctly after ctrl-i italics ([#1176](https://github.com/athensresearch/athens/issues/1176)) ([a11ea7b](https://github.com/athensresearch/athens/commit/a11ea7b8fc3aff2afccf970658adace05f5c7a13))
* **parser:** remove support for underscores so URLs can use ([dd5affb](https://github.com/athensresearch/athens/commit/dd5affb08f1c32efe1917704608bc7380c0df2ae))
* **roam-import:** fix roam-date regex to match ordinal numbers in roam dates more ([#1171](https://github.com/athensresearch/athens/issues/1171)) ([ebd9aac](https://github.com/athensresearch/athens/commit/ebd9aac89e73f2fb7c97bb79903945410c6fe925)), closes [#1135](https://github.com/athensresearch/athens/issues/1135)


* **parser:** add regression test for fixed issue [#1057](https://github.com/athensresearch/athens/issues/1057) ([#1175](https://github.com/athensresearch/athens/issues/1175)) ([e74c0c6](https://github.com/athensresearch/athens/commit/e74c0c6cfe4e4288b7a34ff2588d6d4ae434ddb6))


### Enhancements

* **all-pages:** add arrow UI and re-frame constructs ([#1152](https://github.com/athensresearch/athens/issues/1152)) ([d59198f](https://github.com/athensresearch/athens/commit/d59198ff7f99d2fb80a35e43231b3dfec560955e))

## [1.0.0-beta.80](https://github.com/athensresearch/athens/compare/v1.0.0-beta.79...v1.0.0-beta.80) (2021-05-13)


### Bug Fixes

* **keybindings:** redo sometimes does undo ([#1151](https://github.com/athensresearch/athens/issues/1151)) ([975afc0](https://github.com/athensresearch/athens/commit/975afc04df3422c8a519ef879a522eb0095a7862))


* add cljstyle alias to lein ([#1132](https://github.com/athensresearch/athens/issues/1132)) ([a64025a](https://github.com/athensresearch/athens/commit/a64025a3abd9b1ceaa6b1ebd246f06f9f9a79038))

## [1.0.0-beta.79](https://github.com/athensresearch/athens/compare/v1.0.0-beta.78...v1.0.0-beta.79) (2021-05-10)


### Bug Fixes

* Removal of a daily journal page creates a 404 ([#1094](https://github.com/athensresearch/athens/issues/1094)) ([640420f](https://github.com/athensresearch/athens/commit/640420f8fa81ccf88b0a3dc73b55f89949e91a87))
* **block-embed:** when block-embed is deleted, render uid instead of "invalid" ([#1093](https://github.com/athensresearch/athens/issues/1093)) ([23f1f93](https://github.com/athensresearch/athens/commit/23f1f93453bc090ec6ac4fe1e5562f1183f4365d))


### Enhancements

* **all-pages:** allow sort by titles / links / times ([#1105](https://github.com/athensresearch/athens/issues/1105)) ([2e6c548](https://github.com/athensresearch/athens/commit/2e6c54865e1a7b78419c639627aebf4cad621695))
* **daily-notes:** Shorter debounce time for loading of daily pages ([#1136](https://github.com/athensresearch/athens/issues/1136)) ([9dca967](https://github.com/athensresearch/athens/commit/9dca967ce1564f9c6c09a46f8d3324b6c9c81585))
* **left-sidebar:** clicking on the logo opens to issue creation rather than main repo [#1130](https://github.com/athensresearch/athens/issues/1130) ([82dd853](https://github.com/athensresearch/athens/commit/82dd853d1f9d253a315f8bc7aadcd9e625300367))
* **linked-refs:** sort references by newest first ([#1124](https://github.com/athensresearch/athens/issues/1124)) ([19fb97a](https://github.com/athensresearch/athens/commit/19fb97add8e744e24e7d7df3aec2238556cf22da)), closes [#728](https://github.com/athensresearch/athens/issues/728)

## [1.0.0-beta.78](https://github.com/athensresearch/athens/compare/v1.0.0-beta.77...v1.0.0-beta.78) (2021-04-29)


### Features

* **blocks:** improved visibility of hover/focus of block bullets ([#1046](https://github.com/athensresearch/athens/issues/1046)) ([84f971a](https://github.com/athensresearch/athens/commit/84f971a20a4a1d7ef510916a490b94ae5c83d572))
* **style:** improved highlight color consistency ([#1049](https://github.com/athensresearch/athens/issues/1049)) ([4546825](https://github.com/athensresearch/athens/commit/454682547046109d0cea3512d17d4fb3095397ec))


### Bug Fixes

* **breadcrumbs:** limit breadcrumb size ([#1047](https://github.com/athensresearch/athens/issues/1047)) ([a4686cc](https://github.com/athensresearch/athens/commit/a4686cc2d7978318e63c5ca5ab7618707a7e6048))
* **filesystem:** show hidden database merge button ([#1079](https://github.com/athensresearch/athens/issues/1079)) ([432efb9](https://github.com/athensresearch/athens/commit/432efb9e0e24035ce703a11043065913800581f8))
* use index.transit file if already exists ([#1044](https://github.com/athensresearch/athens/issues/1044)) ([865093f](https://github.com/athensresearch/athens/commit/865093f450dc70be3619f8c062dfbb398b815516))


### Refactors

* **node-page:** replace nodepage dropdown with popover ([#1045](https://github.com/athensresearch/athens/issues/1045)) ([2104370](https://github.com/athensresearch/athens/commit/2104370122e7d9036b6da613a2a850dd451c87ea))

## [1.0.0-beta.77](https://github.com/athensresearch/athens/compare/v1.0.0-beta.76...v1.0.0-beta.77) (2021-04-27)


### Bug Fixes

* **demo:** solve cause of toolbar button wrapping on demo page ([#1037](https://github.com/athensresearch/athens/issues/1037)) ([d0230f6](https://github.com/athensresearch/athens/commit/d0230f66b4277cc4046ed4c0eb2ac3c2d1002a3d))
* allow spaces in image urls ([#1034](https://github.com/athensresearch/athens/issues/1034)) ([aa07a57](https://github.com/athensresearch/athens/commit/aa07a57e8e1b4e2616bd66d8aa3daef3ccb053a8))
* ensure choose file input is visible in merge from roam dialog ([#1032](https://github.com/athensresearch/athens/issues/1032)) ([a69e470](https://github.com/athensresearch/athens/commit/a69e470f566ab37a0c6c0fe55c55c6760adfe0af))


### Documentation

* improve README ([#1018](https://github.com/athensresearch/athens/issues/1018)) ([68d7bf4](https://github.com/athensresearch/athens/commit/68d7bf44ec7541ea7e1db758b7c74ad03a954605))
* update blog links and faces ([5949786](https://github.com/athensresearch/athens/commit/5949786cd912568088f2dbd9813169edbf70c355))


### Refactors

* decompose blocks into its own namespace ([#1033](https://github.com/athensresearch/athens/issues/1033)) ([6023c44](https://github.com/athensresearch/athens/commit/6023c4420a263ebde16742d3d1de1d78b4df473a))
* group pages into new namespace ([#1005](https://github.com/athensresearch/athens/issues/1005)) ([e2046bf](https://github.com/athensresearch/athens/commit/e2046bf7d27a7cf41a3d30e499e9647a6b63520c))


### Enhancements

* **all-pages:** hide empty block bullets in all pages view ([#1040](https://github.com/athensresearch/athens/issues/1040)) ([6a308c5](https://github.com/athensresearch/athens/commit/6a308c5ab024502f1955a9965bc6ba15c5d77578))

## [1.0.0-beta.76](https://github.com/athensresearch/athens/compare/v1.0.0-beta.75...v1.0.0-beta.76) (2021-04-23)


### Bug Fixes

* allow editing of join remote fields ([#1019](https://github.com/athensresearch/athens/issues/1019)) ([3bf2707](https://github.com/athensresearch/athens/commit/3bf27075ca074ccfe7f768ea5d043268524679d7))

## [1.0.0-beta.75](https://github.com/athensresearch/athens/compare/v1.0.0-beta.74...v1.0.0-beta.75) (2021-04-22)


### Bug Fixes

* bugs introduced by new parser ([#1017](https://github.com/athensresearch/athens/issues/1017)) ([a2c7cb6](https://github.com/athensresearch/athens/commit/a2c7cb6b914b62562da0c811fdae246afa4c1cc2))

## [1.0.0-beta.74](https://github.com/athensresearch/athens/compare/v1.0.0-beta.73...v1.0.0-beta.74) (2021-04-22)


### Bug Fixes

* allow for multiple block-refs in a block. ([#1012](https://github.com/athensresearch/athens/issues/1012)) ([f26b719](https://github.com/athensresearch/athens/commit/f26b7195ed6c7975908b6f6640921dcf68274e23))

## [1.0.0-beta.73](https://github.com/athensresearch/athens/compare/v1.0.0-beta.72...v1.0.0-beta.73) (2021-04-21)


* **yarn.lock:** electron 11->12 needs new yarn.lock ([#1009](https://github.com/athensresearch/athens/issues/1009)) ([7239e51](https://github.com/athensresearch/athens/commit/7239e5176139a1c7d6672009325838cc97645376))

## [1.0.0-beta.72](https://github.com/athensresearch/athens/compare/v1.0.0-beta.70...v1.0.0-beta.72) (2021-04-21)


### Bug Fixes

* Shift+click title of reference won't open it in the right sidebar ([#995](https://github.com/athensresearch/athens/issues/995)) ([31e7bd0](https://github.com/athensresearch/athens/commit/31e7bd0a35074aa729d6e68033d665b1ab281497))


### Refactors

* **electron:** update from 11 to 12 ([#982](https://github.com/athensresearch/athens/issues/982)) ([d9d8cbf](https://github.com/athensresearch/athens/commit/d9d8cbf44cc47f15d2d8e19962425fdf165f302a))


### Enhancements

* **electron:** make bg default to dark ([#1003](https://github.com/athensresearch/athens/issues/1003)) ([b7f1c6e](https://github.com/athensresearch/athens/commit/b7f1c6e003d5309413b4908a37ce8b01de25a66f))
* toolbar, sidebar, and scrolling polish ([#998](https://github.com/athensresearch/athens/issues/998)) ([4cf0c99](https://github.com/athensresearch/athens/commit/4cf0c990f009ae334e96803887e9153cd2e07e38))

## [1.0.0-beta.71](https://github.com/athensresearch/athens/compare/v1.0.0-beta.70...v1.0.0-beta.71) (2021-04-21)


### Features

* **app-toolbar:** basic tooltips ([#994](https://github.com/athensresearch/athens/issues/994)) ([2996bad](https://github.com/athensresearch/athens/commit/2996bada15ba8fb1b3eab5ecbd1a25bc2aecc298))


### Bug Fixes

* `deref` error in web environment. ([#999](https://github.com/athensresearch/athens/issues/999)) ([fa186cd](https://github.com/athensresearch/athens/commit/fa186cdb5c6d7f8650ca41ab1e3dc7451905d3a2))


### Refactors

* **parser:** multi-stage parser ([#957](https://github.com/athensresearch/athens/issues/957)) ([9a30ce1](https://github.com/athensresearch/athens/commit/9a30ce139c88b2f412d2f2f8f0c9b597c73994c7))

![](https://media.discordapp.net/attachments/800579670629679165/834182913792671744/115297103-a681b880-a110-11eb-8bc1-5219441d4bbd.png?width=1440&height=610)

## [1.0.0-beta.70](https://github.com/athensresearch/athens/compare/v1.0.0-beta.69...v1.0.0-beta.70) (2021-04-20)


### Bug Fixes

* Delete a block and "undo" cannot bring it back ([#991](https://github.com/athensresearch/athens/issues/991)) ([a7958fc](https://github.com/athensresearch/athens/commit/a7958fcfbfcb326cdaa16e6a0391dbd1a0a2dc65))
* **right-sidebar:** scroll to top if open another page on right ([#992](https://github.com/athensresearch/athens/issues/992)) ([4fff704](https://github.com/athensresearch/athens/commit/4fff7048c9a295f69be0debaa8a657f12961b573))


### Enhancements

* **db-picker:** Polish db dialog ([#993](https://github.com/athensresearch/athens/issues/993)) ([a104161](https://github.com/athensresearch/athens/commit/a10416180597bada4752229421427069637bb604))

## [1.0.0-beta.69](https://github.com/athensresearch/athens/compare/v1.0.0-beta.68...v1.0.0-beta.69) (2021-04-18)


### Features

* self-hosted real-time collab and presence ([#930](https://github.com/athensresearch/athens/issues/930)) ([8393c47](https://github.com/athensresearch/athens/commit/8393c4764d7866cf1fb8b2651c9bac8263077e7c))


### Bug Fixes

* expand the breadcrumb when [[link]], don't navigate ([#976](https://github.com/athensresearch/athens/issues/976)) ([2d3c086](https://github.com/athensresearch/athens/commit/2d3c08624340494040e268cce4ae0688fda16422))
* non-formatted breadcrumb on page block ([#972](https://github.com/athensresearch/athens/issues/972)) ([c875b3d](https://github.com/athensresearch/athens/commit/c875b3d6b02256e949c0e105388eff79849403cc)), closes [#970](https://github.com/athensresearch/athens/issues/970)


### Performance

* **athena:** debounce athena search ([#975](https://github.com/athensresearch/athens/issues/975)) ([0eef6e2](https://github.com/athensresearch/athens/commit/0eef6e234aa50c9c1d56a0f6c808006eb6093e5c))

## [1.0.0-beta.68](https://github.com/athensresearch/athens/compare/v1.0.0-beta.66...v1.0.0-beta.68) (2021-04-12)


### Bug Fixes

* add missing toolbar background color ([#941](https://github.com/athensresearch/athens/issues/941)) ([e17c2c1](https://github.com/athensresearch/athens/commit/e17c2c10842c40383dbccb6afca0005ea8c270f7))
* athena should stay within viewport bounds ([#943](https://github.com/athensresearch/athens/issues/943)) ([8593b58](https://github.com/athensresearch/athens/commit/8593b58dcf6b4511eb8ff1c5013d23e61a48548e))
* block ref count is above the search dropdown ([#940](https://github.com/athensresearch/athens/issues/940)) ([d94e5fd](https://github.com/athensresearch/athens/commit/d94e5fdcbfacbfb9327faec0a996cbe971d3544e))
* Checking a TODO item in block embed results to error ([#939](https://github.com/athensresearch/athens/issues/939)) ([dc393d4](https://github.com/athensresearch/athens/commit/dc393d45bb855e1e48c2bf34c8e0122f43dbf9ce))
* clearer checkbox checked style ([#942](https://github.com/athensresearch/athens/issues/942)) ([eec391c](https://github.com/athensresearch/athens/commit/eec391c9f64136007f57f6edc525587fe98bffb4))


### Documentation

* remove all docs ([#919](https://github.com/athensresearch/athens/issues/919)) ([b5f2887](https://github.com/athensresearch/athens/commit/b5f2887a3434c4a63a924cb6157a393939356752))


### Enhancements

* **settings:** redo settings page ([#931](https://github.com/athensresearch/athens/issues/931)) ([c1ebd70](https://github.com/athensresearch/athens/commit/c1ebd70156fb241fea6f564a01e874b660dc7800))

## [1.0.0-beta.67](https://github.com/athensresearch/athens/compare/v1.0.0-beta.66...v1.0.0-beta.67) (2021-04-11)


### Bug Fixes

* add missing toolbar background color ([#941](https://github.com/athensresearch/athens/issues/941)) ([e17c2c1](https://github.com/athensresearch/athens/commit/e17c2c10842c40383dbccb6afca0005ea8c270f7))
* athena should stay within viewport bounds ([#943](https://github.com/athensresearch/athens/issues/943)) ([8593b58](https://github.com/athensresearch/athens/commit/8593b58dcf6b4511eb8ff1c5013d23e61a48548e))
* block ref count is above the search dropdown ([#940](https://github.com/athensresearch/athens/issues/940)) ([d94e5fd](https://github.com/athensresearch/athens/commit/d94e5fdcbfacbfb9327faec0a996cbe971d3544e))
* Checking a TODO item in block embed results to error ([#939](https://github.com/athensresearch/athens/issues/939)) ([dc393d4](https://github.com/athensresearch/athens/commit/dc393d45bb855e1e48c2bf34c8e0122f43dbf9ce))
* fix-border-flash-on-new-block ([(#944)](https://github.com/athensresearch/athens/commit/67ab47fc161e9918d873aa370430e0e088c19b41))


### Documentation

* remove all docs ([#919](https://github.com/athensresearch/athens/issues/919)) ([b5f2887](https://github.com/athensresearch/athens/commit/b5f2887a3434c4a63a924cb6157a393939356752))

## [1.0.0-beta.66](https://github.com/athensresearch/athens/compare/v1.0.0-beta.65...v1.0.0-beta.66) (2021-04-07)


### Features

* import from Roam ([#918](https://github.com/athensresearch/athens/issues/918)) ([1a3ee32](https://github.com/athensresearch/athens/commit/1a3ee3205a9ddfed54c3f40705d9ec2daca22e5d))


### Documentation

* describe collapse/expand shortcut in Welcome ([#913](https://github.com/athensresearch/athens/issues/913)) ([0bc7bc3](https://github.com/athensresearch/athens/commit/0bc7bc30b3ed342b98b8724f6f77d1ba2c7402ec))

## [1.0.0-beta.65](https://github.com/athensresearch/athens/compare/v1.0.0-beta.64...v1.0.0-beta.65) (2021-04-06)


### Bug Fixes

* **10x:** ctrl/cmd-t always works to toggle 10x ([#907](https://github.com/athensresearch/athens/issues/907)) ([460fdbd](https://github.com/athensresearch/athens/commit/460fdbd017aafe04d82259e2f1edc455259a1d00))


* **db:** add Karma test for node search by title ([#903](https://github.com/athensresearch/athens/issues/903)) ([2876194](https://github.com/athensresearch/athens/commit/2876194a0c68da33f8dd0eadd3a5534f2b811148))
* **settings:** allow any user to opt-out; improve copy ([#908](https://github.com/athensresearch/athens/issues/908)) ([98a1fca](https://github.com/athensresearch/athens/commit/98a1fca13aed2bead6dd3ec9ee41d7c0078e0a53))

## [1.0.0-beta.64](https://github.com/athensresearch/athens/compare/v1.0.0-beta.63...v1.0.0-beta.64) (2021-04-02)


### Features

* **copy+paste:** respect h1/h2/h3 markdown ([#901](https://github.com/athensresearch/athens/issues/901)) ([a2bcef5](https://github.com/athensresearch/athens/commit/a2bcef5c0862dcc88af5ff73ad4b0f044ad2c1d6))

## [1.0.0-beta.63](https://github.com/athensresearch/athens/compare/v1.0.0-beta.62...v1.0.0-beta.63) (2021-04-01)


### Features

* **copy:** right-click to copy without formatting ([#897](https://github.com/athensresearch/athens/issues/897)) ([d994f66](https://github.com/athensresearch/athens/commit/d994f664032a0ac5e1643f1974a6504a8e2f9211))


* increase network request timeout ([#899](https://github.com/athensresearch/athens/issues/899)) ([a224c23](https://github.com/athensresearch/athens/commit/a224c23791f8d5b5f03507e8fac5917a34608130))

## [1.0.0-beta.62](https://github.com/athensresearch/athens/compare/v1.0.0-beta.61...v1.0.0-beta.62) (2021-04-01)


### Features

* **block-ref:** shift + "Drag'n'Drop" to copy block uid ([#876](https://github.com/athensresearch/athens/issues/876)) ([613b244](https://github.com/athensresearch/athens/commit/613b244a1ecc696d5fbd6e885f7e47f0c4e69ff4))


### Bug Fixes

* url to changelog ([#896](https://github.com/athensresearch/athens/issues/896)) ([241f1fd](https://github.com/athensresearch/athens/commit/241f1fdfa2d56726d3d87eb95193ffc9e9858858))
* **athena:** make sure element exists before scroll ([#877](https://github.com/athensresearch/athens/issues/877)) ([32ae526](https://github.com/athensresearch/athens/commit/32ae52623841ebd499f4106167c24535005e5f4a))
* catch all KaTeX errors ([#871](https://github.com/athensresearch/athens/issues/871)) ([e37a50c](https://github.com/athensresearch/athens/commit/e37a50cc436e108d9317160b835cb9a02d95844b))
* **parser:** parse some edge case URLs ([#838](https://github.com/athensresearch/athens/issues/838)) ([1d20057](https://github.com/athensresearch/athens/commit/1d200577f61702dc31a85f582be28899beaf7da2))

## [1.0.0-beta.61](https://github.com/athensresearch/athens/compare/v1.0.0-beta.60...v1.0.0-beta.61) (2021-03-25)


### Bug Fixes

* **parser:** parse multiple raw URLs in a single block ([#830](https://github.com/athensresearch/athens/issues/830)) ([92b766a](https://github.com/athensresearch/athens/commit/92b766a8628b95b0c136677b74d2df9fdcfaa042))
* toolbar overlaps on scrollbar ([#818](https://github.com/athensresearch/athens/issues/818)) ([8aa6790](https://github.com/athensresearch/athens/commit/8aa6790876cd31a85f4e7e02352d028620cba932))


### Documentation

* update download links ([#804](https://github.com/athensresearch/athens/issues/804)) ([1c4aa1a](https://github.com/athensresearch/athens/commit/1c4aa1ada5e325388bc874a7aeb023760ccad64a))

## [1.0.0-beta.60](https://github.com/athensresearch/athens/compare/v1.0.0-beta.59...v1.0.0-beta.60) (2021-03-13)


### Bug Fixes

* clicking the date doesn't open the date page ([#807](https://github.com/athensresearch/athens/issues/807)) ([9fc1a72](https://github.com/athensresearch/athens/commit/9fc1a72d7205b7bba4d4c95fb0887a8c36bf95ce)), closes [#802](https://github.com/athensresearch/athens/issues/802)
* toolbar overlaps on scrollbar ([#809](https://github.com/athensresearch/athens/issues/809)) ([2657478](https://github.com/athensresearch/athens/commit/2657478db5ef6d77b20085b6a8093c708c974033))


### Documentation

* add gitbook config ([7b2c8d7](https://github.com/athensresearch/athens/commit/7b2c8d74122243083fd837ecf344e6f5b45a6090))
* add more contributor faces ([9b7fdad](https://github.com/athensresearch/athens/commit/9b7fdada18f84e965fecdc1b7713bf78b519dbdc))


### Performance

* **undo/redo:** undo by transaction rather than entire db ([#808](https://github.com/athensresearch/athens/issues/808)) ([167804d](https://github.com/athensresearch/athens/commit/167804d07226c2909cc4d5cb8b383afce777bdf8))

## [1.0.0-beta.59](https://github.com/athensresearch/athens/compare/v1.0.0-beta.58...v1.0.0-beta.59) (2021-03-11)


### Bug Fixes

* **undo/redo:** textarea undo/redo; italics, highlight, underline, strikethrough [#717](https://github.com/athensresearch/athens/issues/717) ([#803](https://github.com/athensresearch/athens/issues/803)) ([2509b9a](https://github.com/athensresearch/athens/commit/2509b9a3cb63bea66f6b44cedf398d7b93dfe825))

## [1.0.0-beta.58](https://github.com/athensresearch/athens/compare/v1.0.0-beta.57...v1.0.0-beta.58) (2021-03-11)


### Bug Fixes

* **title:** shift-click on title should open in right sidebar ([#802](https://github.com/athensresearch/athens/issues/802)) ([a51e001](https://github.com/athensresearch/athens/commit/a51e001cd4543b389460b1def17446230f51062c)), closes [#775](https://github.com/athensresearch/athens/issues/775)
* Persistent Pop-up when scrolling ([#801](https://github.com/athensresearch/athens/issues/801)) ([ce4fe61](https://github.com/athensresearch/athens/commit/ce4fe61e1e73733e22367f1160456ce0829a9e8f))

## [1.0.0-beta.57](https://github.com/athensresearch/athens/compare/v1.0.0-beta.56...v1.0.0-beta.57) (2021-03-11)


### Bug Fixes

* **blocks:** links made clickable, z-index clean up [#772](https://github.com/athensresearch/athens/issues/772) ([#778](https://github.com/athensresearch/athens/issues/778)) ([8cabc5f](https://github.com/athensresearch/athens/commit/8cabc5fa38cdcf6624cb84b5826fbb17de628fea))
* **icons:** fix [#792](https://github.com/athensresearch/athens/issues/792) ([#795](https://github.com/athensresearch/athens/issues/795)) ([3c7368d](https://github.com/athensresearch/athens/commit/3c7368d4e18b21b24ab5fbfda29aa638934d4525))


* S3 -> GitHub release ([#799](https://github.com/athensresearch/athens/issues/799)) ([19ceb80](https://github.com/athensresearch/athens/commit/19ceb80cc713112573098cbaf10ae7dddcea5391))

## [1.0.0-beta.56](https://github.com/athensresearch/athens/compare/v1.0.0-beta.55...v1.0.0-beta.56) (2021-03-10)


### Bug Fixes

* **electron:** issue updating Mac (Intel, not Silicon) ([#796](https://github.com/athensresearch/athens/issues/796)) ([2037a83](https://github.com/athensresearch/athens/commit/2037a83ebf60ca53a1b96be653cd55e82c36d557)), closes [#749](https://github.com/athensresearch/athens/issues/749) [#793](https://github.com/athensresearch/athens/issues/793)


### Documentation

* update version number ([60390da](https://github.com/athensresearch/athens/commit/60390daea2ddba084c00ad78d65e6080c062d7ef))

## [1.0.0-beta.55](https://github.com/athensresearch/athens/compare/v1.0.0-beta.54...v1.0.0-beta.55) (2021-03-10)


### Documentation

* add directions on how to download a specific version of Athens ([6d6e7ac](https://github.com/athensresearch/athens/commit/6d6e7ac245837fef48c5578fdf382f1fd462cad6))

## [1.0.0-beta.54](https://github.com/athensresearch/athens/compare/v1.0.0-beta.53...v1.0.0-beta.54) (2021-03-10)


### Enhancements

* **linked-refs:** sort references by newest first ([#776](https://github.com/athensresearch/athens/issues/776)) ([954da17](https://github.com/athensresearch/athens/commit/954da17da57524fe55a4b25ea3c518a012f803c7))


### Performance

* **icons:** optimize size of material-ui/icons ([#790](https://github.com/athensresearch/athens/issues/790)) ([e60d92f](https://github.com/athensresearch/athens/commit/e60d92fb9d1235d205f868d91fbce6935617d254))

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
