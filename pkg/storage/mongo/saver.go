package mongo

import "github.com/gomods/athens/pkg/storage"

func (s *MongoModuleStore) Save(baseURL, module, version string, mod, zip []byte) error {
	m := &storage.Module{
		BaseURL: baseURL,
		Module:  module,
		Version: version,
		Mod:     mod,
		Zip:     zip,
	}

	c := s.s.DB(s.d).C(s.c)
	return c.Insert(m)
}
