$benchFiles = Get-ChildItem -Recurse -Filter "*storage*test.go" | Resolve-Path -Relative | Select-String -Pattern $(Join-Path "vendor" "") -NotMatch
& go test -v $benchFiles -bench=. -run=^$

