package pop

import (
	"fmt"
	"log"
	"os"

	"github.com/fatih/color"
)

// AvailableDialects lists the available database dialects
var AvailableDialects = []string{"postgres", "mysql", "cockroach"}

// Debug mode, to toggle verbose log traces
var Debug = false

// Color mode, to toggle colored logs
var Color = true
var logger = log.New(os.Stdout, "[POP] ", log.LstdFlags)

// Log a formatted string to the logger
var Log = func(s string, args ...interface{}) {
	if Debug {
		if len(args) > 0 {
			xargs := make([]string, len(args))
			for i, a := range args {
				switch a.(type) {
				case string:
					xargs[i] = fmt.Sprintf("%q", a)
				default:
					xargs[i] = fmt.Sprintf("%v", a)
				}
			}
			s = fmt.Sprintf("%s | %s", s, xargs)
		}
		if Color {
			s = color.YellowString(s)
		}
		logger.Println(s)
	}
}

// DialectSupported checks support for the given database dialect
func DialectSupported(d string) bool {
	for _, ad := range AvailableDialects {
		if ad == d {
			return true
		}
	}
	return false
}
