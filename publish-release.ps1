$ErrorActionPreference = 'Stop'

$repo = 'IGNGserver/igngbbs-android'
$tag = 'v1.1'
$releaseName = 'v1.1'
$apkPath = Join-Path $PSScriptRoot 'Android\app\build\outputs\apk\release\app-release.apk'
$assetName = 'IGNGBBS-v1.1-release.apk'
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

$releaseBody = @"
## IGNGBBS Android v1.1

本版本在 v1.0 基础上新增互动、订阅与管理能力。

### 新增功能

- **文章收藏**：支持收藏 / 取消收藏文章，并在“收藏文章”中集中查看
- **用户订阅**：支持订阅 / 取消订阅用户，并在“订阅用户”中管理关注关系
- **专栏订阅**：支持订阅专栏，展示已订阅 / 审核中等状态
- **首页排序**：支持“最新 / 热门”切换
- **段落评论**：支持基于段落锚点的评论、回复与删除
- **评论管理**：支持删除自己发布的评论
- **隐私设置**：可控制关注列表与收藏的可见性
- **阅读增强**：阅读器支持复制段落链接、记录阅读进度
- **全站资源管理（管理员）**：管理文章 / 页面 / 评论 / 专栏 / 系列、审核队列、举报、用户权限
- **系统通知发送（管理员）**：支持在移动端发送系统通知

### 安装说明

1. 卸载旧测试包（若签名不一致）后安装本 APK，或直接覆盖安装正式签名版本
2. 安装后登录 IGNG 账号体验新增功能
3. 管理员功能仅对具备相应权限的账号可见

### 校验

- 文件：`IGNGBBS-v1.1-release.apk`
- SHA-256：$sha
"@

try {
    $release = Invoke-RestMethod -Headers $headers -Uri "https://api.github.com/repos/$repo/releases/tags/$tag" -Method Get
} catch {
    $body = @{
        tag_name = $tag
        target_commitish = 'main'
        name = $releaseName
        body = $releaseBody
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
