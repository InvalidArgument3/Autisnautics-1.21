# End-to-end: ensure world, launch Prism SP, watch log, optional stop.
param(
    [int]$TimeoutSec = 900,
    [string]$OfflineName = "AutisSmoke",
    [switch]$KeepRunning
)

. "$PSScriptRoot\lib.ps1"
Ensure-OutDir

Write-Host "=== Autisnautics smoke ==="
Write-Host "Instance: $InstanceId"
Write-Host "Prism:    $PrismExe"
Write-Host "World:    $TestWorldName"
Write-Host "Timeout:  ${TimeoutSec}s"

& "$PSScriptRoot\launch-sp.ps1" -OfflineName $OfflineName
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$watchArgs = @{ TimeoutSec = $TimeoutSec }
if (-not $KeepRunning) { $watchArgs["StopWhenDone"] = $true }

& "$PSScriptRoot\watch-smoke.ps1" @watchArgs
exit $LASTEXITCODE
