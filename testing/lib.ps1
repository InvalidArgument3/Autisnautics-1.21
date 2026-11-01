# Shared paths for Autisnautics Prism smoke tests.
# Instance folder:  U:\Games-NVME\Prism Launcher\instances\Autisnautics 1.21
# Live launcher:    U:\Games-NVME\Prism Launcher\prismlauncher.exe  (NOT backup_*)
$ErrorActionPreference = "Stop"

$script:TestingRoot = $PSScriptRoot
$script:MinecraftRoot = Split-Path $TestingRoot -Parent
$script:InstanceRoot = Split-Path $MinecraftRoot -Parent
$script:InstancesDir = Split-Path $InstanceRoot -Parent
$script:PrismRoot = Split-Path $InstancesDir -Parent
$script:InstanceId = Split-Path $InstanceRoot -Leaf

# Prefer env override, else live Prism next to instances/ (never backup_* folders).
if ($env:AUTIS_PRISM_EXE -and (Test-Path -LiteralPath $env:AUTIS_PRISM_EXE)) {
    $script:PrismExe = $env:AUTIS_PRISM_EXE
    $script:PrismRoot = Split-Path $script:PrismExe -Parent
} elseif ($env:AUTIS_PRISM_ROOT -and (Test-Path -LiteralPath (Join-Path $env:AUTIS_PRISM_ROOT "prismlauncher.exe"))) {
    $script:PrismRoot = $env:AUTIS_PRISM_ROOT
    $script:PrismExe = Join-Path $script:PrismRoot "prismlauncher.exe"
} else {
    $script:PrismExe = Join-Path $script:PrismRoot "prismlauncher.exe"
}

$script:SavesDir = Join-Path $MinecraftRoot "saves"
$script:LogsDir = Join-Path $MinecraftRoot "logs"
$script:CrashDir = Join-Path $MinecraftRoot "crash-reports"
$script:OutDir = Join-Path $TestingRoot "out"
$script:TestWorldName = "AutisTest"

function Assert-PrismExe {
    if (-not (Test-Path -LiteralPath $script:PrismExe)) {
        throw "Prism not found at $($script:PrismExe). Set AUTIS_PRISM_EXE to the live prismlauncher.exe."
    }
    $full = (Resolve-Path -LiteralPath $script:PrismExe).Path
    if ($full -match '\\backup_[^\\]+\\') {
        throw "Refusing old Prism backup: $full - use U:\Games-NVME\Prism Launcher\prismlauncher.exe"
    }
    $ver = (Get-Item -LiteralPath $full).VersionInfo.FileVersion
    Write-Host "Prism: $full (v$ver)"
    Write-Host "Instance: $($script:InstanceId) @ $($script:InstanceRoot)"
}

function Get-LatestLogPath {
    Join-Path $script:LogsDir "latest.log"
}

function Ensure-OutDir {
    New-Item -ItemType Directory -Force -Path $script:OutDir | Out-Null
}

function Get-PrismLaunchProcesses {
    # Prism instance launched with --launch "<InstanceId>"
    $inst = $script:InstanceId
    Get-CimInstance Win32_Process -Filter "Name = 'prismlauncher.exe'" -ErrorAction SilentlyContinue |
        Where-Object {
            $_.CommandLine -and (
                $_.CommandLine -like "*--launch*$inst*" -or
                $_.CommandLine -like "*--launch `"$inst`*" -or
                $_.CommandLine -like "*--launch $inst*"
            )
        }
}

function Get-MinecraftJavaProcesses {
    # Sodium/NeoForge often rewrite Win32 CommandLine to a short main-class stub
    # (e.g. "net.caffeinemc.sodium / net.minecraft.client.main.Main /"), so path/
    # instance/neoforge matching alone misses the live client.
    $inst = $script:InstanceId
    $mc = $script:MinecraftRoot
    $prismPids = @((Get-PrismLaunchProcesses | ForEach-Object { $_.ProcessId }))

    Get-CimInstance Win32_Process -Filter "Name = 'javaw.exe' OR Name = 'java.exe'" -ErrorAction SilentlyContinue |
        Where-Object {
            $cl = $_.CommandLine
            if (-not $cl) { return $false }
            if ($cl -like "*redhat.java*" -or $cl -like "*jdt.ls*" -or $cl -like "*LanguageServer*") {
                return $false
            }
            if ($cl -like "*$inst*" -or $cl -like "*$mc*" -or $cl -like "*neoforge*") { return $true }
            if ($cl -like "*minecraft.client.main*" -or $cl -like "*caffeinemc.sodium*") { return $true }
            if ($cl -like "*crash_assistant*") { return $true }
            if ($prismPids -contains $_.ParentProcessId) { return $true }
            $false
        }
}

function Stop-MinecraftGame {
    param([int]$TimeoutSec = 30)
    $procs = @(Get-MinecraftJavaProcesses)
    foreach ($p in $procs) {
        try { Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue } catch {}
    }
    # Also close the Prism process that owns this --launch (leaves GUI Prism alone).
    foreach ($p in @(Get-PrismLaunchProcesses)) {
        try { Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue } catch {}
    }
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        if (-not (Get-MinecraftJavaProcesses) -and -not (Get-PrismLaunchProcesses)) { return $true }
        Start-Sleep -Seconds 1
    }
    return (-not [bool](Get-MinecraftJavaProcesses)) -and (-not [bool](Get-PrismLaunchProcesses))
}
