#!/usr/bin/env python3
"""Re-download gear images that are HTML error pages or too small."""

from __future__ import annotations

import json
import re
import sys
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "public" / "images" / "gears"
MANIFEST = Path(__file__).resolve().parent / "gear_image_manifest.json"
UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36"
MIN = 8000

# Dead URLs replaced with verified alternatives
OVERRIDES: dict[int, dict[str, list[str]]] = {
    28: {
        "main": [
            "https://cdn11.bigcommerce.com/s-jmhyu0/images/stencil/1280x1280/products/388/3035/Sawyer-Squeeze-Water-Filtration-System__68831.1757870500.jpg?c=2"
        ],
        "hover": [
            "https://cdn.prod.website-files.com/61549f9352f3558157a226ea/698b4622f92f548340c80d0b_SP304-Sawyer-CNOC-Micro-Squeeze-Water-Filtration-System-Main-Image.png"
        ],
    },
    62: {
        "main": [
            "https://cdn11.bigcommerce.com/s-jmhyu0/images/stencil/1280x1280/products/388/3035/Sawyer-Squeeze-Water-Filtration-System__68831.1757870500.jpg?c=2"
        ],
        "hover": [
            "https://cdn.prod.website-files.com/61549f9352f3558157a226ea/698b4622f92f548340c80d0b_SP304-Sawyer-CNOC-Micro-Squeeze-Water-Filtration-System-Main-Image.png"
        ],
    },
    6: {
        "main": ["https://cascadedesigns.com/cdn/shop/files/10316_msr_hubbahubba_nx_fly.png?v=1734452712"],
        "hover": ["https://cascadedesigns.com/cdn/shop/files/10316_msr_hubbahubba_nx_packed.png?v=1734452712"],
    },
    2: {
        "main": ["https://www.mysteryranch.com/cdn/shop/files/Terraframe-2065-20112383_black-10_jpg.jpg?v=1723468112"],
        "hover": [
            "https://www.mysteryranch.com/cdn/shop/files/Terraframe-2065-20112383_black-_Body-20Panel_-1010_jpg.jpg?v=1723468112"
        ],
    },
    5: {
        "main": ["https://www.mysteryranch.com/cdn/shop/files/Radix-31-11027-Black-001.jpg?v=1723468112"],
    },
    27: {
        "main": [
            "https://www.patagonia.com/dw/image/v2/BDJB_PRD/on/demandware.static/-/Sites-patagonia-master/default/dw85240_BLK/images/hi-res/85240_BLK.jpg"
        ],
        "hover": [
            "https://www.patagonia.com/dw/image/v2/BDJB_PRD/on/demandware.static/-/Sites-patagonia-master/default/dw85240_BLK/images/hi-res/85240_BLK_alt1.jpg"
        ],
    },
    63: {
        "main": [
            "https://cdn.shopify.com/s/files/1/0301/4023/5913/products/C052AA004.jpg?v=1747224600"
        ],
    },
}

# When remote sources fail, reuse a valid local image from a similar gear category
LOCAL_COPY: dict[int, int] = {
    2: 4,
    5: 12,
    27: 59,
    68: 58,
}

CAMPmor = {
    2: "mystery-ranch-terraframe-65-backpack",
    5: "mystery-ranch-radix-31-backpack",
    27: "patagonia-mens-torrentshell-3l-jacket",
    63: "black-diamond-gym-sack-chalk-bag",
    65: "rab-neutrino-800-sleeping-bag",
    68: "patagonia-mens-nano-puff-jacket",
}


def fetch(url: str, referer: str | None = None) -> bytes:
    headers = {"User-Agent": UA}
    if referer:
        headers["Referer"] = referer
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=35) as resp:
        return resp.read()


def is_valid_image(data: bytes) -> bool:
    if len(data) < MIN:
        return False
    head = data[:32].lstrip()
    if head.startswith(b"<!") or head.startswith(b"<html") or head.startswith(b"<HTML"):
        return False
    return True


def is_corrupt_file(path: Path) -> bool:
    if not path.exists():
        return True
    return not is_valid_image(path.read_bytes())


def campmor_images(slug: str) -> list[str]:
    try:
        html = fetch(f"https://www.campmor.com/products/{slug}", "https://www.campmor.com/").decode(
            "utf-8", errors="ignore"
        )
    except Exception:
        return []
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
        html,
    )
    return [i for i in imgs if "extensions" not in i]


def cascade_images(slug: str) -> list[str]:
    raw = fetch(f"https://cascadedesigns.com/products/{slug}.json")
    data = json.loads(raw)
    return [img["src"] for img in data["product"]["images"]]


def urls_for(gid: int, kind: str) -> list[str]:
    urls: list[str] = []
    if gid in OVERRIDES:
        urls.extend(OVERRIDES[gid].get(kind, []))
    if slug := CAMPmor.get(gid):
        imgs = campmor_images(slug)
        if kind == "main":
            urls.extend(imgs[:1])
        else:
            urls.extend(imgs[1:2])
    if gid == 6 and kind == "main" and not urls:
        urls.extend(cascade_images("hubba-hubba-nx-2-backpacking-tent")[:1])
    if gid == 6 and kind == "hover" and not urls:
        urls.extend(cascade_images("hubba-hubba-nx-2-backpacking-tent")[1:2])

    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    urls.extend(manifest.get(str(gid), {}).get(kind, []))

    seen: set[str] = set()
    out: list[str] = []
    for u in urls:
        if u and u not in seen:
            seen.add(u)
            out.append(u)
    return out


def download_one(gid: int, kind: str) -> bool:
    dest = OUT / f"{gid}-{kind}.jpg"
    for url in urls_for(gid, kind):
        try:
            data = fetch(url, referer="https://www.campmor.com/")
            if is_valid_image(data):
                dest.write_bytes(data)
                print(f"[OK] {gid}-{kind} ({len(data)} bytes) <- {url[:80]}")
                return True
        except Exception as exc:
            print(f"[TRY] {gid}-{kind} failed: {exc}", file=sys.stderr)
    main_path = OUT / f"{gid}-main.jpg"
    if kind == "hover" and main_path.exists() and is_valid_image(main_path.read_bytes()):
        dest.write_bytes(main_path.read_bytes())
        print(f"[COPY] {gid}-hover <- {gid}-main")
        return True
    if source_id := LOCAL_COPY.get(gid):
        source = OUT / f"{source_id}-main.jpg"
        if source.exists() and is_valid_image(source.read_bytes()):
            dest.write_bytes(source.read_bytes())
            print(f"[LOCAL] {gid}-{kind} <- {source_id}-main")
            return True
    print(f"[FAIL] {gid}-{kind}", file=sys.stderr)
    return False


def main() -> int:
    fixed = failed = 0
    for path in sorted(OUT.glob("*-main.jpg")):
        stem = path.name.split("-")[0]
        if not stem.isdigit():
            continue
        gid = int(stem)
        for kind in ("main", "hover"):
            dest = OUT / f"{gid}-{kind}.jpg"
            if is_corrupt_file(dest):
                if download_one(gid, kind):
                    fixed += 1
                else:
                    failed += 1
    print(f"\nFixed: {fixed}, still failing: {failed}")
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
