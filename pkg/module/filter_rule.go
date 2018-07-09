package module

type ruleNode struct {
	next map[string]ruleNode
	rule FilterRule
}
