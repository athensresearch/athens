# check_golint.ps1
# Run the linter on everything

$out = & golint -set_exit_status $(& go list ./...)
if ($LastExitCode -ne 0) {
    Write-Error $out
}
