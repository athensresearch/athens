[CmdletBinding()]
Param(
	[Parameter(Mandatory=$false)]
	[switch]$build,

	[Parameter(Mandatory=$false)]
	[switch]$run,

	[Parameter(Mandatory=$false)]
	[switch]$docs,

	[Parameter(Mandatory=$false)]
	[Alias("setup-dev-env")]
	[switch]$setup_dev_env,

	[Parameter(Mandatory=$false)]
	[switch]$verify,

	[Parameter(Mandatory=$false)]
	[switch]$test,

	[Parameter(Mandatory=$false)]
	[Alias("test-unit")]
	[switch]$test_unit,

	[Parameter(Mandatory=$false)]
	[Alias("test-e2e")]
	[switch]$test_e2e,

	[Parameter(Mandatory=$false)]
	[switch]$docker,

	[Parameter(Mandatory=$false)]
	[Alias("olympus-docker")]
	[switch]$olympus_docker,

	[Parameter(Mandatory=$false)]
	[Alias("proxy-docker")]
	[switch]$proxy_docker,

	[Parameter(Mandatory=$false)]
	[switch]$bench,

	[Parameter(Mandatory=$false)]
	[switch]$alldeps,

	[Parameter(Mandatory=$false)]
	[switch]$dev,

	[Parameter(Mandatory=$false)]
	[switch]$down
)
function execScript($name) {
	$scriptsDir = "$(join-path scripts ps)"
	& "$(Join-Path $scriptsDir $name)"
}

if ($setup_dev_env.IsPresent) {
	execScript "get_dev_tools.ps1"
	& docker-compose -p athensdev up -d mongo
	& docker-compose -p athensdev up -d redis
}

if ($build.IsPresent) {
	try {
		Push-Location $(Join-Path cmd proxy)
		& buffalo build
	}
	finally {
		Pop-Location
	}
	
	try {
		Push-Location $(Join-Path cmd olympus) 
		& buffalo build
	}
	finally {
		Pop-Location
	}
}

if ($run.IsPresent) {
	Set-Location $(Join-Path cmd proxy)
	& buffalo dev
}

if ($docs.IsPresent) {
	Set-Location docs 
	& hugo
}

if ($verify.IsPresent) {
	execScript "check_gofmt.ps1"
	execScript "check_golint.ps1"
	execScript "check_deps.ps1"
}

if ($alldeps.IsPresent) {
	& docker-compose -p athensdev up -d mongo
	& docker-compose -p athensdev up -d redis
	& docker-compose -p athensdev up -d minio
	& docker-compose -p athensdev up -d jaeger
	Write-Host "sleeping for a bit to wait for the DB to come up"
	Start-Sleep 5
}

if ($dev.IsPresent) {
	& docker-compose -p athensdev up -d mongo
	& docker-compose -p athensdev up -d redis
}

if ($test.IsPresent) {
	try {
		Push-Location  $(Join-Path cmd proxy)
		& buffalo test
	}
	finally {
		Pop-Location
	}
	try {
		Push-Location $(Join-Path cmd olympus) 
		& buffalo test
	}
	finally {
		Pop-Location
	}
}

if ($test_unit.IsPresent) {
	execScript "test_unit.ps1"
}

if ($test_e2e.IsPresent) {
	execScript "test_e2e.ps1"
}

if ($docker.IsPresent) {
	& docker build -t gomods/olympus -f cmd/olympus/Dockerfile .
	& docker build -t gomods/proxy -f cmd/proxy/Dockerfile .
}

if ($olympus_docker.IsPresent) {
	& docker build -t gomods/olympus -f cmd/olympus/Dockerfile .
}

if ($proxy_docker.IsPresent) {
	& docker build -t gomods/proxy -f cmd/proxy/Dockerfile .
}

if ($bench.IsPresent) {
	execScript "benchmark.ps1"
}

if ($down.IsPresent) {
	& docker-compose -p athensdev down -v
}

