package storage

// Module represents a vgo module saved in a storage backend.
type Module struct {
	Module  string `bson:"module"`
	Version string `bson:"version"`
	Mod     []byte `bson:"mod"`
	Zip     []byte `bson:"zip"`
}
