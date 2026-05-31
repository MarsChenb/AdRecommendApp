# AdRecommendApp 技术方案

## 1. 设计目标

本项目采用 Android Kotlin + Jetpack Compose 实现广告推荐信息流 Demo，目标是在较短周期内完成稳定、可演示、可答辩的客户端工程。

核心设计目标：

- 信息流体验完整。
- 数据和状态流转清晰。
- AI 能力真实接入但不影响端侧稳定性。
- 代码结构便于分阶段提交和测试。
- 文档能支撑答辩说明。

## 2. 技术选型

### 2.1 客户端 UI

选择：Jetpack Compose + Material3。

原因：

- 当前空项目已使用 Compose。
- 适合快速构建信息流、详情页和搜索面板。
- 组件状态管理直观，方便实现点赞、收藏和过滤。
- Material3 提供基础视觉和交互组件。

### 2.2 页面导航

第一版选择：单 Activity 内部状态切换。

备选方案：Navigation Compose。

对比：

|方案|优点|缺点|结论|
|---|---|---|---|
|单 Activity 状态切换|实现快，依赖少，适合 Demo|页面复杂后需要手动管理返回栈|MVP 优先采用|
|Navigation Compose|结构标准，适合多页面扩展|需要额外接入和调试|后续复杂化时引入|

### 2.3 数据来源

选择：离线采集真实或半真实广告/商品数据，整理为 JSON。

不选择 Android 端实时爬虫的原因：

- 移动端爬虫不稳定。
- 容易受网页结构变化和反爬影响。
- 运行时网络失败会影响演示。
- 答辩时不如离线数据管线清楚。

推荐链路：

```text
公开页面或人工整理
→ data/ads_raw.json
→ AI enrich 脚本
→ data/ads_ai_enriched.json
→ app/src/main/assets/ads_ai_enriched.json
→ Android App 读取展示
```

### 2.4 AI 接入

选择：真实 AI 接口离线预处理 + Android 端缓存展示。

原因：

- 可以满足“真实 AI”的要求。
- 避免列表运行时频繁请求模型。
- 可以人工检查 AI 输出质量。
- API key 不进入 App 和 Git 仓库。
- 失败时可以保留缓存数据，不影响 App 演示。

备选方案：

|方案|优点|缺点|结论|
|---|---|---|---|
|离线 AI 预处理|稳定、可缓存、便于验证|不够实时|MVP 采用|
|App 内实时请求 AI|交互更真实|网络、费用、密钥和失败处理复杂|作为后续增强|
|纯 Mock AI|开发最快|不满足真实 AI 期望|只作为降级方案|

## 3. 模块划分

目标结构：

```text
app/src/main/java/com/example/adrecommend/
  MainActivity.kt
  model/
    AdItem.kt
    AdType.kt
    AdChannel.kt
    AdInteractionState.kt
  data/
    AdRepository.kt
    LocalAdRepository.kt
  ai/
    AiSearchMatcher.kt
  state/
    FeedUiState.kt
    AdFeedController.kt
  ui/feed/
    AdFeedScreen.kt
    AdCard.kt
    ChannelTabs.kt
  ui/detail/
    AdDetailScreen.kt
  ui/search/
    SearchPanel.kt
  ui/stats/
    StatsPanel.kt
```

脚本和数据：

```text
data/
  ads_raw.json
  ads_ai_enriched.json

scripts/
  enrich_ads_with_ai.py
```

## 4. 数据模型设计

广告基础模型：

```kotlin
data class AdItem(
    val id: String,
    val title: String,
    val brand: String,
    val description: String,
    val imageUrl: String,
    val landingUrl: String,
    val channel: AdChannel,
    val category: String,
    val priceText: String,
    val materialType: AdType,
    val aiSummary: String,
    val aiTags: List<String>,
    val audience: List<String>,
    val sellingPoints: List<String>
)
```

互动状态独立于广告内容：

```kotlin
data class AdInteractionState(
    val liked: Boolean = false,
    val favorited: Boolean = false,
    val sharedCount: Int = 0,
    val clickCount: Int = 0,
    val exposureCount: Int = 0
)
```

拆分原因：

- 广告内容来自数据集，相对稳定。
- 点赞、收藏、曝光等是本地运行态。
- 详情页和列表页共享同一份互动状态，避免不同步。

## 5. 状态管理方案

第一版采用 Compose 本地状态和轻量 controller。

核心状态：

- 当前频道。
- 当前广告列表。
- 当前分页信息。
- 当前选中的广告。
- 当前过滤标签。
- 搜索 query。
- 搜索结果。
- 互动状态 Map。
- 统计数据。

后续如果状态复杂，可引入 ViewModel。

## 6. 信息流设计

使用 `LazyColumn` 实现单列列表。

关键策略：

- 使用稳定 key：`key = ad.id`。
- 卡片组件按 `AdType` 动态选择样式。
- 频道切换时刷新数据，但保留必要的互动状态。
- 详情页返回时不销毁列表状态。
- 上拉加载通过分页状态控制。

卡片类型：

- `large_image`：大图广告，适合品牌和视觉素材。
- `small_image`：小图广告，适合商品列表。
- `video`：视频样式广告，第一版模拟播放状态。

## 7. 详情页设计

详情页通过选中的 `adId` 从数据集中查找广告内容。

互动状态从全局状态 Map 中读取和写入：

```text
AdDetailScreen
→ onLike(adId)
→ update interactionStateMap[adId]
→ Feed 列表读取同一份状态
```

这样可以保证列表和详情状态同步。

## 8. 刷新和分页设计

第一版使用本地模拟：

- 下拉刷新：重新加载当前频道数据，可调整排序或重置分页。
- 上拉加载：从本地数据集中分页追加。
- 空态：当前频道无数据或过滤无结果。
- 错误态：JSON 读取失败或解析失败。

后续接网络时，Repository 接口保持不变，只替换实现。

## 9. 搜索和标签过滤

### 9.1 标签过滤

标签来源：

- `aiTags`
- `audience`
- `category`

过滤逻辑：

```text
选中标签
→ 在当前频道数据中查找包含该标签的广告
→ 更新 Feed 列表
```

### 9.2 对话式搜索

MVP 搜索逻辑：

- 用户输入自然语言 query。
- 本地匹配标题、描述、AI 摘要、标签、受众和卖点。
- 按命中数量排序。

后续增强：

- 调用真实 AI 做 query 改写。
- 调用真实 AI 对候选广告重排。
- 缓存 query 和结果，避免重复请求。

## 10. AI 输出约束

AI enrich 脚本必须要求模型输出 JSON：

```json
{
  "aiSummary": "一句话广告摘要",
  "aiTags": ["标签1", "标签2", "标签3"],
  "audience": ["人群1", "人群2"],
  "sellingPoints": ["卖点1", "卖点2"]
}
```

约束原则：

- 摘要控制在 30-60 个中文字符。
- 标签数量建议 3-6 个。
- 标签避免过长。
- 输出必须可 JSON 解析。
- 解析失败时重试或使用降级结果。

缓存策略：

- `ads_raw.json` 保存原始数据。
- `ads_ai_enriched.json` 保存 AI 结果。
- 如果广告 id 和原始描述没有变化，不重复请求 AI。

## 11. 埋点统计设计

本项目只做本地模拟统计，不做真实商业回传。

统计指标：

- 曝光数：广告卡片首次进入可见区域后增加。
- 点击数：进入详情页时增加。
- 点赞数：用户点击点赞时更新。
- 收藏数：用户点击收藏时更新。
- 分享数：用户点击分享时增加。

统计展示：

- 总曝光。
- 总点击。
- 总互动。
- Top 广告。

口径限制：

- 第一版曝光统计可采用近似方案，不追求像商业系统一样精确。
- 最终文档需要说明统计是本地模拟。

## 12. 图片和视频策略

### 12.1 图片

第一版可以使用远程图片 URL，后续视情况加入 Coil。

降级策略：

- 图片加载中显示占位。
- 加载失败显示默认背景。
- 保留广告标题和摘要，避免卡片空白。

### 12.2 视频

第一版视频卡片采用模拟播放状态：

- 显示视频封面。
- 显示播放按钮。
- 点击后切换播放/暂停状态。
- 详情页可显示“正在播放”状态。

后续增强：

- 接入 ExoPlayer 或 Media3。
- 实现静音、暂停、续播。
- 实现播放器资源复用。

## 13. 错误处理

需要覆盖：

- JSON 文件不存在。
- JSON 解析失败。
- 当前频道无数据。
- 搜索无结果。
- 图片加载失败。
- AI enrich 失败。

原则：

- App 不崩溃。
- UI 有明确空态或错误态。
- 数据层保留错误信息，方便调试。

## 14. 测试策略

### 14.1 每步通用测试

```powershell
$env:JAVA_HOME='D:\app\Android\jbr'
.\gradlew.bat assembleDebug
```

### 14.2 功能测试

- 首页是否能打开。
- 频道切换是否正确。
- 每种卡片样式是否出现。
- 详情页状态是否同步。
- 标签过滤是否有效。
- 搜索是否返回合理结果。
- 统计数据是否变化。

### 14.3 数据测试

- JSON 格式合法。
- 必填字段完整。
- AI 输出可解析。
- 数据来源可追溯。

## 15. 风险和降级方案

|风险|影响|降级方案|
|---|---|---|
|真实数据采集困难|数据不足|人工整理 20-50 条公开商品或品牌数据|
|AI 接口不稳定|摘要和标签生成失败|保留上一次缓存结果或使用本地规则生成|
|图片加载失败|卡片视觉受影响|显示占位图和文字内容|
|视频接入耗时|影响主线进度|第一版使用视频样式模拟|
|状态同步复杂|点赞收藏不一致|将互动状态独立为全局 Map|
|命令行缺少 JAVA_HOME|无法自动构建|临时使用 Android Studio JBR 路径|

## 16. 分阶段验收

当前 Step 1 验收：

- README 存在并说明项目目标。
- `docs/PRODUCT_SPEC.md` 存在并说明功能范围。
- `docs/TECH_DESIGN.md` 存在并说明技术方案。
- 不改业务代码。
- Git commit 清晰。

下一步 Step 2：

- 拆分工程结构。
- 定义模型和 Repository 接口。
- 保持 App 可运行。
