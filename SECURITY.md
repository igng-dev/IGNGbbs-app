# Security Policy

## 支持版本

本项目仅在默认分支 `main` 与最新正式发布版本上提供安全修复。

## 敏感信息红线

请勿在公开 Issue、Pull Request、提交历史或日志中提交以下信息：

- 账号密码、OAuth 令牌、Cookie、API Key、SSH 私钥
- Android Release 签名密钥（`.jks` / `.keystore` / `.p12` / `.pfx` / `.pem` / `.key`）及口令
- 内网地址、本机个人绝对路径、真实用户隐私数据

本项目所有正式发布签名密钥仅存放于仓库受控的 GitHub Actions Secrets，不进入代码仓库。

## 报告安全漏洞

如发现安全漏洞、认证绕过或疑似凭据泄露，请勿在公开 Issue 中披露细节；请通过 GitHub Security Advisories 或项目维护者的私下渠道联系，并附上：

- 受影响的版本号或 Commit Hash
- 复现步骤与预期影响范围
- 脱敏后的日志或截图
