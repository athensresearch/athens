package actions

import (
	"github.com/gomods/athens/pkg/cdn"
	"github.com/gomods/athens/pkg/cdn/fake"
)

func newCDNGetter() cdn.Getter {
	return &fake.Getter{URL: "https://mycdn.com"}
}
