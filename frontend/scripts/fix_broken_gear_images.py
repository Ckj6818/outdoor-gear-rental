#!/usr/bin/env python3
"""Re-download broken gear images (403/error page saved as jpg)."""
import json
import re
import subprocess
from pathlib import Path

UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0"
OUT = Path(__file__).resolve().parents[1] / "public" / "images" / "gears"
MIN = 15000

# id -> URL list (campmor shopify CDN preferred)
FIXES = {
    2: [
        "https://www.mysteryranch.com/cdn/shop/files/Terraframe-2065-20112383_black-10_jpg.jpg?v=1723468112",
        "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_884fe15f-4ec5-4348-b0e5-ee4c64790fb9.jpg?v=1747164527",
    ],
    5: [
        "https://www.mysteryranchuk.com/cdn/shop/files/Radix-31-11027-Black-001.jpg?v=1723468112",
        "https://www.mysteryranch.com/cdn/shop/files/Radix-31-11027-Black-001.jpg?v=1723468112",
    ],
}


def curl(url, referer=None):
    cmd = ["curl", "-sL", "-A", UA, "--max-time", "35", url]
    if referer:
        cmd.extend(["-e", referer])
    return subprocess.run(cmd, capture_output=True).stdout or b""


def campmor_search(q):
    raw = curl(f"https://www.campmor.com/search/suggest.json?q={q.replace(' ', '+')}")
    try:
        return json.loads(raw)["resources"]["results"]["products"]
    except Exception:
        return []


def campmor_img(slug):
    html = curl(f"https://www.campmor.com/products/{slug.split('?')[0]}").decode("utf-8", "ignore")
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
        html,
    )
    return [u for u in dict.fromkeys(imgs) if "extensions" not in u]


def save(gid, data):
    if len(data) < MIN:
        return False
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    print(f"OK {gid} {len(data)}")
    return True


def fix_id(gid, urls=None):
    urls = list(urls or [])
    queries = {
        2: "mystery ranch terraframe 65",
        5: "mystery ranch radix 31",
    }
    if q := queries.get(gid):
        for p in campmor_search(q)[:2]:
            slug = p["url"].split("/products/")[-1]
            urls.extend(campmor_img(slug)[:2])
    for u in dict.fromkeys(urls):
        if save(gid, curl(u, "https://www.campmor.com/")):
            return True
    print(f"FAIL {gid}", flush=True)
    return False


if __name__ == "__main__":
    for gid, urls in FIXES.items():
        main = OUT / f"{gid}-main.jpg"
        if main.exists() and main.stat().st_size >= MIN:
            print(f"SKIP {gid} already {main.stat().st_size}")
            continue
        fix_id(gid, urls)
