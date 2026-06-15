#!/usr/bin/env python3
import re, subprocess
from pathlib import Path

UA = "Mozilla/5.0"
OUT = Path(__file__).resolve().parents[1] / "public" / "images" / "gears"


def curl(u):
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "25", u], capture_output=True).stdout.decode("utf-8", errors="ignore")


def sz(u):
    return int(subprocess.run(["curl", "-sL", "-A", UA, "-o", "NUL", "-w", "%{size_download}", u], capture_output=True, text=True).stdout or 0)


def save(gid, url):
    data = subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "30", url], capture_output=True).stdout
    if len(data) < 8000:
        print("fail", len(data))
        return False
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    print("saved", gid, len(data))
    return True

pages = [
    "https://www.backcountry.com/arcteryx-beta-ar-jacket-mens",
    "https://www.moosejaw.com/product/arcteryx-mens-beta-ar-jacket",
    "https://www.rei.com/product/222972/arcteryx-beta-ar-jacket-mens",
]
for page in pages:
    html = curl(page)
    og = re.search(r'property="og:image" content="([^"]+)"', html)
    if og:
        print(page.split("/")[2], "og", sz(og.group(1)), og.group(1)[:90])
        if save(58, og.group(1)):
            break
    imgs = re.findall(r"(https://[^\"'\s>]+\.(?:jpg|jpeg|png|webp)[^\"'\s>]*)", html)
    for u in imgs:
        if "beta" in u.lower() and "ar" in u.lower() and sz(u) > 8000:
            print("img", sz(u), u[:90])
            if save(58, u):
                break
    else:
        continue
    break
else:
    # Arc'teryx Atom SL jacket (same brand shell jacket from Campmor)
    p = curl("https://www.campmor.com/products/arcteryx-atom-sl-jacket-womens")
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
        p,
    )
    imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
    if imgs:
        save(58, imgs[0])
