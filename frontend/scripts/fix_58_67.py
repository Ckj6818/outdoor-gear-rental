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
        print(gid, "FAIL", len(data), url[:90])
        return
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    print(gid, "OK", len(data), url[:90])


# Arc'teryx Beta AR / Beta Jacket from campmor listing
html = curl("https://www.campmor.com/search?q=arcteryx+beta").decode("utf-8", errors="ignore")
slugs = re.findall(r"/products/(arcteryx[^\"']+)", html)
for slug in list(dict.fromkeys(slugs))[:10]:
    p = curl(f"https://www.campmor.com/products/{slug}").decode("utf-8", errors="ignore")
    title = re.search(r"<title>([^<]+)", p)
    t = title.group(1).lower() if title else slug
    if "beta" not in t:
        continue
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
        p,
    )
    imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
    if imgs and sz(imgs[0]) > 8000:
        print("candidate 58", t[:50], sz(imgs[0]))
        save(58, imgs[0])
        break

# Deuter rain cover direct URLs
for url in [
    "https://www.deuter.com/dw/image/v2/BDJM_PRD/on/demandware.static/-/Sites-deuter-master-catalog/default/dw3940021/images/large/3940021-70000_orchid_01.jpg",
    "https://www.deuter.com/dw/image/v2/BDJM_PRD/on/demandware.static/-/Sites-deuter-master-catalog/default/dw3940021/images/large/3940021-70000_black_01.jpg",
    "https://www.deuter.com/dw/image/v2/BDJM_PRD/on/demandware.static/-/Sites-deuter-master-catalog/default/dw3940021/images/large/3940021-70000_anthracite_01.jpg",
    "https://www.deuter.com/on/demandware.static/-/Sites-deuter-master-catalog/default/dw8a3940021/images/large/3940021-70000.jpg",
]:
    if sz(url) > 8000:
        save(67, url)
        break

# campmor deuter rain
html = curl("https://www.campmor.com/search?q=deuter+rain+cover").decode("utf-8", errors="ignore")
for slug in list(dict.fromkeys(re.findall(r"/products/([a-z0-9-]+)", html)))[:8]:
    if "deuter" not in slug and "rain" not in slug:
        continue
    p = curl(f"https://www.campmor.com/products/{slug}").decode("utf-8", errors="ignore")
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
        p,
    )
    imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
    if imgs and sz(imgs[0]) > 8000:
        print("candidate 67", slug, sz(imgs[0]))
        save(67, imgs[0])
        break
