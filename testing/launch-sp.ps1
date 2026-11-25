# Launch Autisnautics via Prism into the test singleplayer world.
# IMPORTANT: never use $args (PowerShell automatic variable) - it breaks --launch "Autisnautics 1.21"
# into a positional URL "1.21" which Qt treats as http://1.0.0.21.
param(
    [string]$World = "",
    [string]$OfflineName = "",
    [switch]$ShowWindow,
    [switch]$Headless,
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

# Bypass missing dev updater.jar in instance.cfg if needed
$instanceCfg = Join-Path $InstanceRoot "instance.cfg"
$updaterJar = Join-Path $MinecraftRoot "autis\updater.jar"
if ((Test-Path -LiteralPath $instanceCfg) -and -not (Test-Path -LiteralPath $updaterJar)) {
    $cfgText = Get-Content -LiteralPath $instanceCfg -Raw
    if ($cfgText -match '(?m)^OverrideCommands=true\r?$') {
        $cfgText = $cfgText -replace '(?m)^OverrideCommands=true(\r?)$', 'OverrideCommands=false$1'
        [System.IO.File]::WriteAllText($instanceCfg, $cfgText, [System.Text.UTF8Encoding]::new($false))
        Write-Host "Dev bypass: set OverrideCommands=false in instance.cfg (no autis/updater.jar)."
    }
}

# Handle audio muting for -Headless (restored by stop-game.ps1)
$bridgeDir = Join-Path $MinecraftRoot "testing\bridge"
New-Item -ItemType Directory -Force -Path $bridgeDir | Out-Null
$volBackup = Join-Path $bridgeDir "prev_master_vol.txt"
$optionsTxt = Join-Path $MinecraftRoot "options.txt"

if ($Headless) {
    if (Test-Path -LiteralPath $optionsTxt) {
        $optLines = Get-Content -LiteralPath $optionsTxt
        $prevVol = $null
        $updated = $optLines | ForEach-Object {
            if ($_ -match '^soundCategory_master:(.*)$') {
                $prevVol = $Matches[1]
                "soundCategory_master:0.0"
            } else { $_ }
        }
        if ($null -ne $prevVol -and -not (Test-Path -LiteralPath $volBackup)) {
            [System.IO.File]::WriteAllText($volBackup, $prevVol, [System.Text.UTF8Encoding]::new($false))
        } elseif ($null -eq $prevVol) {
            $updated += "soundCategory_master:0.0"
        }
        [System.IO.File]::WriteAllLines($optionsTxt, $updated, [System.Text.UTF8Encoding]::new($false))
        Write-Host "Headless mode: muted soundCategory_master in options.txt (saved previous: $prevVol)."
    }
} elseif (Test-Path -LiteralPath $volBackup) {
    $prevVol = (Get-Content -LiteralPath $volBackup -Raw).Trim()
    if (Test-Path -LiteralPath $optionsTxt) {
        $optLines = Get-Content -LiteralPath $optionsTxt
        $updated = $optLines | ForEach-Object {
            if ($_ -match '^soundCategory_master:') { "soundCategory_master:$prevVol" } else { $_ }
        }
        [System.IO.File]::WriteAllLines($optionsTxt, $updated, [System.Text.UTF8Encoding]::new($false))
    }
    Remove-Item -LiteralPath $volBackup -Force -ErrorAction SilentlyContinue
}

# Kill stray Prism UI so --dir is not forwarded to some empty default window
Get-Process prismlauncher -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
while (Get-Process prismlauncher -ErrorAction SilentlyContinue) { Start-Sleep -Milliseconds 200 }
Start-Sleep -Seconds 1

# Build one argument string with quoted values (spaces in instance id).
$prismArgList = @(
    "--dir", "`"$PrismRoot`"",
    "--launch", "`"$InstanceId`"",
    "--world", "`"$World`""
)
if ($OfflineName) { $prismArgList += @("--offline", "`"$OfflineName`"") }
if ($ShowWindow -and -not $Headless) { $prismArgList += "--show-window" }

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