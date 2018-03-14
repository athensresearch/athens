package memory

import (
	"io/ioutil"
	"time"
)

func (m *MemoryTests) TestSave() {
	r := m.Require()
	vsn := "v1.0.0"
	r.NoError(m.mem.Save(baseURL, module, vsn, mod, zip))

	// make sure List returns the right version list
	listed, err := m.mem.List(baseURL, module)
	r.NoError(err)
	r.Len(listed, 1)
	r.Equal(vsn, listed[0])

	// make sure get returns the right version
	version, err := m.mem.Get(baseURL, module, vsn)
	r.NoError(err)
	r.Equal(vsn, version.RevInfo.Version)
	r.Equal(vsn, version.RevInfo.Name)
	r.Equal(vsn, version.RevInfo.Short)
	// current time should be after vsn.RevInfo.Time
	time.Sleep(time.Millisecond * 3)
	r.True(time.Now().Sub(version.RevInfo.Time) > 0)
	r.Equal(mod, version.Mod)
	zipContent, err := ioutil.ReadAll(version.Zip)
	r.NoError(err)
	r.Equal(zip, zipContent)
}
