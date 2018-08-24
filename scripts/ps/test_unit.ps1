# Run the unit tests with the race detector and code coverage enabled

$envPath = Join-Path "cmd" "proxy" | Join-Path -ChildPath ".env"
Get-Content $envPath | ForEach-Object {
    $line = $_.ToString().Split("=")
    if ($line.Length -ne 2) {
        return
    }
    $name = $line[0]
    $val = $line[1]

    [System.Environment]::SetEnvironmentVariable($name, $val)
}

& go test -race -coverprofile cover.out -covermode atomic ./...
