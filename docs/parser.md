# Athens Block Parser Documentation

The EBNF syntax of instaparse (the parsing library Athens uses) meant that if we need to increase the performance of the Athens block parser, the readability and extensibility must be reduced in favor of a less recursive parsing process.

Therefore, this document is created in order to provide a handy reference for future parser updates and extensions.

## Reserved Characters

We try to imitate a state machine in the parsing process, so we check for every reserved charater and use them to stop the current "any-chars" evaluation. For a list of reserved characters, please see the `parser.cljc` file within `src/cljc/athens`.

## See Also

* <https://github.com/athensresearch/athens/issues/240>
* <https://github.com/Engelberg/instaparse/issues/131>
* <https://discordapp.com/channels/708122962422792194/714190075843313754/733205043033145384>
