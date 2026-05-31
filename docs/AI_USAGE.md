# AI 使用说明

## 1. 当前 AI 接入方式

Step 4 使用 DeepSeek Chat Completions API 对 `data/ads_raw.json` 中的广告素材进行离线预处理，生成广告摘要、智能标签、目标人群和卖点。

输入：

```text
data/ads_raw.json
```

输出：

```text
data/ads_ai_enriched.json
app/src/main/assets/ads_ai_enriched.json
```

Android 客户端后续优先读取 `ads_ai_enriched.json`。如果 enriched 文件不存在或解析失败，再降级读取 raw 数据。

## 2. 使用的模型接口

当前默认配置：

```text
Base URL: https://api.deepseek.com
Endpoint: /chat/completions
Model: deepseek-v4-flash
Thinking: disabled
Response format: json_object
```

选择原因：

- DeepSeek API 兼容 OpenAI Chat Completions 格式。
- `deepseek-v4-flash` 适合批量结构化生成，成本和速度更适合数据预处理。
- 使用 JSON 输出，便于脚本校验和 Android 端解析。
- 离线预处理避免在信息流滑动过程中频繁请求大模型。

## 3. API Key 管理

API key 只能通过环境变量传入：

```powershell
$env:DEEPSEEK_API_KEY="你的 API key"
```

禁止：

- 将 API key 写入代码。
- 将 API key 写入 README 或文档。
- 将 API key 提交到 Git。
- 在日志中打印 API key。

## 4. 运行脚本

真实 DeepSeek 生成：

```powershell
$env:DEEPSEEK_API_KEY="你的 API key"
python scripts/enrich_ads_with_ai.py --force
```

只测试前 3 条：

```powershell
$env:DEEPSEEK_API_KEY="你的 API key"
python scripts/enrich_ads_with_ai.py --limit 3 --force
```

无 API key 时生成本地规则降级数据：

```powershell
python scripts/enrich_ads_with_ai.py --allow-fallback --force
```

## 5. AI 输出字段

每条广告会新增：

```json
{
  "aiSummary": "广告摘要",
  "aiTags": ["标签1", "标签2", "标签3"],
  "audience": ["目标人群1", "目标人群2"],
  "sellingPoints": ["卖点1", "卖点2"],
  "aiProvider": "deepseek",
  "aiModel": "deepseek-v4-flash",
  "aiGeneratedAt": "2026-05-31T..."
}
```

## 6. 输出约束

脚本会要求模型输出：

- 一个合法 JSON object。
- `aiSummary`：30-70 个中文字符左右。
- `aiTags`：3-6 个短标签。
- `audience`：1-4 个目标人群。
- `sellingPoints`：2-5 个核心卖点。

脚本会校验：

- `aiSummary` 非空。
- 至少 2 个 `aiTags`。
- 至少 1 个 `audience`。
- 至少 1 个 `sellingPoints`。
- 输出能被 JSON 解析。

## 7. 缓存和复用

如果 `data/ads_ai_enriched.json` 已存在，默认会复用同 id 的已生成结果，避免重复调用 API。

需要重新生成时使用：

```powershell
python scripts/enrich_ads_with_ai.py --force
```

## 8. 降级策略

真实 AI 是主路径。本地规则生成只作为兜底：

- 没有 API key 时用于继续开发 Android 客户端。
- DeepSeek 服务不可用时用于保底演示。
- 最终答辩时需要说明哪些数据来自真实 AI，哪些来自降级规则。

## 9. 人工验证方式

生成后至少人工抽查 5 条：

- 摘要是否符合原始广告描述。
- 标签是否适合搜索和过滤。
- 目标人群是否没有明显臆造。
- 卖点是否来自标题、分类、描述、评分或价格。

同时运行 JSON 校验，确认字段完整。
