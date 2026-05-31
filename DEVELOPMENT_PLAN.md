# AdRecommendApp 开发计划

## 1. 项目目标

本项目实现一个 Android Kotlin Compose 版广告推荐信息流 App，面向训练营课题“AI 广告推荐信息流”。

核心目标：

- 使用真实或半真实广告/商品数据作为信息流内容。
- 使用真实 AI 接口生成广告摘要、智能标签、受众和卖点。
- Android 端实现单列信息流、频道切换、详情页、互动状态同步、搜索、标签过滤和统计展示。
- 每个开发阶段都保持项目可运行、可测试、可提交。

推荐技术路线：

```text
真实广告/商品数据采集
→ 本地结构化 JSON
→ AI 离线生成摘要/标签
→ Android 读取 enriched JSON
→ 信息流 / 详情 / 搜索 / 统计 / 交互
```

不建议在第一版中让 Android App 直接爬取数据，也不建议在列表滑动时实时请求 AI。真实 AI 能力优先通过离线预处理接入，端侧展示缓存后的结构化结果。

## 2. Git 开发规范

### 2.1 分支规范

```text
main              稳定可运行分支
feature/xxx       单个功能开发分支
fix/xxx           Bug 修复分支
docs/xxx          文档更新分支
```

### 2.2 Commit 规范

格式：

```text
type(scope): message
```

常用类型：

```text
chore   工程配置、项目初始化
docs    文档
feat    新功能
fix     Bug 修复
refactor 重构
style   UI 或格式调整
test    测试
```

示例：

```text
chore(project): add initial android compose project
docs(plan): add product and technical roadmap
feat(data): add ad data schema and sample dataset
feat(ai): add ai enrichment script
feat(feed): implement single column ad feed
fix(detail): sync favorite state after returning from detail
```

### 2.3 每一步完成标准

每个开发步骤完成后必须具备：

- 有明确功能产出。
- App 或脚本能运行。
- 有对应测试方式和测试结果。
- 有规范 Git commit。
- 不把 API key、临时文件、无关 IDE 本地配置提交到仓库。

通用构建命令：

```powershell
.\gradlew.bat assembleDebug
```

如添加单元测试：

```powershell
.\gradlew.bat testDebugUnitTest
```

## 3. 阶段开发计划

## Step 0：仓库基线整理

### 目标

保证当前仓库干净、可追踪、可回滚。

### 实际产出

- 确认 `.gitignore`。
- 确认是否提交 `.idea` 目录中的文件。
- 检查 `app/src/main/new.sh` 是否有用。
- 提交当前 Android 空项目基线。

### 测试

- Android Studio 能打开项目。
- Gradle Sync 成功。
- `assembleDebug` 成功。

### Git 记录

```text
chore(project): add initial android compose project
```

## Step 1：产品范围和技术方案文档

### 目标

先明确做什么、不做什么、如何验收。

### 实际产出

```text
README.md
docs/PRODUCT_SPEC.md
docs/TECH_DESIGN.md
```

文档内容：

- MVP 功能范围。
- 页面结构。
- 数据流。
- AI 使用方式。
- 真实数据来源策略。
- 风险与降级方案。
- 每个功能的验收标准。

### 测试

- 文档完整可读。
- 可以根据文档讲清楚项目方案。

### Git 记录

```text
docs(project): add product spec and technical design
```

## Step 2：工程结构拆分

### 目标

从默认 `MainActivity.kt` 拆出合理结构，但暂不实现复杂业务。

### 推荐结构

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
```

### 实际产出

- `AdItem`
- `AdType`
- `AdChannel`
- `AdInteractionState`
- Repository 接口
- 基础 App 入口

### 测试

- App 能打开。
- 默认页面正常显示。
- `assembleDebug` 成功。

### Git 记录

```text
refactor(project): introduce app module structure
```

## Step 3：真实数据 Schema 与样例数据

### 目标

定义广告数据格式，并准备第一批真实或半真实数据。

### 实际产出

```text
data/ads_raw.json
app/src/main/assets/ads_raw.json
docs/DATA_SOURCE.md
```

### 数据字段建议

```json
{
  "id": "ad_001",
  "title": "商品或广告标题",
  "brand": "品牌名",
  "description": "公开描述",
  "imageUrl": "图片地址",
  "landingUrl": "来源页面",
  "channel": "精选",
  "category": "数码",
  "priceText": "¥399",
  "materialType": "large_image"
}
```

### 数据来源建议

- 品牌官网公开页面。
- 电商公开商品页。
- 人工整理少量广告素材。
- 记录来源链接，保证答辩时能说明数据来源。

### 测试

- JSON 格式校验通过。
- 至少 20 条广告数据。
- 至少 3 个频道。
- 至少 3 种卡片类型。

### Git 记录

```text
feat(data): add ad data schema and initial dataset
```

## Step 4：AI 摘要和标签生成脚本

### 目标

接入真实 AI，但先在电脑端离线生成摘要和标签。

### 实际产出

```text
scripts/enrich_ads_with_ai.py
data/ads_ai_enriched.json
docs/AI_USAGE.md
```

### AI 输出字段

```json
{
  "aiSummary": "适合通勤和轻运动的降噪耳机，主打长续航与舒适佩戴。",
  "aiTags": ["数码", "通勤", "降噪", "性价比"],
  "audience": ["学生党", "上班族"],
  "sellingPoints": ["长续航", "主动降噪", "轻量佩戴"]
}
```

### 实现要求

- API key 走环境变量，不写进 Git。
- AI 输出强制 JSON。
- 失败时保留原始数据并记录错误。
- 生成结果缓存，避免重复调用。

### 测试

- 先对 3 条数据小批量测试。
- 再对全量数据生成。
- 人工检查至少 5 条摘要和标签是否合理。

### Git 记录

```text
feat(ai): add ai enrichment pipeline for ad summaries and tags
```

## Step 5：Android 数据层

### 目标

App 读取 AI 处理后的本地 JSON。

### 实际产出

- `AdRepository`
- `LocalAdRepository`
- JSON 解析逻辑
- 频道过滤
- 分页加载
- 简单错误状态

### 测试

- Repository 单元测试。
- App 能读取本地广告数据。
- 空数据、坏 JSON 有兜底。
- `assembleDebug` 成功。

### Git 记录

```text
feat(data): load enriched ads from local assets
```

## Step 6：首页信息流

### 目标

完成核心广告 Feed。

### 实际产出

- 顶部频道 Tab。
- `LazyColumn` 单列信息流。
- 大图卡片。
- 小图卡片。
- 视频样式卡片。
- AI 摘要和标签展示。

### 测试

- 三个频道可切换。
- 滚动流畅。
- 每种卡片样式都出现。
- `assembleDebug` 成功。

### Git 记录

```text
feat(feed): implement channel tabs and single column ad feed
```

## Step 7：详情页和状态同步

### 目标

点击卡片进入详情页，点赞和收藏状态同步回列表。

### 实际产出

- 详情页。
- 返回按钮。
- 点赞、收藏、分享。
- 点击统计。
- 列表和详情状态一致。

### 测试

- 点击列表卡片进入详情。
- 在详情点赞后，返回列表仍然保持点赞状态。
- 收藏状态同理。
- 点击次数增加。
- `assembleDebug` 成功。

### Git 记录

```text
feat(detail): add ad detail page with interaction sync
```

## Step 8：刷新、加载更多、加载状态

### 目标

补齐信息流基础体验。

### 实际产出

- 下拉刷新。
- 上拉加载更多。
- Loading 状态。
- 空态。
- 错误态。

### 测试

- 刷新后数据顺序或推荐内容变化。
- 滑到底部能加载更多。
- 无数据时显示空态。
- 模拟错误时 App 不崩溃。
- `assembleDebug` 成功。

### Git 记录

```text
feat(feed): add refresh load more and list states
```

## Step 9：标签过滤与对话式搜索

### 目标

做出 AI 推荐体验。

### 实际产出

- 标签点击过滤。
- 搜索输入框。
- 对话式搜索页或底部面板。
- 根据自然语言匹配广告。
- 可选：调用真实 AI 做 query 改写或排序。

### 测试

- 点击“学生党”只看相关广告。
- 输入“适合通勤的降噪耳机”能返回匹配结果。
- 搜索无结果时有提示。
- 清空搜索后恢复 Feed。
- `assembleDebug` 成功。

### Git 记录

```text
feat(search): add tag filtering and conversational ad search
```

## Step 10：统计埋点和可视化

### 目标

满足课题中的曝光、点击等统计要求。

### 实际产出

- 曝光统计。
- 点击统计。
- 点赞、收藏、分享统计。
- 简单统计面板。
- 统计口径文档。

### 测试

- 卡片进入可见区域后曝光数增加。
- 点击详情后点击数增加。
- 点赞、收藏、分享数据变化。
- 统计面板展示正确。
- `assembleDebug` 成功。

### Git 记录

```text
feat(stats): add local exposure and interaction analytics
```

## Step 11：UI 打磨和性能优化

### 目标

让 Demo 像一个完整产品，而不是功能堆叠。

### 实际产出

- 统一主题色。
- 卡片视觉优化。
- 图片加载占位和失败态。
- 列表 key 优化。
- 必要的交互动效。
- 横竖屏和小屏适配检查。

### 测试

- 冷启动正常。
- 快速滚动不卡死。
- 断网时本地数据仍可展示。
- 不同屏幕尺寸下布局不明显错乱。
- `assembleDebug` 成功。

### Git 记录

```text
style(ui): polish feed visuals and loading states
```

## Step 12：测试、文档、答辩材料

### 目标

完成最终交付物。

### 实际产出

```text
README.md
docs/TECH_DESIGN.md
docs/AI_USAGE.md
docs/DATA_SOURCE.md
docs/TEST_REPORT.md
```

README 内容：

- 项目介绍。
- 如何运行。
- 功能完成情况。
- 模块划分。
- 开发规范。
- AI 声明。
- 已知问题。
- 后续规划。

### 测试

- 完整跑一遍演示路径。
- 记录测试结果。
- 截图或录屏。
- `assembleDebug` 成功。

### Git 记录

```text
docs(delivery): add final readme test report and ai statement
```

最终可打 tag：

```text
v1.0-demo
```

## 4. 推荐执行方式

每一步开发都按以下模板推进：

```text
本步骤目标
要改哪些文件
实现内容
测试方式
Git commit message
实际完成结果
```

建议从 Step 0 开始，不急于写功能。先保证仓库状态干净，再补产品和技术文档，然后进入代码开发。

## 5. 当前建议下一步

下一步执行 Step 0：

- 检查当前 Git 状态。
- 判断哪些文件应该提交。
- 检查是否存在无用文件。
- 完成初始项目基线提交。
- 确认远端 push 成功。
