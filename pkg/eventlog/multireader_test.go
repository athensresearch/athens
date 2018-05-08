package eventlog

import (
	"testing"

	"github.com/globalsign/mgo/bson"
	"github.com/stretchr/testify/suite"
)

type MultiReaderTests struct {
	suite.Suite
}

func (m *MultiReaderTests) TestDedupRead() {
	inMemReader1 := &InMemoryReader{[]Event{
		{ID: bson.NewObjectId().Hex(), Module: "c", Version: "v1"},
		{ID: bson.NewObjectId().Hex(), Module: "d", Version: "v1"},
		{ID: bson.NewObjectId().Hex(), Module: "e", Version: "v1"},
	}}
	inMemReader2 := &InMemoryReader{[]Event{
		{ID: bson.NewObjectId().Hex(), Module: "a", Version: "v1"},
		{ID: bson.NewObjectId().Hex(), Module: "d", Version: "v1"},
		{ID: bson.NewObjectId().Hex(), Module: "f", Version: "v1"},
	}}
	inMemReader3 := &InMemoryReader{[]Event{
		{ID: bson.NewObjectId().Hex(), Module: "b", Version: "v1"},
		{ID: bson.NewObjectId().Hex(), Module: "e", Version: "v1"},
		{ID: bson.NewObjectId().Hex(), Module: "c", Version: "v2"},
	}}

	storageChecker := ModuleStorageChecker{Module: "f"}

	mr := NewMultiReader(storageChecker, inMemReader1, inMemReader2, inMemReader3)

	r := m.Require()

	result, err := mr.Read()

	r.Equal(nil, err)
	r.Equal(6, len(result), "Retrieved result %v", result)

	r.Equal("c", result[0].Module)
	r.Equal("v1", result[0].Version)

	r.Equal("d", result[1].Module)
	r.Equal("v1", result[1].Version)

	r.Equal("e", result[2].Module)
	r.Equal("v1", result[2].Version)

	r.Equal("a", result[3].Module)
	r.Equal("v1", result[3].Version)

	r.Equal("b", result[4].Module)
	r.Equal("v1", result[4].Version)

	r.Equal("c", result[5].Module)
	r.Equal("v2", result[5].Version)
}

func (m *MultiReaderTests) TestDedupReadFrom() {
	pointer1 := bson.NewObjectId().Hex()
	inMemReader1 := &InMemoryReader{[]Event{
		{ID: pointer1, Module: "c", Version: "v1"},
		{ID: bson.NewObjectId().Hex(), Module: "d", Version: "v1"},
		{ID: bson.NewObjectId().Hex(), Module: "e", Version: "v1"},
	}}
	pointer2 := bson.NewObjectId().Hex()
	inMemReader2 := &InMemoryReader{[]Event{
		{ID: bson.NewObjectId().Hex(), Module: "a", Version: "v1"},
		{ID: pointer2, Module: "d", Version: "v1"},
		{ID: bson.NewObjectId().Hex(), Module: "f", Version: "v1"},
	}}
	pointer3 := bson.NewObjectId().Hex()
	inMemReader3 := &InMemoryReader{[]Event{
		{ID: bson.NewObjectId().Hex(), Module: "b", Version: "v1"},
		{ID: pointer3, Module: "e", Version: "v1"},
		{ID: bson.NewObjectId().Hex(), Module: "c", Version: "v2"},
	}}

	storageChecker := ModuleStorageChecker{Module: "f"}

	sequencedLog1 := SequencedLog{Index: pointer1, Log: inMemReader1}
	sequencedLog2 := SequencedLog{Index: pointer2, Log: inMemReader2}
	sequencedLog3 := SequencedLog{Index: pointer3, Log: inMemReader3}

	mr := NewMultiReaderFrom(storageChecker, sequencedLog1, sequencedLog2, sequencedLog3)

	r := m.Require()

	result, err := mr.ReadFrom("")

	r.Equal(nil, err)
	r.Equal(3, len(result), "Retrieved result %v", result)

	r.Equal("d", result[0].Module)
	r.Equal("v1", result[0].Version)

	r.Equal("e", result[1].Module)
	r.Equal("v1", result[1].Version)

	r.Equal("c", result[2].Module)
	r.Equal("v2", result[2].Version)
}

func TestDiskStorage(t *testing.T) {
	suite.Run(t, new(MultiReaderTests))
}

type InMemoryReader struct {
	mem []Event
}

// Read reads all events in event log.
func (m *InMemoryReader) Read() ([]Event, error) {
	return m.mem, nil
}

// ReadFrom reads all events from the log starting at event with specified id (excluded).
// If id is not found behaves like Read().
func (m *InMemoryReader) ReadFrom(id string) ([]Event, error) {
	var index int

	for i, e := range m.mem {
		if e.ID == id {
			index = i
			break
		}
	}

	return m.mem[index+1:], nil
}

// Append appends Event to event log and returns its ID.
func (m *InMemoryReader) Append(event Event) (string, error) {
	event.ID = bson.NewObjectId().Hex()
	m.mem = append(m.mem, event)

	return event.ID, nil

}

func (m *InMemoryReader) Clear(id string) error {
	m.mem = make([]Event, 0)
	return nil
}

type ModuleStorageChecker struct {
	Module string
}

func (s ModuleStorageChecker) Exists(module, version string) bool {
	return module == s.Module
}
