package actions

import (
	cdnmetadata "github.com/gomods/athens/pkg/cdn/metadata"
	"github.com/gomods/athens/pkg/cdn/metadata/fake"
)

func newCDNGetter() cdnmetadata.Getter {
	return &fake.Getter{URL: "https://mycdn.com"}
}
