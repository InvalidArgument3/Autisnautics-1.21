# Launch Autisnautics via Prism into the test singleplayer world.
# IMPORTANT: never use $args (PowerShell automatic variable) - it breaks --launch "Autisnautics 1.21"
# into a positional URL "1.21" which Qt treats as http://1.0.0.21.
param(
    [string]$World = "",
    [string]$OfflineName = "AutisSmoke",
    [switch]$ShowWindow,
    [switch]$Wait
)

. "$PSScriptRoot\lib.ps1"
Assert-PrismExe

if (-not $World) { $World = $TestWorldName }

& "$PSScriptRoot\ensure-test-world.ps1"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$existing = @(Get-MinecraftJavaProcesses)
if ($existing.Count -gt 0) {
    Write-Host "Minecraft already running (pids: $($existing.ProcessId -join ', ')). Stop it first or use stop-game.ps1."
    exit 2
}

# Kill stray Prism UI so --dir is not forwarded to some empty default window
Get-Process prismlauncher -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Milliseconds 500

# Build one argument string with quoted values (spaces in instance id).
$prismArgList = @(
    "--dir", "`"$PrismRoot`"",
    "--launch", "`"$InstanceId`"",
    "--world", "`"$World`"",
    "--offline", "`"$OfflineName`""
)
if ($ShowWindow) { $prismArgList += "--show-window" }

$argString = $prismArgList -join " "
Write-Host "Launching: `"$PrismExe`" $argString"

$start = @{
    FilePath         = $PrismExe
    ArgumentList     = $argString
    WorkingDirectory = $PrismRoot
    PassThru         = $true
}
$p = Start-Process @start
Write-Host "Prism PID $($p.Id) started (game JVM comes up after assets/mods load)."

if ($Wait) {
    Wait-Process -Id $p.Id
    exit $p.ExitCode
}
exit 0
