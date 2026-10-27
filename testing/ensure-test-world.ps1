# Ensure saves/AutisTest exists (clone from New World or any existing save).
. "$PSScriptRoot\lib.ps1"

$dest = Join-Path $SavesDir $TestWorldName
if (Test-Path $dest) {
    Write-Host "Test world already exists: $TestWorldName"
    exit 0
}

if (-not (Test-Path $SavesDir)) {
    throw "No saves folder at $SavesDir - launch the pack once from Prism first."
}

$src = $null
$preferred = Join-Path $SavesDir "New World"
if (Test-Path -LiteralPath $preferred) {
    $src = $preferred
} else {
    $first = Get-ChildItem -LiteralPath $SavesDir -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($first) { $src = $first.FullName }
}

if (-not $src) {
    throw "No world to clone. Create one manually in Prism once, then re-run."
}

Write-Host "Cloning '$src' -> '$dest'"
Copy-Item -LiteralPath $src -Destination $dest -Recurse -Force
Remove-Item -LiteralPath (Join-Path $dest "session.lock") -Force -ErrorAction SilentlyContinue
Write-Host "Ready: $TestWorldName"
