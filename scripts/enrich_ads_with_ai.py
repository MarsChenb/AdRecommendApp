#!/usr/bin/env python3
"""Enrich raw ad records with AI summaries, tags, audiences, and selling points."""

from __future__ import annotations

import argparse
import json
import os
import shutil
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


DEFAULT_BASE_URL = "https://api.deepseek.com"
DEFAULT_MODEL = "deepseek-v4-flash"


def load_json(path: Path) -> list[dict[str, Any]]:
    with path.open("r", encoding="utf-8-sig") as file:
        data = json.load(file)
    if not isinstance(data, list):
        raise ValueError(f"Expected a JSON array in {path}")
    return data


def save_json(path: Path, data: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as file:
        json.dump(data, file, ensure_ascii=False, indent=2)
        file.write("\n")


def normalize_list(value: Any, max_items: int) -> list[str]:
    if not isinstance(value, list):
        return []
    result: list[str] = []
    for item in value:
        text = str(item).strip()
        if text and text not in result:
            result.append(text[:28])
        if len(result) >= max_items:
            break
    return result


def contains_cjk(text: str) -> bool:
    return any("\u4e00" <= char <= "\u9fff" for char in text)


def validate_ai_result(result: dict[str, Any]) -> dict[str, Any]:
    summary = str(result.get("aiSummary", "")).strip()
    if not summary:
        raise ValueError("AI result is missing aiSummary")
    if not contains_cjk(summary):
        raise ValueError("AI summary must be written in Chinese")

    tags = normalize_list(result.get("aiTags"), 6)
    audience = normalize_list(result.get("audience"), 4)
    selling_points = normalize_list(result.get("sellingPoints"), 5)

    if len(tags) < 2:
        raise ValueError("AI result must contain at least 2 aiTags")
    if len(audience) < 1:
        raise ValueError("AI result must contain at least 1 audience")
    if len(selling_points) < 1:
        raise ValueError("AI result must contain at least 1 sellingPoint")
    if not any(contains_cjk(item) for item in tags):
        raise ValueError("AI tags must include Chinese labels")

    return {
        "aiSummary": summary[:120],
        "aiTags": tags,
        "audience": audience,
        "sellingPoints": selling_points,
    }


def build_prompt(ad: dict[str, Any]) -> str:
    payload = {
        "title": ad.get("title", ""),
        "brand": ad.get("brand", ""),
        "description": ad.get("description", ""),
        "category": ad.get("category", ""),
        "priceText": ad.get("priceText", ""),
        "rawTags": ad.get("rawTags", []),
        "rating": ad.get("rating", ""),
        "ratingCount": ad.get("ratingCount", 0),
        "channel": ad.get("channel", ""),
    }
    return (
        "Generate ad feed metadata for this app promotion record.\n"
        "Return exactly one valid JSON object, without markdown.\n"
        "JSON schema:\n"
        "{\n"
        '  "aiSummary": "30-70 Chinese characters, user-facing ad summary",\n'
        '  "aiTags": ["3-6 short Chinese tags"],\n'
        '  "audience": ["1-4 target audience labels in Chinese"],\n'
        '  "sellingPoints": ["2-5 concise selling points in Chinese"]\n'
        "}\n"
        "Rules:\n"
        "- All user-facing string values must be Simplified Chinese.\n"
        "- English output is invalid unless it is a product or brand name.\n"
        "- Do not invent unsupported facts.\n"
        "- Use the provided title, category, rating, price, and description.\n"
        "- Tags should be useful for filtering an ad feed.\n"
        "- Keep all values concise.\n\n"
        f"Input ad JSON:\n{json.dumps(payload, ensure_ascii=False)}"
    )


def call_deepseek(
    *,
    api_key: str,
    base_url: str,
    model: str,
    ad: dict[str, Any],
    timeout_seconds: int,
    retries: int,
) -> dict[str, Any]:
    url = base_url.rstrip("/") + "/chat/completions"
    body = {
        "model": model,
        "thinking": {"type": "disabled"},
        "response_format": {"type": "json_object"},
        "temperature": 0.2,
        "max_tokens": 400,
        "messages": [
            {
                "role": "system",
                "content": (
                    "You generate structured advertising metadata. "
                    "You must output valid JSON only. "
                    "All user-facing fields must be written in Simplified Chinese."
                ),
            },
            {"role": "user", "content": build_prompt(ad)},
        ],
    }
    request_body = json.dumps(body).encode("utf-8")
    request = urllib.request.Request(
        url,
        data=request_body,
        method="POST",
        headers={
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
        },
    )

    last_error: Exception | None = None
    for attempt in range(retries + 1):
        try:
            with urllib.request.urlopen(request, timeout=timeout_seconds) as response:
                response_data = json.loads(response.read().decode("utf-8"))
            content = response_data["choices"][0]["message"]["content"]
            parsed = json.loads(content)
            return validate_ai_result(parsed)
        except (urllib.error.URLError, urllib.error.HTTPError, KeyError, json.JSONDecodeError, ValueError) as error:
            last_error = error
            if attempt >= retries:
                break
            time.sleep(1.5 * (attempt + 1))
    raise RuntimeError(f"DeepSeek enrichment failed for {ad.get('id')}: {last_error}")


def local_fallback(ad: dict[str, Any]) -> dict[str, Any]:
    category = str(ad.get("category", "应用")).strip() or "应用"
    price = str(ad.get("priceText", "免费")).strip() or "免费"
    rating = str(ad.get("rating", "")).strip()
    title = str(ad.get("title", "这款应用")).strip()
    brand = str(ad.get("brand", "开发者")).strip()

    tags = [category, price]
    for raw_tag in ad.get("rawTags", []):
        text = str(raw_tag).strip()
        if text and text not in tags:
            tags.append(text)
        if len(tags) >= 4:
            break

    rating_phrase = f"，评分 {rating}" if rating else ""
    return {
        "aiSummary": f"{title} 来自 {brand}，主打 {category} 场景{rating_phrase}，适合快速了解和试用。",
        "aiTags": tags[:6],
        "audience": ["移动用户", "效率探索者"],
        "sellingPoints": [f"{category} 场景", f"{price} 入门", "公开评分可参考"],
    }


def enrich_ads(args: argparse.Namespace) -> list[dict[str, Any]]:
    raw_ads = load_json(args.input)
    existing_by_id: dict[str, dict[str, Any]] = {}
    if args.output.exists() and not args.force:
        for item in load_json(args.output):
            item_id = item.get("id")
            if item_id:
                existing_by_id[str(item_id)] = item

    api_key = os.environ.get("DEEPSEEK_API_KEY", "").strip()
    use_fallback = not api_key
    if use_fallback and not args.allow_fallback:
        raise RuntimeError(
            "DEEPSEEK_API_KEY is not set. Set it or pass --allow-fallback for local rule output."
        )

    generated_at = datetime.now(timezone.utc).isoformat(timespec="seconds")
    enriched_ads: list[dict[str, Any]] = []
    selected_ads = raw_ads[: args.limit] if args.limit > 0 else raw_ads
    total = len(selected_ads)

    for index, ad in enumerate(selected_ads, start=1):
        ad_id = str(ad.get("id", ""))
        if ad_id in existing_by_id and not args.force:
            enriched_ads.append(existing_by_id[ad_id])
            print(f"[{index}/{total}] reused {ad_id}")
            continue

        if use_fallback:
            ai_result = local_fallback(ad)
            provider = "local_fallback"
            model = "rules"
        else:
            ai_result = call_deepseek(
                api_key=api_key,
                base_url=args.base_url,
                model=args.model,
                ad=ad,
                timeout_seconds=args.timeout,
                retries=args.retries,
            )
            provider = "deepseek"
            model = args.model

        enriched = {
            **ad,
            **ai_result,
            "aiProvider": provider,
            "aiModel": model,
            "aiGeneratedAt": generated_at,
        }
        enriched_ads.append(enriched)
        print(f"[{index}/{total}] enriched {ad_id} via {provider}")

        if args.sleep > 0:
            time.sleep(args.sleep)

    return enriched_ads


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", type=Path, default=Path("data/ads_raw.json"))
    parser.add_argument("--output", type=Path, default=Path("data/ads_ai_enriched.json"))
    parser.add_argument(
        "--asset-output",
        type=Path,
        default=Path("app/src/main/assets/ads_ai_enriched.json"),
    )
    parser.add_argument("--base-url", default=os.environ.get("DEEPSEEK_BASE_URL", DEFAULT_BASE_URL))
    parser.add_argument("--model", default=os.environ.get("DEEPSEEK_MODEL", DEFAULT_MODEL))
    parser.add_argument("--timeout", type=int, default=60)
    parser.add_argument("--retries", type=int, default=2)
    parser.add_argument("--sleep", type=float, default=0.2)
    parser.add_argument("--limit", type=int, default=0)
    parser.add_argument("--force", action="store_true")
    parser.add_argument("--allow-fallback", action="store_true")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    enriched_ads = enrich_ads(args)
    save_json(args.output, enriched_ads)
    args.asset_output.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(args.output, args.asset_output)
    print(f"Wrote {len(enriched_ads)} ads to {args.output}")
    print(f"Copied enriched data to {args.asset_output}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as error:
        print(f"error: {error}", file=sys.stderr)
        raise SystemExit(1)
