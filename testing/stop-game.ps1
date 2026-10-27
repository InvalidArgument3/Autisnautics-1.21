. "$PSScriptRoot\lib.ps1"
$ok = Stop-MinecraftGame
if ($ok) { Write-Host "Game stopped (or was not running)." ; exit 0 }
Write-Host "Timed out waiting for JVM exit." ; exit 1
