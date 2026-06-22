#!/usr/bin/env python3
"""Resolve campmor image URLs for batch3 gear inserts."""
import json
import re
import subprocess
from pathlib import Path

UA = "Mozilla/5.0"
# (id, search queries in priority order)
ITEMS: list[tuple[int, list[str]]] = [
    (71, ["columbia watertight ii jacket mens"]),
    (72, ["marmot fleece jacket mens", "marmot preon fleece"]),
    (73, ["la sportiva tx4", "la sportiva approach shoe"]),
    (74, ["goal zero nomad 10 solar panel"]),
    (75, ["camelbak classic 3l", "camelbak hydration pack"]),
    (76, ["western mountaineering ultralite", "feathered friends sleeping bag"]),
    (77, ["icebreaker merino 260", "smartwool base layer mens"]),
    (78, ["gregory maven 45", "gregory womens backpack 45"]),
    (79, ["osprey talon 22", "osprey daylite"]),
    (80, ["jetboil sumo cooking system", "jetboil minimo"]),
]


def curl(url: str) -> bytes:
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "25", url], capture_output=True).stdout


def product_images(slug: str) -> list[str]:
    html = curl(f"https://www.campmor.com/products/{slug.split('?')[0]}").decode("utf-8", "ignore")
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|jpeg|png|webp)[^\s\"'\\]*",
        html,
    )
    return [u for u in dict.fromkeys(imgs) if "extensions" not in u]


def search_products(query: str) -> list[dict]:
    raw = curl(f"https://www.campmor.com/search/suggest.json?q={query.replace(' ', '+')}")
    try:
        return json.loads(raw)["resources"]["results"]["products"]
    except (json.JSONDecodeError, KeyError, TypeError):
        return []


def resolve(gid: int, queries: list[str]) -> dict | None:
    for query in queries:
        for product in search_products(query)[:3]:
            slug = product["url"].split("/products/")[-1]
            imgs = product_images(slug)
            if imgs:
                return {
                    "id": gid,
                    "title": product["title"],
                    "main": imgs[0],
                    "hover": imgs[1] if len(imgs) > 1 else imgs[0],
                }
    return None


def main() -> None:
    out: dict[str, dict] = {}
    for gid, queries in ITEMS:
        hit = resolve(gid, queries)
        if hit:
            out[str(gid)] = hit
            print(gid, hit["title"][:55])
        else:
            print(gid, "FAIL", queries[0])

    Path(__file__).with_name("batch3_urls.json").write_text(
        json.dumps(out, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )


if __name__ == "__main__":
    main()
