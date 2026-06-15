#!/usr/bin/env python3
import json, re, subprocess
UA = "Mozilla/5.0"

def curl(u):
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "20", u], capture_output=True).stdout.decode("utf-8", errors="ignore")

def sz(u):
    return int(subprocess.run(["curl", "-sL", "-A", UA, "-o", "NUL", "-w", "%{size_download}", u], capture_output=True, text=True).stdout or 0)

for q in ["deuter rain cover", "deuter cover", "backpack rain cover", "arcteryx beta ar jacket", "arcteryx beta ar mens"]:
    raw = subprocess.run(["curl", "-sL", "-A", UA, f"https://www.campmor.com/search/suggest.json?q={q.replace(' ','+')}"], capture_output=True).stdout
    try:
        ps = json.loads(raw)["resources"]["results"]["products"]
    except Exception:
        ps = []
    print("===", q, len(ps))
    for p in ps[:6]:
        slug = p["url"].split("/products/")[-1]
        html = curl(f"https://www.campmor.com/products/{slug}")
        imgs = re.findall(r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*", html)
        imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
        print(p["title"][:60], slug, sz(imgs[0]) if imgs else 0, imgs[0][:75] if imgs else "")

# try campmor beta ar slug guesses
for slug in ["arcteryx-mens-beta-ar-jacket", "arcteryx-beta-ar-jacket", "mens-arcteryx-beta-ar-jacket"]:
    html = curl(f"https://www.campmor.com/products/{slug}")
    if "404" in html[:800]:
        print("404", slug)
        continue
    imgs = re.findall(r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*", html)
    imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
    print("slug", slug, sz(imgs[0]) if imgs else 0)
