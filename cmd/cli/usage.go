package main

const usage = `athens <dir> --base-url foo.com --module bar --version v1.0.0

Details:

- The directory from which code will be uploaded is <dir>
- ... and that directory must have a go.mod file in it
- ... and the go.mod file's 'module' directive must match 'bar'
- ... and if there's a vendor directory under that directory, it won't be ignored right now
- ... and the go.mod file will be uploaded with the source`
