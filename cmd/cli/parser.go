package main

import (
	"strings"

	ignore "github.com/sabhiram/go-gitignore"
)

type multiIgnoreParser struct {
	parsers []ignore.IgnoreParser
}

// Returns map of patterns for more performant searches
func newMultiIgnoreParser(parsers ...ignore.IgnoreParser) ignore.IgnoreParser {
	parser := multiIgnoreParser{}

	for _, p := range parsers {
		if p != nil {
			parser.parsers = append(parser.parsers, p)
		}
	}

	return parser
}

func (p multiIgnoreParser) MatchesPath(f string) bool {
	for _, parser := range p.parsers {
		if parser.MatchesPath(f) {
			return true
		}
	}

	return false
}

type dsStoreIgnoreParser struct{}

func (p dsStoreIgnoreParser) MatchesPath(f string) bool {
	return strings.Contains(f, "/.DS_Store")
}
