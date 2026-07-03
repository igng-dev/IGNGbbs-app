$ErrorActionPreference = 'Stop'

$configPath = Join-Path $PSScriptRoot 'nas-backup.local.psd1'

if (-not (Test-Path $configPath)) {
    throw "Missing NAS config: $configPath"
}

$config = Import-PowerShellDataFile -Path $configPath
$nasIp = $config.nasIp
$nasUser = $config.nasUser
$nasPassword = $config.nasPassword
$nasShare = $config.nasShare

$sourceDir = Join-Path $PSScriptRoot 'artifacts\release\v1.0'
$files = @(
    Join-Path $sourceDir 'IGNGBBS-v1.0-release.apk'
    Join-Path $sourceDir 'IGNGBBS-v1.0-release.apk.sha256'
)

foreach ($file in $files) {
    if (-not (Test-Path $file)) {
        throw "Missing release artifact: $file"
    }
}

cmdkey /add:$nasIp /user:$nasUser /pass:$nasPassword | Out-Null

if (-not (Test-Connection -ComputerName $nasIp -Count 1 -Quiet)) {
    throw "NAS $nasIp is unreachable."
}

if (-not (Test-Path $nasShare)) {
    throw "NAS share is unavailable: $nasShare"
}

foreach ($file in $files) {
    Copy-Item -LiteralPath $file -Destination $nasShare -Force
}

Write-Output "Copied release artifacts to $nasShare"
