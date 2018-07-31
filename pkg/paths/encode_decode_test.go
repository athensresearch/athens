package paths

import "testing"

var testCases = []struct {
	name, decoded, encoded string
}{
	{
		"happy path",
		"github.com/a/b",
		"github.com/a/b",
	},
	{
		"capital letters",
		"github.com/NYTimes/gizmo",
		"github.com/!n!y!times/gizmo",
	},
}

func TestEncodeDecode(t *testing.T) {
	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			testEncodeDecode(t, tc.decoded, tc.encoded)
		})
	}
}

func testEncodeDecode(t *testing.T, exDec, exEnc string) {
	t.Helper()
	enc, err := EncodePath(exDec)
	if err != nil {
		t.Fatal(err)
	}
	dec, err := DecodePath(exEnc)
	if err != nil {
		t.Fatal(err)
	}

	if exEnc != enc {
		t.Fatalf("expected %v and %v to be equal", exEnc, enc)
	}

	if exDec != dec {
		t.Fatalf("expected %v and %v to be equal", exDec, dec)
	}
}
