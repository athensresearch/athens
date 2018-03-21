package storage

type Module struct {
	Module  string `bson:"module"`
	Version string `bson:"version"`
	Mod     []byte `bson:"mod"`
	Zip     []byte `bson:"zip"`
}
