# IGNGbbs应用（igngbbs-android）· 项目 Agent 规范
> 只写本仓库与设备级规范的差异。Git 纪律、worktree、冲突处理见 `~/.qoder/coder-rules/global-rules.md`。

Collaboration: collaborative
Default branch: main
Integration: direct-after-validation
Release: manual，发布脚本由人执行（**本仓库无 CI workflow**）
Worktree: `~/项目/.wt/IGNGbbs应用/<slug>`

## 这是什么
IGNG BBS 的 Android 客户端（工程在 `Android/`，Kotlin）。

## 接口归属（保留原 AGENTS.md 的唯一约定）
- 站点端是同级目录 `~/项目/IGNG/IGNG站点`。涉及接口新增或变更时，在站点仓库修改并遵循其文档完成构建与上传；不得在客户端复制或伪造服务端规则。

## 验证命令
- `cd Android && ./gradlew assembleDebug`（AGP 标准任务，**尚未在本仓库实测跑通**）；失败要报告缺口，不得跳过验证。
- 发布与备份脚本 `publish-release.ps1`、`backup-release-to-nas.ps1` 属 Windows 侧，**由用户执行**，Agent 只给命令。

## 项目特殊限制
- `nas-backup.local.psd1` 含 NAS 访问信息，已被 `.gitignore` 排除（仓库里只有 `nas-backup.local.example.psd1`）：**不得** `git add -f` 它，也不得把其中口令抄进任何文档、日志或提交信息。
- 构建产物与 APK 不提交；已有 Release 与 tag 不得移动、删除或覆盖，新版本用新版本号与新文件名。
