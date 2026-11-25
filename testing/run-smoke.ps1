# End-to-end: ensure world, launch Prism SP, watch log, optional stop.
param(
    [int]$TimeoutSec = 900,
    [string]$OfflineName = "",
    [switch]$Headless,
    [switch]$ShowWindow,
    [switch]$KeepRunning
)

. "$PSScriptRoot\lib.ps1"
Ensure-OutDir

Write-Host "=== Autisnautics smoke ==="
Write-Host "Instance: $InstanceId"
Write-Host "Prism:    $PrismExe"
Write-Host "World:    $TestWorldName"
Write-Host "Headless: $Headless"
Write-Host "Timeout:  ${TimeoutSec}s"

$launchArgs = @{}
if ($OfflineName) { $launchArgs["OfflineName"] = $OfflineName }
if ($Headless) { $launchArgs["Headless"] = $true }
if ($ShowWindow) { $launchArgs["ShowWindow"] = $true }

& "$PSScriptRoot\launch-sp.ps1" @launchArgs
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$watchArgs = @{ TimeoutSec = $TimeoutSec }
if (-not $KeepRunning) { $watchArgs["StopWhenDone"] = $true }

& "$PSScriptRoot\watch-smoke.ps1" @watchArgs
exit $LASTEXITCODE