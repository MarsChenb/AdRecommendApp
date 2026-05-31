# 数据来源说明

## 1. 当前数据集

Step 3 生成了第一版原始广告素材数据：

```text
data/ads_raw.json
app/src/main/assets/ads_raw.json
```

两份文件内容完全一致：

- `data/ads_raw.json`：仓库级数据文件，供脚本、AI enrich 和人工检查使用。
- `app/src/main/assets/ads_raw.json`：Android App 可读取的 assets 数据文件。

当前数据量：

```text
24 条广告素材
```

## 2. 数据源选择

### 2.1 最终采用的数据源

当前使用 Apple iTunes Search API 获取公开 App Store 软件条目，并将这些真实软件条目标准化成广告推荐信息流的原始素材。

数据源：

- Apple iTunes Search API
- 文档地址：https://performance-partners.apple.com/search-api
- 查询接口示例：https://itunes.apple.com/search?term=fitness&country=us&media=software&entity=software&limit=8

选择原因：

- 无需 API key，适合训练营 Demo 复现。
- 返回结构稳定，包含名称、开发者、描述、图标、价格、评分、分类和 App Store 链接。
- App 推广广告是常见广告类型，适合映射成广告信息流卡片。
- 数据可追溯，每条记录都保留原始 App Store 链接。

### 2.2 未采用的数据源

最初计划使用 Open Food Facts 获取真实食品商品数据，但实际拉取时匿名请求被临时限制，接口返回页面不可用提示。为了保证 Step 3 可稳定完成，当前切换到 Apple iTunes Search API。

后续如果需要商品类广告，可以继续补充：

- 品牌官网公开页面。
- 电商商品公开页面。
- Open Food Facts 数据下载文件。
- 人工整理的真实广告素材。

## 3. 字段说明

单条广告素材结构如下：

```json
{
  "id": "ad_001",
  "sourceProductId": "1208224953",
  "title": "Apple Fitness",
  "brand": "Apple Inc.",
  "description": "App Store description, normalized for feed display.",
  "imageUrl": "https://...",
  "landingUrl": "https://apps.apple.com/...",
  "channel": "featured",
  "category": "Health & Fitness",
  "priceText": "Free",
  "materialType": "large_image",
  "rawTags": ["Health & Fitness", "Rating 2.9", "Free"],
  "rating": "2.9",
  "ratingCount": 10990,
  "releaseDate": "2017-04-10T23:30:13Z",
  "currentVersionReleaseDate": "2026-03-17T21:41:25Z",
  "source": {
    "provider": "Apple iTunes Search API",
    "providerUrl": "https://performance-partners.apple.com/search-api",
    "sourceUrl": "https://apps.apple.com/us/app/apple-fitness/id1208224953?uo=4",
    "retrievedAt": "2026-05-31"
  }
}
```

## 4. 字段来源口径

### 4.1 来自原始数据源的字段

|本项目字段|Apple API 字段|说明|
|---|---|---|
|`sourceProductId`|`trackId`|App Store 软件 id|
|`title`|`trackName`|软件名称|
|`brand`|`sellerName`|开发者或销售方名称|
|`description`|`description`|软件描述，已压缩空白并截断到适合卡片展示的长度|
|`imageUrl`|`artworkUrl512`|512px 图标|
|`landingUrl`|`trackViewUrl`|App Store 页面|
|`category`|`primaryGenreName`|主分类|
|`priceText`|`formattedPrice`|价格展示文案|
|`rating`|`averageUserRating`|平均评分，保留一位小数|
|`ratingCount`|`userRatingCount`|评分数量|
|`releaseDate`|`releaseDate`|首次发布时间|
|`currentVersionReleaseDate`|`currentVersionReleaseDate`|当前版本发布时间|

### 4.2 Demo 加工字段

|字段|说明|
|---|---|
|`id`|本项目内部广告 id，格式为 `ad_001`|
|`channel`|用于信息流频道切换，根据分类和顺序映射到 `featured`、`ecommerce`、`local`|
|`materialType`|用于卡片动态样式，按顺序分配 `large_image`、`small_image`、`video`|
|`rawTags`|由分类、评分、价格等基础信息组合而成，供 Step 4 AI enrich 参考|
|`source.retrievedAt`|本次整理日期|

这些加工字段不是 Apple 原始广告投放字段，而是为了训练营 Demo 的信息流、频道和多样式卡片能力准备的端侧展示字段。

## 5. 数据覆盖情况

当前数据覆盖：

- 数量：24 条。
- 频道：`featured`、`ecommerce`、`local`。
- 卡片类型：`large_image`、`small_image`、`video`。
- 分类：包含 Health & Fitness、Food & Drink、Shopping、Business 等。
- 每条数据包含图片 URL、详情链接、标题、品牌和描述。

## 6. 使用边界

当前数据仅用于训练营学习和 Demo 展示：

- 不代表真实广告投放库存。
- 不代表商业推荐排序结果。
- 不对外分发 Apple 或 App Store 内容。
- 不把 App Store 文案包装成自有版权内容。
- 后续 AI 生成摘要和标签时，需要保留来源链接，避免误导用户。

## 7. 后续计划

Step 4 将基于 `data/ads_raw.json` 调用真实 AI 接口，生成：

- `aiSummary`
- `aiTags`
- `audience`
- `sellingPoints`

输出文件：

```text
data/ads_ai_enriched.json
app/src/main/assets/ads_ai_enriched.json
```

Android 客户端后续优先读取 `ads_ai_enriched.json`，如果 enrich 数据不存在，再降级读取 `ads_raw.json`。
