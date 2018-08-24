# check_gofmt.ps1
# Fail if a .go file hasn't been formatted with gofmt
$GO_FILES = Get-ChildItem -Recurse -Filter *.go  -Name | Select-String -Pattern $(Join-Path "vendor" | Join-Path -ChildPath "") -NotMatch
$out = & gofmt -s -l $GO_FILES
if ($out.length -gt 0) {
    Write-Error $out
}
