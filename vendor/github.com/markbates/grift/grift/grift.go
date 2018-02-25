package grift

import (
	"fmt"
	"io"
	"log"
	"os"
	"os/exec"
	"sort"
	"strings"
	"sync"
	"time"
)

var CommandName = "grift"
var griftList = map[string]Grift{}
var descriptions = map[string]string{}
var lock = &sync.Mutex{}
var namespace string

type Grift func(c *Context) error

// Namespace will place all tasks within the given prefix.
func Namespace(name string, s func()) error {
	defer func() {
		namespace = ""
	}()

	namespace = applyNamespace(name)
	s()
	return nil
}

func applyNamespace(name string) string {
	if namespace != "" {
		if strings.HasPrefix(name, ":") {
			return name[1:]
		}
		if name == "default" {
			return name
		}
		return fmt.Sprintf("%s:%s", namespace, name)
	}

	return name
}

// Add a grift. If there is already a grift
// with the given name the two grifts will
// be bundled together.
func Add(name string, grift Grift) error {
	lock.Lock()
	defer lock.Unlock()

	name = applyNamespace(name)

	if griftList[name] != nil {
		fn := griftList[name]
		griftList[name] = func(c *Context) error {
			err := fn(c)
			if err != nil {
				return err
			}
			return grift(c)
		}
	} else {
		griftList[name] = grift
	}
	return nil
}

// Set a grift. This is similar to `Add` but it will
// overwrite an existing grift with the same name.
func Set(name string, grift Grift) error {
	lock.Lock()
	defer lock.Unlock()
	name = applyNamespace(name)
	griftList[name] = grift
	return nil
}

// Rename a grift. Useful if you want to re-define
// an existing grift, but don't want to write over
// the original.
func Rename(oldName string, newName string) error {
	lock.Lock()
	defer lock.Unlock()

	oldName = applyNamespace(oldName)
	newName = applyNamespace(newName)

	if griftList[oldName] == nil {
		return fmt.Errorf("No task named %s defined!", oldName)
	}
	griftList[newName] = griftList[oldName]
	delete(griftList, oldName)
	return nil
}

// Remove a grift. Not incredibly useful, but here for
// completeness.
func Remove(name string) error {
	lock.Lock()
	defer lock.Unlock()

	name = applyNamespace(name)

	delete(griftList, name)
	delete(descriptions, name)
	return nil
}

// Desc sets a helpful descriptive text for a grift.
// This description will be shown when `grift list`
// is run.
func Desc(name string, description string) error {
	lock.Lock()
	defer lock.Unlock()

	name = applyNamespace(name)

	descriptions[name] = description
	return nil
}

// Run a grift. This allows for the chaining for grifts.
// One grift can Run another grift and so on.
func Run(name string, c *Context) error {
	name = applyNamespace(name)

	if griftList[name] == nil {
		if name == "list" {
			PrintGrifts(os.Stdout)
			return nil
		}
		return fmt.Errorf("No task named '%s' defined!", name)
	}
	if c.Verbose {
		defer func(start time.Time) {
			log.Printf("Completed task %s in %s\n", name, time.Now().Sub(start))
		}(time.Now())
		log.Printf("Starting task %s\n", name)
	}
	return griftList[name](c)
}

// List of the names of the defined grifts.
func List() []string {
	keys := []string{}
	for k := range griftList {
		keys = append(keys, k)
	}
	sort.Strings(keys)
	return keys
}

// Exec the grift stack. This is the main "entry point" to
// the grift system.
func Exec(args []string, verbose bool) error {
	name := "list"
	if len(args) >= 1 {
		name = args[0]
	}
	switch name {
	case "list":
		PrintGrifts(os.Stdout)
	default:
		c := NewContext(name)
		c.Verbose = verbose
		if len(args) >= 1 {
			c.Args = args[1:]
		}
		return Run(name, c)
	}
	return nil
}

// PrintGrifts to the screen, nice, sorted, and with descriptions,
// should they exist.
func PrintGrifts(w io.Writer) {
	fmt.Fprint(w, "Available grifts\n================\n")

	cnLen := len(CommandName)
	maxLen := cnLen
	l := List()

	for _, k := range l {
		if (len(k) + cnLen) > maxLen {
			maxLen = len(k) + cnLen
		}
	}

	for _, k := range l {
		m := strings.Join([]string{CommandName, k}, " ")
		suffix := strings.Repeat(" ", (maxLen+3)-len(m)) + " #"

		fmt.Fprintln(w, strings.Join([]string{m, suffix, descriptions[k]}, " "))
	}
}

// RunSource executes the command passed as argument,
// in the current shell/context
func RunSource(cmd *exec.Cmd) error {
	cmd.Stdin = os.Stdin
	cmd.Stderr = os.Stderr
	cmd.Stdout = os.Stdout
	return cmd.Run()
}
