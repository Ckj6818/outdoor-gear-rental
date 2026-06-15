#!/usr/bin/env python3
import json
import re
import subprocess
from pathlib import Path

UA = "Mozilla/5.0"
OUT = Path(__file__).resolve().parents[1] / "public" / "images" / "gears"
PAGES = {
    5: "https://www.mysteryranchuk.com/products/radix-31",
    19: "https://www.deuter.com/en-de/backpacks-and-bags/backpacks/hiking-backpacks/aircontact-lite-50-10-sl-3200021-70000",
    24: "https://www.marmot.com/men/trestles-30-sleeping-bag",
    26: "https://www.osprey.com/gb/osprey-daylite-plus-26",
    53: "https://www.petzl.com/US/en/Sport/Corax-Harness/C38A",
    58: "https://arcteryx.com/us/en/shop/mens/beta-ar-jacket",
    60: "https://www.leki.com/products/trail-al-poles",
    67: "https://www.deuter.com/en-de/accessories/rain-covers/rain-cover-iii-3940021-70000",
    70: "https://www.osprey.com/gb/osprey-ultralight-45",
}


def curl(url: str) -> bytes:
    return subprocess.run(
        ["curl", "-sL", "-A", UA, "--max-time", "40", url],
        capture_output=True,
    ).stdout or b""


def extract(html: str) -> list[str]:
    patterns = [
        r"https?://[^\"'\s>]+\.(?:jpg|jpeg|png|webp)[^\"'\s>]*",
        r"//cdn\.shopify\.com[^\"'\s>]+\.(?:jpg|jpeg|png|webp)[^\"'\s>]*",
        r"https://www\.osprey\.com[^\"'\s>]+\.(?:jpg|jpeg|png|webp)[^\"'\s>]*",
        r"https://images\.arcteryx\.com[^\"'\s>]+\.(?:jpg|jpeg|png|webp)[^\"'\s>]*",
        r"https://www\.petzl\.com[^\"'\s>]+\.(?:jpg|jpeg|png|webp)[^\"'\s>]*",
    ]
    found = []
    for p in patterns:
        for m in re.findall(p, html, re.I):
            u = m if m.startswith("http") else "https:" + m
            bad = ("icon", "logo", "banner", "sprite", "nav-", "social", "careers")
            if not any(b in u.lower() for b in bad):
                found.append(u)
    out, seen = [], set()
    for u in found:
        if u not in seen:
            seen.add(u)
            out.append(u)
    return out


def main():
    result = {}
    for gid, page in PAGES.items():
        html = curl(page).decode("utf-8", errors="ignore")
        imgs = extract(html)
        picked = None
        for u in imgs:
            data = curl(u)
            if len(data) >= 8000:
                picked = u
                (OUT / f"{gid}-main.jpg").write_bytes(data)
                (OUT / f"{gid}-hover.jpg").write_bytes(data)
                print(f"[OK] {gid} {len(data)} {u[:90]}")
                result[gid] = u
                break
        if not picked:
            print(f"[FAIL] {gid} candidates={len(imgs)}")
            if imgs:
                print("  first:", imgs[0][:90])
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()
