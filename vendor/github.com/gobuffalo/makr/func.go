package makr

// RunFn interface for a function that can be used as a runner
type RunFn func(string, Data) error

// Func to be run as a generator
type Func struct {
	Should ShouldFunc
	Runner RunFn
}

// Run the generator
func (f Func) Run(rootPath string, data Data) error {
	return chdir(rootPath, func() error {
		if f.Should != nil && !f.Should(data) {
			return nil
		}
		return f.Runner(rootPath, data)
	})
}
