package errors

// ErrNotFound helper function for KindNotFound
func ErrNotFound(err error) bool {
	return Kind(err) == KindNotFound
}
