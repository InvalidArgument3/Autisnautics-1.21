. "$PSScriptRoot\lib.ps1"
$ok = Stop-MinecraftGame

# Restore master volume if launch-sp.ps1 -Headless muted it
$volBackup = Join-Path $MinecraftRoot "testing\bridge\prev_master_vol.txt"
$optionsTxt = Join-Path $MinecraftRoot "options.txt"
if (Test-Path -LiteralPath $volBackup) {
    $prevVol = (Get-Content -LiteralPath $volBackup -Raw).Trim()
    if (Test-Path -LiteralPath $optionsTxt) {
        $optLines = Get-Content -LiteralPath $optionsTxt
        $updated = $optLines | ForEach-Object {
            if ($_ -match '^soundCategory_master:') { "soundCategory_master:$prevVol" } else { $_ }
        }
        [System.IO.File]::WriteAllLines($optionsTxt, $updated, [System.Text.UTF8Encoding]::new($false))
        Write-Host "Restored soundCategory_master:$prevVol in options.txt."
    }
    Remove-Item -LiteralPath $volBackup -Force -ErrorAction SilentlyContinue
}

if ($ok) { Write-Host "Game stopped (or was not running)." ; exit 0 }
Write-Host "Timed out waiting for JVM exit." ; exit 1