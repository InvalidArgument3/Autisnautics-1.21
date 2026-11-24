# Send a command to the KubeJS dev bridge (server or client).
# Usage:
#   .\testing\bridge.ps1 server '{"cmd":"ping"}'
#   .\testing\bridge.ps1 client '{"cmd":"screenshot","file":"cap_overworld.png"}'
param(
    [Parameter(Mandatory)][ValidateSet('server', 'client')][string]$Target,
    [Parameter(Mandatory)][string]$Payload,
    [int]$TimeoutSec = 30
)

$ErrorActionPreference = 'Stop'
$mcRoot = Split-Path $PSScriptRoot -Parent
$bridgeRoot = Join-Path $mcRoot 'testing\bridge'
New-Item -ItemType Directory -Force -Path $bridgeRoot | Out-Null

$reqPath = Join-Path $bridgeRoot ($Target + '_req.json')
$resPath = Join-Path $bridgeRoot ($Target + '_res.json')

$obj = $Payload | ConvertFrom-Json
$id = [guid]::NewGuid().ToString('N')
$obj | Add-Member -NotePropertyName 'id' -NotePropertyValue $id -Force
$json = $obj | ConvertTo-Json -Depth 10 -Compress

[System.IO.File]::WriteAllText($reqPath, $json, [System.Text.UTF8Encoding]::new($false))

$deadline = (Get-Date).AddSeconds($TimeoutSec)
while ((Get-Date) -lt $deadline) {
    if (Test-Path -LiteralPath $resPath) {
        try {
            $text = [System.IO.File]::ReadAllText($resPath, [System.Text.UTF8Encoding]::new($false))
            if ($text -and $text.Contains($id)) {
                Write-Output $text
                exit 0
            }
        } catch {}
    }
    Start-Sleep -Milliseconds 150
}

Write-Error "Bridge timeout ($TimeoutSec s) waiting for $Target response"
exit 2
