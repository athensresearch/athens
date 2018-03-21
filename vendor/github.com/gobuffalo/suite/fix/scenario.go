package fix

type Scenario struct {
	Name   string `toml:"name"`
	Tables Tables `toml:"table"`
}

// type Scenarios []Scenario
type Scenarios struct {
	Scenarios []Scenario `toml:"scenario"`
}

type Table struct {
	Name string                   `toml:"name"`
	Row  []map[string]interface{} `toml:"row"`
}

type Tables []Table
