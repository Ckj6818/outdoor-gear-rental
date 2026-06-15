#!/usr/bin/env python3
import json, re, subprocess
from pathlib import Path

UA = "Mozilla/5.0"
OUT = Path(__file__).resolve().parents[1] / "public" / "images" / "gears"


def curl(u):
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "25", u], capture_output=True).stdout


def save(gid, url):
    data = curl(url)
    if len(data) < 8000:
        print("fail", len(data), url[:100])
        return False
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    print("saved", gid, len(data))
    return True

for q in ["arcteryx beta", "arcteryx beta sl", "arcteryx beta jacket"]:
    raw = curl(f"https://www.campmor.com/search/suggest.json?q={q.replace(' ','+')}")
    ps = json.loads(raw).get("resources", {}).get("results", {}).get("products", [])
    print("Q", q, len(ps))
    for p in ps:
        slug = p["url"].split("/products/")[-1].split("?")[0]
        html = curl(f"https://www.campmor.com/products/{slug}").decode("utf-8", errors="ignore")
        imgs = re.findall(
            r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
            html,
        )
        imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
        print(" ", p["title"][:55], imgs[0][:80] if imgs else "none")
        if imgs and ("beta ar" in p["title"].lower() or "beta sl" in p["title"].lower() or ("beta jacket" in p["title"].lower() and "men" in p["title"].lower())):
            if save(58, imgs[0]):
                raise SystemExit(0)

# hardcoded from earlier successful campmor scrape
for u in [
    "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_46e71d84-01bd-42be-a45f-a49b7c8d8e8e.jpg?v=1747164527",
    "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_4372afdb-2fd5-4ebb-b53e-ac6d3f8e0f0a.jpg?v=1747164527",
    "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_9181a556-53fb-4383-9158-0f66a8d8e8e8.jpg?v=1747164527",
]:
    if save(58, u):
        break
