# Poll latest.log until world join / timeout; write a JSON report under testing/out.
param(
    [int]$TimeoutSec = 900,
    [int]$PollMs = 2000,
    [switch]$StopWhenDone
)

. "$PSScriptRoot\lib.ps1"
Ensure-OutDir

$logPath = Get-LatestLogPath
$deadline = (Get-Date).AddSeconds($TimeoutSec)
$reportPath = Join-Path $OutDir ("smoke-{0:yyyyMMdd-HHmmss}.json" -f (Get-Date))

# Clear stale "success" if log is old - wait for a fresh log after launch.
$launchMark = Get-Date
$sawFreshLog = $false

$successPatterns = @(
    "Joined the game",
    "Changing view distance",
    "Loaded \d+ advancements",
    "Integrated server started",
    "Saving chunks for level"
)

$failHardPatterns = @(
    "---- Minecraft Crash Report ----",
    "Failed to load registries due to above errors",
    "\]: Fatal errors were detected",
    "Netty Epoll Server IO .* EXCEPTION",
    "Reported exception thrown!"
)

function Get-MatchingLines {
    param([string]$Path, [string[]]$Patterns, [int]$Tail = 8000)
    if (-not (Test-Path $Path)) { return @() }
    $text = Get-Content $Path -Tail $Tail -ErrorAction SilentlyContinue
    if (-not $text) { return @() }
    $hits = @()
    foreach ($line in $text) {
        foreach ($pat in $Patterns) {
            if ($line -match $pat) { $hits += $line; break }
        }
    }
    return $hits
}

function Get-ErrorSummary {
    param([string]$Path)
    if (-not (Test-Path $Path)) { return @{ fatal = @(); error = @(); registry = @() } }
    $tail = Get-Content $Path -Tail 12000 -ErrorAction SilentlyContinue
    $fatal = @($tail | Where-Object { $_ -match '\[.*/FATAL\]|Reported exception thrown|Crash Report' } | Select-Object -Last 40)
    $error = @($tail | Where-Object { $_ -match '\[.*/ERROR\]' } | Select-Object -Last 80)
    $registry = @($tail | Where-Object { $_ -match 'Registry loading errors|Unbound values in registry' } | Select-Object -Last 20)
    return @{ fatal = $fatal; error = $error; registry = $registry }
}

Write-Host "Watching $logPath (timeout ${TimeoutSec}s)"
$result = "timeout"
$successHits = @()
$failHits = @()

while ((Get-Date) -lt $deadline) {
    if (Test-Path $logPath) {
        $logTime = (Get-Item $logPath).LastWriteTime
        if ($logTime -ge $launchMark.AddSeconds(-5)) { $sawFreshLog = $true }

        if ($sawFreshLog) {
            $failHits = @(Get-MatchingLines -Path $logPath -Patterns $failHardPatterns)
            if ($failHits.Count -gt 0) {
                $result = "crash"
                break
            }
            $successHits = @(Get-MatchingLines -Path $logPath -Patterns $successPatterns)
            if ($successHits.Count -gt 0) {
                # Give recipes/datapacks a moment to finish dumping errors after join
                Start-Sleep -Seconds 8
                $failHits = @(Get-MatchingLines -Path $logPath -Patterns $failHardPatterns)
                if ($failHits.Count -gt 0) { $result = "crash" } else { $result = "ok" }
                break
            }
        }
    }

    $proc = @(Get-MinecraftJavaProcesses)
    if ($sawFreshLog -and $proc.Count -eq 0) {
        $result = "exited"
        break
    }

    Start-Sleep -Milliseconds $PollMs
}

$summary = Get-ErrorSummary -Path $logPath
$crashes = @()
if (Test-Path $CrashDir) {
    $crashes = @(Get-ChildItem $CrashDir -Filter "crash-*.txt" |
        Where-Object { $_.LastWriteTime -ge $launchMark.AddMinutes(-5) } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 3 -ExpandProperty Name)
}

$report = [ordered]@{
    result        = $result
    timestamp     = (Get-Date).ToString("o")
    instance      = $InstanceId
    world         = $TestWorldName
    log           = $logPath
    success_hits  = $successHits | Select-Object -Last 10
    fail_hits     = $failHits | Select-Object -Last 20
    fatal_tail    = $summary.fatal
    error_tail    = $summary.error
    registry_tail = $summary.registry
    crash_reports = $crashes
    timeout_sec   = $TimeoutSec
}

$report | ConvertTo-Json -Depth 5 | Set-Content -Path $reportPath -Encoding utf8
Write-Host "Result: $result"
Write-Host "Report: $reportPath"
if ($summary.fatal.Count) {
    Write-Host "--- FATAL (tail) ---"
    $summary.fatal | Select-Object -Last 15 | ForEach-Object { Write-Host $_ }
}
if ($summary.registry.Count) {
    Write-Host "--- REGISTRY ---"
    $summary.registry | ForEach-Object { Write-Host $_ }
}
if ($summary.error.Count) {
    Write-Host "--- ERROR count: $($summary.error.Count) (last 10) ---"
    $summary.error | Select-Object -Last 10 | ForEach-Object { Write-Host $_ }
}

if ($StopWhenDone) {
    Write-Host "Stopping game..."
    Stop-MinecraftGame | Out-Null
}

if ($result -eq "ok") { exit 0 }
if ($result -eq "crash" -or $result -eq "exited") { exit 1 }
exit 3
