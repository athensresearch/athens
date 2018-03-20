package storage

type Module struct {
	BaseURL string `bson:"base_url"`
	Module  string `bson:"module"`
	Version string `bson:"version"`
	Mod     []byte `bson:"mod"`
	Zip     []byte `bson:"zip"`
}
