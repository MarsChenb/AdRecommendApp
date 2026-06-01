# AdRecommendApp

AdRecommendApp 是一个 Android Kotlin Compose 广告推荐信息流项目，用于完成训练营课题“AI 广告推荐信息流”。

项目目标是实现一个可演示、可答辩、可扩展的单列广告信息流 App。客户端展示真实或半真实广告/商品数据，并结合真实 AI 接口生成摘要、智能标签、受众和卖点，最终提供频道切换、详情页、互动同步、标签过滤、对话式搜索和本地统计能力。

## 当前阶段

当前处于 Step 7：详情页和状态同步阶段。

已完成：

- Android Compose 空项目初始化。
- Git 仓库和远端同步。
- 开发计划文档：[DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md)
- 产品规格文档：[docs/PRODUCT_SPEC.md](docs/PRODUCT_SPEC.md)
- 技术方案文档：[docs/TECH_DESIGN.md](docs/TECH_DESIGN.md)
- 数据来源文档：[docs/DATA_SOURCE.md](docs/DATA_SOURCE.md)
- AI 使用文档：[docs/AI_USAGE.md](docs/AI_USAGE.md)
- 原始广告素材数据：[data/ads_raw.json](data/ads_raw.json)
- AI 增强广告数据：[data/ads_ai_enriched.json](data/ads_ai_enriched.json)

后续将按开发计划逐步实现 Android 数据读取、信息流 UI、详情页、搜索、统计和交付文档。

## 功能范围

### MVP 必做功能

- 单列广告信息流。
- 顶部频道切换：精选、电商、本地等。
- 三类广告卡片：大图、小图、视频样式。
- 点击卡片进入详情页。
- 返回详情页后保持列表位置。
- 点赞、收藏、分享互动。
- 列表页和详情页互动状态同步。
- 下拉刷新、上拉加载更多。
- 展示 AI 摘要和智能标签。
- 标签过滤。
- 对话式搜索。
- 本地曝光、点击、互动统计。

### 第一版不做或降级处理

- 不在 Android 端直接爬取网页。
- 不在列表滑动时实时请求 AI。
- 第一版视频播放可先做视频样式和播放状态模拟，后续再接真实播放器。
- 图片加载失败时提供占位和重试入口。

## 技术路线

推荐整体链路：

```text
真实广告/商品数据采集
→ 本地结构化 ads_raw.json
→ AI 离线生成摘要、标签、受众和卖点
→ 输出 ads_ai_enriched.json
→ Android App 从 assets 读取 enriched 数据
→ Compose 信息流展示、搜索、过滤和统计
```

这种方式可以减少运行时网络不确定性，同时保留真实数据和真实 AI 能力，适合作业演示和答辩。

## 项目结构规划

```text
app/src/main/java/com/example/adrecommend/
  MainActivity.kt
  model/
  data/
  ai/
  state/
  ui/feed/
  ui/detail/
  ui/search/
  ui/stats/

data/
  ads_raw.json
  ads_ai_enriched.json

scripts/
  enrich_ads_with_ai.py

docs/
  PRODUCT_SPEC.md
  TECH_DESIGN.md
  DATA_SOURCE.md
  AI_USAGE.md
  TEST_REPORT.md
```

## 构建运行

Android Studio 中运行：

1. 使用 Android Studio 打开项目根目录。
2. 等待 Gradle Sync 完成。
3. 在 Device Manager 中启动模拟器，例如 Pixel 7。
4. 选择 `app` 运行配置。
5. 点击 Run。

命令行构建：

```powershell
$env:JAVA_HOME='D:\app\Android\jbr'
.\gradlew.bat assembleDebug
```

如果本机已经配置好系统级 `JAVA_HOME`，可以直接执行：

```powershell
.\gradlew.bat assembleDebug
```

## Git 规范

Commit 使用如下格式：

```text
type(scope): message
```

示例：

```text
feat(feed): implement single column ad feed
fix(detail): sync favorite state after returning from detail
docs(project): add product spec and technical design
```

每一步开发完成后应满足：

- 项目可构建。
- 功能可验证。
- 有清晰 Git commit。
- 必要时同步推送到远端。

## AI 声明

本项目允许使用 AI 辅助完成开发和内容生成，但需要在最终交付文档中如实说明：

- AI 用于辅助编写代码、整理文档和生成广告摘要/标签。
- 开发者需要人工验证 AI 输出的正确性、格式稳定性和用户体验。
- AI 生成的广告结构化结果需要缓存，避免每次启动 App 重复请求。
- API key 不得提交到 Git 仓库。

## 当前验收状态

- Step 0：仓库基线整理，已完成。
- Step 1：产品范围和技术方案文档，已完成。
- Step 2：工程结构拆分，已完成。
- Step 3：真实数据 Schema 与样例数据，已完成。
- Step 4：AI 摘要和标签生成脚本，已完成。
- Step 5：Android 数据层，已完成。
- Step 6：首页信息流，已完成。
- Step 7：详情页和状态同步，已完成。
