#!/usr/bin/env python3
"""Fix all gear main images below size threshold using download-report + campmor."""
import json
import re
import subprocess
from pathlib import Path

UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0"
ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "public" / "images" / "gears"
REPORT = OUT / "download-report.json"
MIN = 15000


def curl(url, referer=None):
    cmd = ["curl", "-sL", "-A", UA, "--max-time", "35", url]
    if referer:
        cmd.extend(["-e", referer])
    return subprocess.run(cmd, capture_output=True).stdout or b""


def save(gid, data):
    if len(data) < MIN:
        return False
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    return True


def campmor_search(q):
    raw = curl(f"https://www.campmor.com/search/suggest.json?q={q.replace(' ', '+')}")
    try:
        return json.loads(raw)["resources"]["results"]["products"]
    except Exception:
        return []


def campmor_imgs(slug):
    html = curl(f"https://www.campmor.com/products/{slug.split('?')[0]}").decode("utf-8", "ignore")
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
        html,
    )
    return [u for u in dict.fromkeys(imgs) if "extensions" not in u]


def fix_from_urls(gid, urls):
    for u in urls:
        data = curl(u, "https://www.campmor.com/")
        if save(gid, data):
            print(f"OK {gid} {len(data)} {u[:85]}")
            return True
    return False


EXTRA_QUERIES = {
    5: ["mystery ranch radix 31", "mystery ranch backpack"],
    29: ["jetboil minimo carbon", "jetboil flash"],
}


def main():
    report = json.loads(REPORT.read_text(encoding="utf-8"))
    url_map = {item["id"]: item["main"] for item in report.get("ok", [])}

    broken = []
    for f in OUT.glob("*-main.jpg"):
        prefix = f.name.split("-")[0]
        if not prefix.isdigit():
            continue
        gid = int(prefix)
        if f.stat().st_size < MIN:
            broken.append(gid)

    for gid in sorted(set(broken)):
        urls = [url_map.get(gid, "")]
        urls = [u for u in urls if u and "(copy)" not in u]
        if fix_from_urls(gid, urls):
            continue
        for q in EXTRA_QUERIES.get(gid, []):
            for p in campmor_search(q)[:3]:
                slug = p["url"].split("/products/")[-1]
                if fix_from_urls(gid, campmor_imgs(slug)):
                    break
            else:
                continue
            break
        else:
            print(f"FAIL {gid}")

    still = [f.name for f in OUT.glob("*-main.jpg") if f.stat().st_size < MIN]
    print("still broken:", still or "none")


if __name__ == "__main__":
    main()
