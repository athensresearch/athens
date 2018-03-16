package cdn

// CDN represents access to a CDN for registry use
type CDN interface {
	Saver
	Getter
}
