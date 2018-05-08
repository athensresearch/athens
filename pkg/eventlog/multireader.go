package eventlog

import "github.com/gomods/athens/pkg/storage"

type multiReader struct {
	logs    []SequencedLog
	checker storage.Checker
}

// SequencedLog is collection of event logs with specified starting pointers used by ReadFrom function.
type SequencedLog struct {
	Log   Eventlog
	Index string
}

// NewMultiReader creates composite reader of specified readers.
// Order of readers matters in a way how Events are deduplicated.
// Initial state:
// - InMemory [A, B] - as im.A, im.B
// R1: [C,D,E] - as r1.C...
// R2: [A,D,F]
// R3: [B, G]
// result [r1.C, r1.D, r1.E, r2.F, r3.G]
// r2.A, r2.D, r3.B - skipped due to deduplication checks
func NewMultiReader(ch storage.Checker, ll ...Eventlog) Reader {
	logs := make([]SequencedLog, 0, len(ll))
	for _, l := range ll {
		// init to -1, not 0, 0 might mean first item and as this is excluding pointer we might lose it
		logs = append(logs, SequencedLog{Log: l})
	}

	return NewMultiReaderFrom(ch, logs...)
}

// NewMultiReaderFrom creates composite reader of specified readers.
// Order of readers matters in a way how Events are deduplicated.
// Initial state:
// - InMemory [A, B] - as im.A, im.B
// R1: [B,C,E] - as r1.C... - pointer to D
// R2: [A,D,F] - pointer to A
// R3: [B, G] - pointer to B
// result [r1.E, r2.D, r2.F, r3.G]
func NewMultiReaderFrom(ch storage.Checker, l ...SequencedLog) Reader {
	return &multiReader{
		logs:    l,
		checker: ch,
	}
}

func (mr *multiReader) Read() ([]Event, error) {
	events := make([]Event, 0)

	for _, r := range mr.logs {
		ee, err := r.Log.Read()
		if err != nil {
			return nil, err
		}

		for _, e := range ee {
			if exists(e, events, mr.checker) {
				continue
			}
			events = append(events, e)
		}
	}

	return events, nil
}

func (mr *multiReader) ReadFrom(index string) ([]Event, error) {
	events := make([]Event, 0)

	for _, r := range mr.logs {
		var ee []Event
		var err error

		if r.Index == "" {
			ee, err = r.Log.Read()
		} else {
			ee, err = r.Log.ReadFrom(r.Index)
		}

		if err != nil {
			return nil, err
		}

		for _, e := range ee {
			if exists(e, events, mr.checker) {
				continue
			}
			events = append(events, e)
		}
	}

	return events, nil
}

func exists(event Event, log []Event, checker storage.Checker) bool {
	for _, e := range log {
		if e.Module == event.Module && e.Version == event.Version {
			return true
		}
	}

	return checker.Exists(event.Module, event.Version)
}
