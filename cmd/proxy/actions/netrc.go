package actions

import (
	"io/ioutil"
	"log"
	"path/filepath"

	"github.com/gomods/athens/pkg/config/env"
	"github.com/mitchellh/go-homedir"
)

// initializeNETRC checks if .netrc is at a pre-configured path
// and moves to ~/.netrc -- note that this will override whatever
// .netrc you have in your home directory.
func initializeNETRC() {
	p := env.NETRCPath()
	if p == "" {
		return
	}

	netrcBts, err := ioutil.ReadFile(p)
	if err != nil {
		log.Fatalf("could not read %s: %v", p, err)
	}

	hdir, err := homedir.Dir()
	if err != nil {
		log.Fatalf("could not get homedir: %v", err)
	}
	rcp := filepath.Join(hdir, ".netrc")
	ioutil.WriteFile(rcp, netrcBts, 0666)
}
