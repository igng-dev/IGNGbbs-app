# IGNGbbs应用（igng-dev/IGNGbbs-app）· 项目 Agent 规范

只写本仓库的领域约束、接口契约与仓库特有规则；通用 Git 纪律、worktree、冲突处理与授权语义见设备级规范 `~/.qoder/coder-rules/global-rules.md`。`CLAUDE.md` 仅作 `@AGENTS.md` 薄指针，改约定只改本文件，不要双写。

Collaboration: collaborative
Baseline: main
Default branch: main
Integration: pull-request
Release: cloud-workflow / tag-increment-only（经 `.github/workflows/release.yml` 在 GitHub Actions 云端使用仓库 Secrets 完成签名构建、验签与 Release 发布；推送 `v*.*.*` tag 或手动 `workflow_dispatch` 触发，不随 PR 合并自动发生）
Validation: 本地 `bash scripts/check-privacy.sh`（Windows：`powershell -ExecutionPolicy Bypass -File .\scripts\check-privacy.ps1`）、`git diff --cached --check`；Android 构建、Release DEX 校验与单测（`assembleDebug` / 未签名 `assembleRelease` / `verify-release-dex.py` / `testDebugUnitTest`）由 PR 上的 CI（`.github/workflows/ci.yml` 的 `verify` job）执行，内循环调试可直接在本地 worktree 工作区执行
Worktree: `~/dev/.wt/IGNGbbs应用/<slug>`

## 项目范围

- IGNG BBS 的官方原生 Android 客户端，正式包名 `net.igng.bbs`，最低版本 `minSdk = 26`，工程入口在 `Android/`（Kotlin + Jetpack Compose 14 模块架构，Gradle Daemon 要求 JDK 21）。
- **严格只覆盖 BBS 社区领域，不包含 Ink 系列**（小说 / 漫画 / 电子书 / 书评 / 读书会 / Ink 创作与阅读器）。严禁在客户端引入任何 `ink_*` 字段、接口或入口。
- 默认服务地址（`IGNG_API_BASE_URL`、`IGNG_BBS_BASE_URL`、`IGNG_SSO_BASE_URL`）写在公开 Gradle 配置里；不得把测试地址、内网地址、本机路径、账号或凭据写进源码。

## 接口与契约归属

- 客户端统一通过 `IGNG站点`（同级仓库 `~/dev/IGNG/IGNG站点`）的一方 Client API v1（`https://www.igng.net/api/v1/bbs/**`、`/api/v1/devices`、`/api/v1/users/me/**`、`/api/v1/media/**`）与 OAuth 2.1 PKCE（`igng_bbs_android`，回调 `igngbbs://auth/callback`）通信。
- 涉及接口新增或变更时，须先在 `IGNG站点` 修改并更新 `contracts/fixtures/igngbbs-v1/*.json`，再同步黄金样本至本仓库 `Android/core/data/src/test/resources/fixtures/v1/` 并通过 `DtoDeserializationTest` 校验；不得在客户端复制或伪造服务端业务规则。

## 隐私与安全（永不进入 Git / GitHub）

- `local.properties`、`.env` / `.env.*`（`.env.example` 除外）、本机个人绝对路径（`/Users/...`、`/home/...`、`C:\Users\...`）与内网私网地址/SMB 共享路径
- 任何签名密钥、口令与凭据文件（`keystore.properties`、`release-signing.properties`、`Android/signing/`、`.jks`、`.keystore`、`.p12`、`.pfx`、`.pem`、`.key`、`.der`）
- 账号密码、会话令牌、API key、SSH 私钥、Cookie、验证码、真实用户数据
- 构建产物、`artifacts/`、本地 APK / AAB 及含隐私的日志、截图、临时文件

提交前必须扫描：
- Linux / macOS：`bash scripts/check-privacy.sh`（全仓审计：`bash scripts/check-privacy.sh --all`）
- Windows：`powershell -ExecutionPolicy Bypass -File .\scripts\check-privacy.ps1`（全仓审计加 `-All`）
- 空白与冲突标记：`git diff --cached --check`

扫描失败须先清除敏感内容，不得绕过 hook（`--no-verify`）或 `git add -f` 被忽略文件。

## 构建、验证与云端发布

- **PR 验证以 CI（`.github/workflows/ci.yml` 的 `verify` job）为准**：涵盖全仓隐私扫描（`check-privacy.sh --all`）、脚本语法校验、`assembleDebug`、未签名 `assembleRelease`、Release DEX 关键符号校验（`scripts/verify-release-dex.py`）、`testDebugUnitTest` 与调试 APK artifact。引用 CI 结论必须附 run 链接与 job 名。
- **本地开发与内循环调试**：可直接在本地 worktree 工作区运行 `cd Android && ./gradlew assembleDebug testDebugUnitTest`（Windows：`.\gradlew.bat`），不把构建产物提交入库；仅文档改动（diff 仅含 `*.md`）按设备级规范执行范围校验、文档结构检查与凭据扫描并说明未跑项。
- **云端 Release 发布是唯一正式签名与发版通道（`.github/workflows/release.yml`）**：
  - 正式签名密钥统一存放在仓库 GitHub Actions Secrets（`ANDROID_KEYSTORE_BASE64`、`ANDROID_KEYSTORE_PASSWORD`、`ANDROID_KEY_ALIAS`、`ANDROID_KEY_PASSWORD`），构建时经 `ANDROID_SIGNING_*` 环境变量注入 `Android/app/build.gradle.kts`，不在工作区内维护明文签名文件。
  - 发布新版本流程：先通过 PR 递增 `Android/app/build.gradle.kts` 的 `versionCode` 与 `versionName`，合并进 `main` 后推送对应 `vX.Y.Z` tag 或在 GitHub Actions 手动触发 `Release` 工作流（支持 `dry_run=true` 仅演练签名与校验）。
- 失败的测试、构建警告或未执行的验证必须如实报告，不得声称已验证。

## 协作约束与 Release 保护

- **本仓库是 `igng-dev` 组织多人协作公开仓库，`main` 受 GitHub 分支保护与本地 `pre-push` 守卫双重约束：严禁直接推送 `main` 或对 `main` 使用 `git push --force`**。统一集成出口：任务分支 → `gh pr create` → CI（`verify`）通过 + review → Squash Merge → `wt done IGNGbbs应用 <slug>`。
- 独立克隆环境可通过 `git config core.hooksPath .githooks`（Windows：`.\scripts\install-git-hooks.ps1`）启用版本化钩子；已使用设备级 `~/.qoder/coder-rules/install-hooks.sh` 的机器会自动链式执行仓库 `.githooks/pre-commit`，无需切换 `core.hooksPath`。
- **Release 不可变保护**：所有已发布的 tag（如 `v1.0`、`v1.1`、`v2.0.0`、`v2.0.1` 等）、GitHub Release 与 APK 下载链接**必须保持不变**——不得移动、删除、覆盖旧 tag，不得删除已有 Release 或替换同名资产；只改文档或普通代码时不重新构建旧版本；未获用户明确要求不打 tag、不创建 Release。
