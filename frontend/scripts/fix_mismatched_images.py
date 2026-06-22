#!/usr/bin/env python3
"""Fix gear images where local files were duplicated across wrong product IDs."""

from __future__ import annotations

import json
import re
import subprocess
import sys
from pathlib import Path

UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36"
ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "public" / "images" / "gears"
MIN = 8000

# Verified or campmor-resolved URLs (main, hover optional)
FIXES: dict[int, dict[str, list[str]]] = {
    68: {
        "main": [
            "https://www.patagonia.com/dw/image/v2/BDJB_PRD/on/demandware.static/-/Sites-patagonia-master/default/dw84212_BLK/images/hi-res/84212_BLK.jpg",
        ],
        "hover": [
            "https://www.patagonia.com/dw/image/v2/BDJB_PRD/on/demandware.static/-/Sites-patagonia-master/default/dw84212_BLK/images/hi-res/84212_BLK_alt1.jpg",
        ],
    },
    58: {
        "main": [
            "https://images-dynamic-arcteryx.imgix.net/pdp/beta-ar-jacket/Mens/Black/Beta-AR-Jacket-Black-Front-View.jpg?w=960&q=75&auto=format",
            "https://images-dynamic-arcteryx.imgix.net/details/960x960/beta-ar-jacket/Mens/Black/Beta-AR-Jacket-Black-Front-View.jpg?w=960",
        ],
        "hover": [],
    },
    59: {
        "main": [
            "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/27538700013.jpg?v=1747233436",
        ],
        "hover": [],
    },
    2: {
        "main": [
            "https://www.mysteryranchuk.com/cdn/shop/files/Terraframe-2065-20112383_black-10_jpg.jpg?v=1723468112",
        ],
        "hover": [
            "https://www.mysteryranchuk.com/cdn/shop/files/Terraframe-2065-20112383_black-_Body-20Panel_-1010_jpg.jpg?v=1723468112",
        ],
    },
    4: {
        "main": [
            "https://www.osprey.com/media/catalog/product/cache/bdd72cdc4bced30f3cf62267d5fe3824/e/x/exos58_s22_side_tungstengrey.jpg",
        ],
        "hover": [
            "https://www.osprey.com/gb/media/catalog/product/_/0/_0002_exos58_s22_sideback_tungstengrey_10004019_web_2.jpg",
        ],
    },
    5: {
        "main": [
            "https://www.mysteryranchuk.com/cdn/shop/files/Radix-31-11027-Black-001.jpg?v=1723468112",
        ],
        "hover": [],
    },
    12: {
        "main": [
            "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_ac8f45f7-a270-4aa2-89b1-bded70a9be24.jpg?v=1747239607",
        ],
        "hover": [],
    },
}

CAMPmor_QUERIES = {
    54: "black diamond storm 400",
    58: "arcteryx beta ar jacket mens",
    63: "black diamond gym chalk bag",
    68: "patagonia nano puff jacket",
    53: "petzl corax harness",
    69: "nemo hornet elite 1p",
    70: "osprey ultralight 45",
    67: "deuter rain cover iii",
}


def curl(url: str, referer: str | None = None) -> bytes:
    cmd = ["curl", "-sL", "-A", UA, "--max-time", "35", url]
    if referer:
        cmd.extend(["-e", referer])
    return subprocess.run(cmd, capture_output=True).stdout or b""


def campmor_images(query: str) -> list[str]:
    raw = curl(f"https://www.campmor.com/search/suggest.json?q={query.replace(' ', '+')}")
    try:
        products = json.loads(raw)["resources"]["results"]["products"]
    except (json.JSONDecodeError, KeyError, TypeError):
        return []
    for product in products[:3]:
        slug = product["url"].split("/products/")[-1]
        html = curl(f"https://www.campmor.com/products/{slug}").decode("utf-8", errors="ignore")
        imgs = re.findall(
            r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|jpeg|png|webp)[^\s\"'\\]*",
            html,
        )
        imgs = [u for u in dict.fromkeys(imgs) if "extensions" not in u]
        if imgs:
            return imgs
    return []


def save(data: bytes, path: Path) -> bool:
    if len(data) < MIN:
        return False
    path.write_bytes(data)
    return True


def try_urls(urls: list[str], dest: Path, referer: str = "https://www.campmor.com/") -> str | None:
    for url in urls:
        if not url:
            continue
        if save(curl(url, referer), dest):
            return url
    return None


def resolve_urls(gid: int, cfg: dict[str, list[str]]) -> tuple[list[str], list[str]]:
    main = list(cfg.get("main", []))
    hover = list(cfg.get("hover", []))
    if query := CAMPmor_QUERIES.get(gid):
        found = campmor_images(query)
        if found:
            main = found[:1] + main
            hover = found[1:2] + hover
    return main, hover


def download_gid(gid: int) -> dict | None:
    cfg = FIXES.get(gid, {"main": [], "hover": []})
    main_urls, hover_urls = resolve_urls(gid, cfg)

    main_dest = OUT / f"{gid}-main.jpg"
    hover_dest = OUT / f"{gid}-hover.jpg"

    main_url = try_urls(main_urls, main_dest)
    if not main_url:
        print(f"[FAIL] {gid} main", file=sys.stderr)
        return None

    hover_url = try_urls(hover_urls, hover_dest) if hover_urls else None
    if not hover_url:
        hover_dest.write_bytes(main_dest.read_bytes())
        hover_url = main_url

    print(f"[OK] {gid} main={main_url[:80]}")
    return {"id": gid, "main": main_url, "hover": hover_url}


def main() -> int:
    targets = sorted(set(FIXES) | set(CAMPmor_QUERIES))
    resolved: dict[str, dict] = {}
    ok = 0
    for gid in targets:
        result = download_gid(gid)
        if result:
            resolved[str(gid)] = {"main": [result["main"]], "hover": [result["hover"]]}
            ok += 1

    report_path = OUT / "fix-mismatch-report.json"
    report_path.write_text(json.dumps(resolved, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"\nFixed {ok}/{len(targets)} -> {report_path}")
    return 0 if ok == len(targets) else 1


if __name__ == "__main__":
    raise SystemExit(main())
