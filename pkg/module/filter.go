package module

import (
	"bufio"
	"os"
	"strings"

	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/errors"
)

var (
	pathSeparator = "/"
)

// Filter is a filter of modules
type Filter struct {
	root ruleNode
}

// NewFilter creates new filter based on rules defined in a configuration file
// WARNING: this is not concurrently safe
// Configuration consists of two operations: + for include and - for exclude
// e.g.
//    - github.com/a
//    + github.com/a/b
// will communicate all modules except github.com/a and its children, but github.com/a/b will be communicated
// example 2:
//   -
//   + github.com/a
// will exclude all items from communication except github.com/a
func NewFilter() *Filter {
	rn := newRule(Default)
	modFilter := Filter{}
	modFilter.root = rn

	modFilter.initFromConfig()

	return &modFilter
}

// AddRule adds rule for specified path
func (f *Filter) AddRule(path string, rule FilterRule) {
	f.ensurePath(path)

	segments := getPathSegments(path)

	if len(segments) == 0 {
		f.root.rule = rule
		return
	}

	// look for latest node in a path
	latest := f.root
	for _, p := range segments[:len(segments)-1] {
		latest = latest.next[p]
	}

	// replace with updated node
	last := segments[len(segments)-1]
	rn := latest.next[last]
	rn.rule = rule
	latest.next[last] = rn
}

// Rule returns the filter rule to be applied to the given path
func (f *Filter) Rule(path string) FilterRule {
	segs := getPathSegments(path)
	rule := f.getAssociatedRule(segs...)
	if rule == Default {
		rule = Include
	}

	return rule
}

func (f *Filter) ensurePath(path string) {
	latest := f.root.next
	pathSegments := getPathSegments(path)

	for _, p := range pathSegments {
		if _, ok := latest[p]; !ok {
			latest[p] = newRule(Default)
		}
		latest = latest[p].next
	}
}

func (f *Filter) getAssociatedRule(path ...string) FilterRule {
	if len(path) == 0 {
		return f.root.rule
	}

	rules := make([]FilterRule, 0, len(path))
	rn := f.root
	for _, p := range path {
		if _, ok := rn.next[p]; !ok {
			break
		}
		rn = rn.next[p]
		rules = append(rules, rn.rule)
	}

	if len(rules) == 0 {
		return f.root.rule
	}

	for i := len(rules) - 1; i >= 0; i-- {
		if rules[i] != Default {
			return rules[i]
		}
	}

	return f.root.rule
}

func (f *Filter) initFromConfig() {
	lines, err := getConfigLines()

	if err != nil || len(lines) == 0 {
		return
	}

	for _, line := range lines {
		split := strings.Split(strings.TrimSpace(line), " ")
		if len(split) > 2 {
			continue
		}

		ruleSign := strings.TrimSpace(split[0])
		rule := Default
		switch ruleSign {
		case "+":
			rule = Include
		case "-":
			rule = Exclude
		case "D":
			rule = Direct
		default:
			continue
		}

		// is root config
		if len(split) == 1 {
			f.AddRule("", rule)
			continue
		}

		path := strings.TrimSpace(split[1])
		f.AddRule(path, rule)
	}
}

func getPathSegments(path string) []string {
	path = strings.TrimSpace(path)
	path = strings.Trim(path, pathSeparator)

	if path == "" {
		return []string{}
	}

	return strings.Split(path, pathSeparator)
}

func newRule(r FilterRule) ruleNode {
	rn := ruleNode{}
	rn.next = make(map[string]ruleNode)
	rn.rule = r

	return rn
}

func getConfigLines() ([]string, error) {
	const op errors.Op = "module.getConfigLines"
	configName := env.FilterConfigurationFileName()

	f, err := os.Open(configName)
	if err != nil {
		return nil, errors.E(op, err)
	}
	defer f.Close()

	scanner := bufio.NewScanner(f)

	var lines []string
	for scanner.Scan() {
		line := scanner.Text()
		line = strings.TrimSpace(line)
		if len(line) == 0 {
			continue
		}

		lines = append(lines, line)
	}

	return lines, nil
}
