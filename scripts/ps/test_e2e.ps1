# Execute end-to-end (e2e) tests to verify that everything is working right
# from the end user perpsective
Get-Process -Name *buffalo*
$repoDir = Join-Path $PSScriptRoot ".." | Join-Path -ChildPath ".."
if (-not (Test-Path env:GO_BINARY_PATH)) { $env:GO_BINARY_PATH = "go" }
$globalTmpDir = [System.IO.Path]::GetTempPath()
$tmpDirName = [GUID]::NewGuid()
$testGoPath = Join-Path $globalTmpDir $tmpDirName

$origGOPATH = $env:GOPATH
$origGOPROXY = $env:GOPROXY

New-Item $testGoPath -ItemType Directory | Out-Null
$goModCache = Join-Path $testGoPath "pkg" | Join-Path -ChildPath "mod"
$env:Path += ";" + "${$(Join-Path $repoDir "bin")}"

function clearGoModCache () {
  Get-ChildItem -Path $goModCache -Recurse | Remove-Item -Recurse -Force -Confirm:$false
}

function stopProcesses () {
  Get-Process -Name buffalo* -ErrorAction SilentlyContinue | Stop-Process -Force
  Get-Process -Name athens-build* -ErrorAction SilentlyContinue | Stop-Process -Force
}

function teardown () {
  # Cleanup after our tests
  $env:GOPATH = $origGOPATH
  $env:GOPROXY = $origGOPROXY

  stopProcesses
  # clean test gopath
  Get-ChildItem -Path $testGoPath -Recurse | Remove-Item -Recurse -Force -Confirm:$false
  
  Pop-Location 
  Pop-Location
}

try {
  ## Start the proxy in the background and wait for it to be ready
  Push-Location $(Join-Path $repoDir cmd | Join-Path -ChildPath proxy)
  ## just in case something is still running
  stopProcesses
  Start-Process -NoNewWindow buffalo dev

  $proxyUp = $false
  do {
    try {
      $proxyUp = (Invoke-WebRequest  -Method GET -Uri http://localhost:3000/healthz).StatusCode -eq "200"
    }
    catch {
      Start-Sleep -Seconds 5
    }
  } while(-not $proxyUp)

  ## Clone our test repo
  $testSource = Join-Path $testGoPath "happy-path"
  git clone https://github.com/athens-artifacts/happy-path.git ${testSource}
  Push-Location ${testSource}

  ## set modules on after running buffalo dev, not sure why
  ## issue https://github.com/gomods/athens/issues/412
  $env:GO111MODULE = "on"

  $env:GOPATH = $testGoPath
  ## Make sure that our test repo works without the GOPROXY first
  if (Test-Path env:GOPROXY) { Remove-Item env:GOPROXY }
  
  & $env:GO_BINARY_PATH run .
  clearGoModCache

  ## Verify that the test works against the proxy
  $env:GOPROXY = "http://localhost:3000"
  & $env:GO_BINARY_PATH run .
}
finally {
  teardown
}
