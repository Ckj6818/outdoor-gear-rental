#!/usr/bin/env python3
"""按 fix_gear_urls.json 重新下载指定商品图；若失败则从官方商品页提取 og:image。"""

from __future__ import annotations

import json
import re
import subprocess
import sys
from pathlib import Path
from urllib.parse import urljoin

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "public" / "images" / "gears"
FIX = Path(__file__).resolve().parent / "fix_gear_urls.json"
PAGES = Path(__file__).resolve().parent / "download_gear_images.py"
UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36"
MIN = 8000

# 从 download_gear_images 复用商品页
import importlib.util

spec = importlib.util.spec_from_file_location("dl", PAGES)
dl = importlib.util.module_from_spec(spec)
spec.loader.exec_module(dl)


def curl(url: str, referer: str | None = None) -> bytes:
    cmd = ["curl", "-sL", "-A", UA, "--max-time", "40", url]
    if referer:
        cmd.extend(["-e", referer])
    return subprocess.run(cmd, capture_output=True).stdout or b""


def save(data: bytes, path: Path) -> bool:
    if len(data) < MIN:
        return False
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)
    return True


def try_urls(urls: list[str], dest: Path, referer: str | None = None) -> str | None:
    for u in urls:
        if not u:
            continue
        if save(curl(u, referer), dest):
            return u
    return None


def main() -> int:
    fixes = json.loads(FIX.read_text(encoding="utf-8"))
    ok, fail = 0, 0

    for sid, urls in fixes.items():
        gid = int(sid)
        page = dl.PRODUCT_PAGES.get(gid)
        extra: list[str] = []
        if page:
            html = curl(page, page).decode("utf-8", errors="ignore")
            extra = dl.extract_images_from_html(html, page)

        main_dest = OUT / f"{gid}-main.jpg"
        hover_dest = OUT / f"{gid}-hover.jpg"

        main_raw = urls.get("main", [])
        hover_raw = urls.get("hover", [])
        main_list = (main_raw if isinstance(main_raw, list) else [main_raw]) + extra
        hover_list = (hover_raw if isinstance(hover_raw, list) else [hover_raw]) + extra[1:2]

        m = try_urls(main_list, main_dest, page)
        h = try_urls(hover_list, hover_dest, page)

        if not h and m and main_dest.exists():
            hover_dest.write_bytes(main_dest.read_bytes())
            h = m + " (copy)"

        if m:
            ok += 1
            print(f"[OK] {gid} main={m[:70]}...")
        else:
            fail += 1
            print(f"[FAIL] {gid}", file=sys.stderr)

    print(f"Fixed: {ok} ok, {fail} fail")
    return 0 if fail == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
