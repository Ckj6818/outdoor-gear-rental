#!/usr/bin/env python3
import json, re, subprocess
from pathlib import Path

UA = "Mozilla/5.0"
OUT = Path(__file__).resolve().parents[1] / "public" / "images" / "gears"


def curl(u, t=20):
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", str(t), u], capture_output=True).stdout


def save(gid, url):
    data = curl(url)
    if len(data) < 8000:
        return False
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    print(f"saved {gid} {len(data)}")
    return True

# Deuter Raincover III
html = curl("https://www.deuter.com/se-en/raincover-iii/4046051162188").decode("utf-8", errors="ignore")
og = re.search(r'property="og:image" content="([^"]+)"', html)
if og:
    save(67, og.group(1))

# Arc'teryx Beta SL from campmor (closest hardshell in catalog)
raw = curl("https://www.campmor.com/search/suggest.json?q=arcteryx+beta+sl")
ps = json.loads(raw)["resources"]["results"]["products"]
for p in ps:
    if "men" in p["title"].lower() and "beta" in p["title"].lower():
        slug = p["url"].split("/products/")[-1].split("?")[0]
        html = curl(f"https://www.campmor.com/products/{slug}").decode("utf-8", errors="ignore")
        imgs = re.findall(
            r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
            html,
        )
        imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
        if imgs and save(58, imgs[0]):
            print(p["title"])
            break

for gid in [58, 67]:
    f = OUT / f"{gid}-main.jpg"
    print("check", gid, f.stat().st_size if f.exists() else 0)
