package storage

type Version struct {
	RevInfo RevInfo
	Mod     []byte
	Zip     []byte
}
