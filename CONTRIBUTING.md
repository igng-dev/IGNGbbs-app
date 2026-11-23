# Contributing

## 首次配置 Git 钩子

- Linux / macOS：

  ```bash
  git config core.hooksPath .githooks
  ```

  > 若本机已通过设备级 `~/.qoder/coder-rules/install-hooks.sh` 管理 `.git/hooks/`，提交时会自动链式调用本仓库的 `.githooks/pre-commit`，无需切换 `core.hooksPath`。

- Windows（PowerShell）：

  ```powershell
  .\scripts\install-git-hooks.ps1
  ```

## 提交代码前

1. 从 `origin/main` 切出任务分支（如 `feat/*`、`fix/*`、`docs/*`、`refactor/*`、`agent/*`）。
2. 只暂存本次开发相关的文件：`git add <files>`。
3. 运行隐私与敏感信息扫描：
   - Linux / macOS：`bash scripts/check-privacy.sh`（全仓审计加 `--all`）
   - Windows：`powershell -ExecutionPolicy Bypass -File .\scripts\check-privacy.ps1`（全仓审计加 `-All`）
4. 运行 `git diff --cached --check` 检查空白字符与冲突标记。
5. 涉及 Android 代码改动时，确保 `cd Android && ./gradlew assembleDebug testDebugUnitTest`（Windows：`.\gradlew.bat`）或 PR 上的 CI `verify` job 通过。
6. 确认暂存区没有 `local.properties`、`.env*`、签名密钥、口令、账号令牌、内网地址、本机个人绝对路径或真实用户数据。
7. 使用 Conventional Commits 格式，例如 `feat: support column bookmarking` 或 `fix: restore reader scroll position`。

`pre-commit` hook 会自动执行 `git diff --cached --check` 与隐私扫描。检查失败时请先移除敏感内容，不要使用 `--no-verify` 绕过 hook。

## 推送与合并规则

- **禁止直接推送 `main`**；`main` 受分支保护与 `pre-push` 守卫约束。
- 统一流程：`git push -u origin <branch>` → 创建 Pull Request → 等待 CI（`verify`）通过并完成 review → Squash Merge。
- 禁止对 `main` 使用 `git push --force` 或改写受保护分支历史。
- 正式签名与发布统一由 `.github/workflows/release.yml` 在 GitHub Actions 云端使用仓库 Secrets 完成；发布新版本只使用递增版本号与新 tag，不得移动、删除或覆盖已发布 tag、Release 与历史资产。
