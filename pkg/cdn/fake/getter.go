package fake

// Getter is a (./pkg/cdn).Getter implementation that always returns URL
type Getter struct {
	URL string
}

// Get is the cdn.Getter implementation that always returns g.URL, nil
func (g *Getter) Get(baseURL, module string) (string, error) {
	return g.URL, nil
}
