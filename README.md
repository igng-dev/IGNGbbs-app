# IGNG BBS Android

IGNG BBS Android 是面向 `IGNG BBS` 社区的官方原生 Android 客户端，基于 **Kotlin + Jetpack Compose** 与 **IGNG Bento 2.0** 设计语言原生重构，专注提供顺滑舒适的移动端阅读、互动、通知、自研块级所见即所得写作以及掌上社区治理体验。

- 正式包名：`net.igng.bbs`（最低支持 Android 8.0 / `minSdk = 26`）
- 领域边界：**专注 IGNG BBS 社区**，不包含 Ink 系列功能
- 项目仓库：[igng-dev/IGNGbbs-app](https://github.com/igng-dev/IGNGbbs-app)
- Release 下载：[Releases](https://github.com/igng-dev/IGNGbbs-app/releases)

## 核心特性

### 1. IGNG Bento 2.0 视觉与舒适动效体系
- 全套 Bento 2.0 卡片容器、1dp 细线描边、表格式数字对齐与浅色/深色主题
- 以“看得更舒服”为核心的物理减速动效文法（`IgngMotion`）：支持「完整动效 / 轻柔动效 / 减少动态」三档强度与系统无障碍动态联动
- 专属「折角核心」图形化加载态（内置 `140ms / 160ms / 240ms / 180ms` 防闪烁包络）与卡片微按压反馈

### 2. OAuth 2.1 PKCE 安全认证与多账户管理
- 采用一方 `igng_bbs_android` OAuth 2.1 Authorization Code + PKCE S256 授权（系统 Custom Tabs，零 WebView 密码收集）
- 硬件级 Android Keystore `AES-256-GCM` 加密多账户凭据库，支持单飞（Single-Flight）令牌轮换刷新与防重放吊销
- 内置多账户快速切换、每日签到 EXP 自动结算提示以及登录设备管理（查看 / 重命名 / 吊销）

### 3. 首页推荐、专栏广场与原生阅读器
- **首页信息流**：社区精选横向卡片（热议 / 升温 / 讨论 / 新作者等）、本地断点「继续阅读」进度卡、分类与热门标签轨、最新/热门排序、同系列文章手风琴折叠
- **专栏广场**：支持多级嵌套专栏面包屑、子专栏浏览、置顶文章以及开放/申请加入与直接/审核/口令订阅状态机
- **原生 Markdown / HTML 阅读器**：零 WebView，原生渲染标题、引用、语法代码块（一键复制）、表格、任务清单、多图缩放画廊灯箱、目录跳转、暖纸/深墨/默认画布与字号行距调节、`ban_status=4` 受限内容提示门控
- **评论与互动**：嵌套楼层回复、`最早/最新/最热` 排序、15 秒轻量新评论提醒条、折叠评论展开、未通过评论在线重提、`@` 用户联想提及与 12 类社区规范举报
- **App Links 深度链接**：支持 `https://www.igngbbs.net/post/...`、`user/...`、`columns/...`、`tags/...`、`pages/rules` 直达客户端

### 4. 创作中心与自研原生块级所见即所得编辑器
- **自研块级 WYSIWYG 编辑器**（Jetpack Compose）：支持 H1–H4 标题、正文、无序/有序/待办列表、引用、代码块（语言切换）、表格、分割线、相册直传图片（Photo Picker）与视频嵌入
- 支持块上下移动、块类型转换、行内加粗/斜体/代码/高亮/删除线/链接快捷条、撤销/重做历史、本地草稿防丢恢复，以及一键无损切换「块级编辑 / Markdown 源码 / 实时阅读预览」
- **全功能创作者工具**：30 天阅读趋势折线图与来源分布、今日投稿配额、文章草稿/历史版本恢复/协作者邀请管理、专栏与事件审批、系列章节编排、评论管理、双域风控仪表盘、审核与申诉 v2（多轮申诉）、云端媒体资产与配额管理

### 5. 统一收件箱与掌上社区治理台
- **统一收件箱**：整合系统通知、`@` 提及、回复与专栏事件，支持检索、仅看未读、已读同步与 WorkManager 后台定期提醒
- **社区治理控制台（RBAC 实时门控）**：仅对具备 BBS 管理权限的账户可见，覆盖帖子治理（状态/可见度/封禁）、人工审核队列、举报队列处置、用户双域风控（`article` / `comment`）设置与解除、用户检索及专栏/系列管理

## 模块架构（14 Modules）

- `:app` — 应用壳层、Navigation Compose 路由、App Links / 快捷方式与后台通知同步
- `:core:model` — 站点端点、偏好配置、账户模型与 Client API v1 全量 DTO
- `:core:network` — OkHttp 客户端、统一错误映射、`Idempotency-Key` 与 Single-Flight 401 刷新器
- `:core:auth` — PKCE S256、Android Keystore AES-256-GCM 凭据库、Custom Tabs OAuth 管理器
- `:core:designsystem` — IGNG Bento 2.0 主题、动效文法、折角核心加载器、图表、灯箱与 BBS 业务组件
- `:core:markdown` — 原生 Markdown / HTML AST 解析器、Compose 渲染画布与自研块级 WYSIWYG 文档编解码器
- `:core:data` — DataStore 本地持久化、SWR 磁盘缓存、7 大业务 Repository 与 DI 容器
- `:feature:feed` / `:feature:column` / `:feature:post` / `:feature:inbox` / `:feature:creator` / `:feature:profile` / `:feature:admin`

## 本地开发与构建

### 环境要求
- JDK 21（Gradle Daemon 工具链锁定的主版本）
- Android SDK（`compileSdk 36`，`minSdk 26`）

### 构建与单元测试

```bash
cd Android
./gradlew assembleDebug testDebugUnitTest
```

Windows PowerShell：

```powershell
cd Android
.\gradlew.bat assembleDebug testDebugUnitTest
```

默认服务地址可通过 Gradle 属性覆写：
- `IGNG_API_BASE_URL=https://www.igng.net`
- `IGNG_BBS_BASE_URL=https://www.igngbbs.net`
- `IGNG_SSO_BASE_URL=https://sso.igng.net`

### 隐私检查

提交前必须通过敏感信息扫描：

```bash
bash scripts/check-privacy.sh --all
```

## 参与贡献与安全反馈

- 贡献流程与钩子配置见 [CONTRIBUTING.md](CONTRIBUTING.md)
- 安全漏洞与敏感信息反馈方式见 [SECURITY.md](SECURITY.md)

## License

MIT
