package refresh

import (
	"fmt"
	"log"
	"os"
	"path"
	"runtime"

	"github.com/fatih/color"
	"github.com/markbates/going/defaults"
	homedir "github.com/mitchellh/go-homedir"
)

const lformat = "=== %s ==="

type Logger struct {
	log *log.Logger
}

func NewLogger(c *Configuration) *Logger {
	color.NoColor = !c.EnableColors
	if runtime.GOOS == "windows" {
		color.NoColor = true
	}
	lname := defaults.String(c.LogName, "refresh")
	return &Logger{
		log: log.New(os.Stdout, fmt.Sprintf("%s: ", lname), log.LstdFlags),
	}
}

func (l *Logger) Success(msg interface{}, args ...interface{}) {
	l.log.Print(color.GreenString(fmt.Sprintf(lformat, msg), args...))
}

func (l *Logger) Error(msg interface{}, args ...interface{}) {
	l.log.Print(color.RedString(fmt.Sprintf(lformat, msg), args...))
}

func (l *Logger) Print(msg interface{}, args ...interface{}) {
	l.log.Printf(fmt.Sprintf(lformat, msg), args...)
}

var LogLocation = func() string {
	dir, _ := homedir.Dir()
	dir, _ = homedir.Expand(dir)
	dir = path.Join(dir, ".refresh")
	os.MkdirAll(dir, 0755)
	return dir
}

var ErrorLogPath = func() string {
	return path.Join(LogLocation(), ID()+".err")
}
