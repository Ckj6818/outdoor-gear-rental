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
        print("fail", gid, len(data), url[:80])
        return False
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    print("ok", gid, len(data))
    return True

raw = subprocess.run(["curl", "-sL", "-A", UA, "https://www.campmor.com/search/suggest.json?q=arcteryx+beta"], capture_output=True).stdout
ps = json.loads(raw)["resources"]["results"]["products"]
for p in ps:
    if "beta ar" in p["title"].lower() or ("beta" in p["title"].lower() and "jacket" in p["title"].lower() and "men" in p["title"].lower()):
        slug = p["url"].split("/products/")[-1]
        html = curl(f"https://www.campmor.com/products/{slug}").decode("utf-8", errors="ignore")
        imgs = re.findall(
            r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
            html,
        )
        imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
        print(p["title"], sz(imgs[0]) if imgs else 0)
        if imgs and save(58, imgs[0]):
            break

# 67 use deuter rain cover from EU shop - try static image from product JSON-LD
html = curl("https://www.deuter.com/en-de/accessories/rain-covers/rain-cover-iii-3940021-70000").decode("utf-8", errors="ignore")
for u in re.findall(r'"(https://[^"]+3940021[^"]+\.(?:jpg|png|webp)[^"]*)"', html):
    if save(67, u):
        break
else:
    # REI-style: use osprey raincover or generic from campmor outdoor research
    raw = subprocess.run(["curl", "-sL", "-A", UA, "https://www.campmor.com/search/suggest.json?q=rain+cover+backpack"], capture_output=True).stdout
    ps = json.loads(raw).get("resources", {}).get("results", {}).get("products", [])
    for p in ps[:8]:
        slug = p["url"].split("/products/")[-1]
        html = curl(f"https://www.campmor.com/products/{slug}").decode("utf-8", errors="ignore")
        imgs = re.findall(
            r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
            html,
        )
        imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
        title = p["title"].lower()
        if imgs and sz(imgs[0]) > 8000 and ("rain" in title or "cover" in title) and "trainer" not in title and "bike" not in title:
            print("67 pick", p["title"])
            save(67, imgs[0])
            break
