package errors

// IsNotFoundErr helper function for KindNotFound
func IsNotFoundErr(err error) bool {
	return Kind(err) == KindNotFound
}
