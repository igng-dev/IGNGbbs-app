$ErrorActionPreference = 'Stop'

$repo = 'IGNGserver/igngbbs-android'
$tag = 'v1.0'
$releaseName = 'v1.0'
$apkPath = Join-Path $PSScriptRoot 'Android\app\build\outputs\apk\release\app-release.apk'
$assetName = 'IGNGBBS-v1.0-release.apk'
$shaPath = Join-Path $PSScriptRoot 'Android\app\build\outputs\apk\release\app-release.apk.sha256'

$sha = (Get-FileHash $apkPath -Algorithm SHA256).Hash.ToLower()
Set-Content -Path $shaPath -Value "$sha  $assetName"

$cred = @'
protocol=https
host=github.com

'@ | git credential-manager get

$username = ($cred | Select-String '^username=').ToString().Split('=')[1]
$password = ($cred | Select-String '^password=').ToString().Split('=')[1]

if ([string]::IsNullOrWhiteSpace($password)) {
    throw 'GitHub credential manager did not return a usable token.'
}

$headers = @{
    Authorization = "Bearer $password"
    'User-Agent' = 'Codex'
    Accept = 'application/vnd.github+json'
    'X-GitHub-Api-Version' = '2022-11-28'
}

try {
    $release = Invoke-RestMethod -Headers $headers -Uri "https://api.github.com/repos/$repo/releases/tags/$tag" -Method Get
} catch {
    $body = @{
        tag_name = $tag
        target_commitish = 'main'
        name = $releaseName
        body = "First public release of the IGNGBBS Android client.`n`n- Signed release APK`n- Source code published on GitHub`n- SHA-256 checksum attached"
        draft = $false
        prerelease = $false
        generate_release_notes = $false
    } | ConvertTo-Json

    $release = Invoke-RestMethod -Headers $headers -Uri "https://api.github.com/repos/$repo/releases" -Method Post -Body $body -ContentType 'application/json'
}

$uploadBase = ($release.upload_url -replace '\{\?name,label\}$', '')
$assets = Invoke-RestMethod -Headers $headers -Uri "https://api.github.com/repos/$repo/releases/$($release.id)/assets" -Method Get

foreach ($existing in $assets) {
    if ($existing.name -in @($assetName, "$assetName.sha256")) {
        Invoke-RestMethod -Headers $headers -Uri "https://api.github.com/repos/$repo/releases/assets/$($existing.id)" -Method Delete
    }
}

Invoke-RestMethod -Headers $headers -Uri ($uploadBase + '?name=' + [Uri]::EscapeDataString($assetName)) -Method Post -InFile $apkPath -ContentType 'application/vnd.android.package-archive' | Out-Null
Invoke-RestMethod -Headers $headers -Uri ($uploadBase + '?name=' + [Uri]::EscapeDataString("$assetName.sha256")) -Method Post -InFile $shaPath -ContentType 'text/plain' | Out-Null

[pscustomobject]@{
    release_url = $release.html_url
    tag = $release.tag_name
    sha256 = $sha
    github_user = $username
} | ConvertTo-Json -Compress
