#!/usr/bin/env python3
import re, subprocess
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
        print("fail", gid, len(data))
        return False
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    print("ok", gid, len(data), url[:95])
    return True

# Deuter rain cover III - scrape all deuter CDN images from product page
html = curl("https://www.deuter.com/en-de/accessories/rain-covers/rain-cover-iii-3940021-70000").decode("utf-8", errors="ignore")
urls = re.findall(r"(https://www\.deuter\.com/dw/image/v2/BDJM_PRD/[^\"'\s>]+)", html)
urls += re.findall(r"(//www\.deuter\.com/dw/image/v2/BDJM_PRD/[^\"'\s>]+)", html)
for u in dict.fromkeys(urls):
    if u.startswith("//"):
        u = "https:" + u
    s = sz(u)
    print("deuter", s, u[:100])
    if s > 8000 and ("3940021" in u or "rain" in u.lower() or "cover" in u.lower() or "large" in u):
        save(67, u)
        break

# Arc'teryx Beta AR via campmor product pages from search HTML
search = curl("https://www.campmor.com/search?q=arcteryx+beta+ar").decode("utf-8", errors="ignore")
for slug in list(dict.fromkeys(re.findall(r"/products/(arcteryx[a-z0-9-]+)", search))):
    p = curl(f"https://www.campmor.com/products/{slug}").decode("utf-8", errors="ignore")
    if "beta" not in p.lower()[:2000]:
        continue
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
        p,
    )
    imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
    if imgs and sz(imgs[0]) > 8000:
        print("beta slug", slug, sz(imgs[0]))
        save(58, imgs[0])
        break

# fallback beta jacket image
if not (OUT / "58-main.jpg").exists() or (OUT / "58-main.jpg").stat().st_size < 8000:
    save(58, "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_4372afdb-2fd5-4ebb-b53e-ac6d3f8e0f0a.jpg?v=1747164527")
