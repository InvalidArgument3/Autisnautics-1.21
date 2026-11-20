<#
  Autisnautics updater bootstrap (Windows).

  Drops autis-updater.jar into a Prism/MultiMC instance and writes the pre-launch
  command into instance.cfg. One-time step for someone who imported the pack
  before the updater existed. Prism rewrites instance.cfg on exit, so it must be
  closed while this runs.

  Usage:
    powershell -ExecutionPolicy Bypass -File autis-bootstrap.ps1 -Instance "U:\...\instances\Autisnautics 1.21"
    powershell -ExecutionPolicy Bypass -File autis-bootstrap.ps1 -Find
#>
param(
  [string]$Instance,
  [switch]$Find,
  [string]$Repo = "InvalidArgument3/Autisnautics-1.21",
  [string]$JarUrl = ""
)

$ErrorActionPreference = "Stop"

if (Get-Process prismlauncher, multimc -ErrorAction SilentlyContinue) {
  throw "Prism/MultiMC is running. Close it first - it rewrites instance.cfg on exit."
}

$roots = @(
  (Join-Path $env:APPDATA "PrismLauncher\instances"),
  (Join-Path $env:LOCALAPPDATA "PrismLauncher\instances"),
  "C:\ProgramData\PrismLauncher\instances"
)

if (-not $Instance) {
  $found = @()
  foreach ($r in $roots) {
    if (Test-Path $r) {
      $found += Get-ChildItem $r -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName "instance.cfg") }
    }
  }
  if ($Find) {
    $found | ForEach-Object { $_.FullName }
    exit 0
  }
  $match = $found | Where-Object { $_.Name -like "*Autisnautics*" }
  if ($match.Count -eq 1) {
    $Instance = $match[0].FullName
  }
  else {
    throw "Pass -Instance <path>. Candidates:`n" + (($match | ForEach-Object { "  " + $_.FullName }) -join "`n")
  }
}

$cfg = Join-Path $Instance "instance.cfg"
$mc = Join-Path $Instance "minecraft"
if (-not (Test-Path $cfg)) { throw "No instance.cfg in $Instance" }
if (-not (Test-Path $mc)) { throw "No minecraft folder in $Instance" }

$jarDir = Join-Path $mc "autis"
New-Item -ItemType Directory -Force -Path $jarDir | Out-Null
if (-not $JarUrl) { $JarUrl = "https://github.com/$Repo/releases/latest/download/autis-updater.jar" }
$jarUrl = $JarUrl
Write-Host "Downloading $jarUrl"
Invoke-WebRequest -Uri $jarUrl -OutFile (Join-Path $jarDir "updater.jar") -UseBasicParsing

$command = '"$INST_JAVA" -jar "$INST_MC_DIR/autis/updater.jar" --game-dir "$INST_MC_DIR"'
$encoded = 'PreLaunchCommand="' + $command.Replace('"', '\"') + '"'

$text = [IO.File]::ReadAllText($cfg)
$nl = if ($text.Contains("`r`n")) { "`r`n" } else { "`n" }
$lines = $text -split [regex]::Escape($nl)

$kept = $lines | Where-Object { $_ -notlike "OverrideCommands=*" -and $_ -notlike "PreLaunchCommand=*" }
$out = New-Object System.Collections.Generic.List[string]
$inserted = $false
foreach ($line in $kept) {
  $out.Add($line)
  if (-not $inserted -and $line -eq "InstanceType=OneSix") {
    $out.Add("OverrideCommands=true")
    $out.Add($encoded)
    $inserted = $true
  }
}
if (-not $inserted) {
  $idx = $out.IndexOf("[General]")
  if ($idx -lt 0) { $out.Insert(0, "[General]"); $idx = 0 }
  $out.Insert($idx + 1, "OverrideCommands=true")
  $out.Insert($idx + 2, $encoded)
}
[IO.File]::WriteAllText($cfg, ($out -join $nl), (New-Object Text.UTF8Encoding $false))

Write-Host ""
Write-Host "Installed: $jarDir\updater.jar"
Write-Host "Patched:   $cfg"
Write-Host "Start the launcher and press Play - the pack syncs itself before Minecraft starts."
