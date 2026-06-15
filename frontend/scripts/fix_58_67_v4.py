#!/usr/bin/env python3
import json, re, subprocess
from pathlib import Path

UA = "Mozilla/5.0"
OUT = Path(__file__).resolve().parents[1] / "public" / "images" / "gears"


def curl(u):
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "25", u], capture_output=True).stdout


def sz(u):
    return int(subprocess.run(["curl", "-sL", "-A", UA, "-o", "NUL", "-w", "%{size_download}", u], capture_output=True, text=True).stdout or 0)


def save(gid, url):
    data = curl(url)
    if len(data) < 8000:
        return False
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    print(f"saved {gid} {len(data)} bytes")
    return True


raw = curl("https://www.campmor.com/search/suggest.json?q=arcteryx+beta")
ps = json.loads(raw)["resources"]["results"]["products"]
best58 = None
for p in ps:
    slug = p["url"].split("/products/")[-1].split("?")[0]
    html = curl(f"https://www.campmor.com/products/{slug}").decode("utf-8", errors="ignore")
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
        html,
    )
    imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
    s = sz(imgs[0]) if imgs else 0
    print(p["title"], s, slug)
    if imgs and s > 8000 and best58 is None:
        best58 = imgs[0]

if best58:
    save(58, best58)

# Rain cover: Osprey raincover from campmor as accessory proxy, or OR
raw = curl("https://www.campmor.com/search/suggest.json?q=osprey+raincover")
ps = json.loads(raw).get("resources", {}).get("results", {}).get("products", [])
for p in ps[:5]:
    slug = p["url"].split("/products/")[-1].split("?")[0]
    html = curl(f"https://www.campmor.com/products/{slug}").decode("utf-8", errors="ignore")
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
        html,
    )
    imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
    s = sz(imgs[0]) if imgs else 0
    print("rain", p["title"], s)
    if imgs and s > 8000 and save(67, imgs[0]):
        break

for gid in [58, 67]:
    f = OUT / f"{gid}-main.jpg"
    print("check", gid, f.stat().st_size if f.exists() else 0)
