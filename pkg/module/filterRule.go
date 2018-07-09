package module

// FilterRule defines behavior of module communication
type FilterRule int

const (
	// Default filter rule does not alter default behavior
	Default FilterRule = iota
	// Include filter rule includes package and its children from communication
	Include
	// Exclude filter rule excludes package and its children from communication
	Exclude
)
