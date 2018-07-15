package makr

import (
	"fmt"
	"os"
	"os/exec"
	"strings"
)

// Command wraps an exec.Cmd to be run
type Command struct {
	Should  ShouldFunc
	Command *exec.Cmd
}

// Run the generator
func (c Command) Run(rootPath string, data Data) error {
	return chdir(rootPath, func() error {
		if c.Should != nil && !c.Should(data) {
			return nil
		}

		cmd := c.Command
		fmt.Printf("--> %s\n", strings.Join(cmd.Args, " "))
		cmd.Stdin = os.Stdin
		cmd.Stderr = os.Stderr
		cmd.Stdout = os.Stdout
		return cmd.Run()
	})
}

// NewCommand ready to run
func NewCommand(cmd *exec.Cmd) Command {
	return Command{
		Command: cmd,
		Should:  nullShould,
	}
}
