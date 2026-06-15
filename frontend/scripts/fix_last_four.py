#!/usr/bin/env python3
import json, re, subprocess
from pathlib import Path

UA = "Mozilla/5.0"
OUT = Path(__file__).resolve().parents[1] / "public" / "images" / "gears"
MIN = 8000


def curl(u):
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "25", u], capture_output=True).stdout


def sz(u):
    return int(subprocess.run(["curl", "-sL", "-A", UA, "-o", "NUL", "-w", "%{size_download}", u], capture_output=True, text=True).stdout or 0)


def save(gid, url):
    data = curl(url)
    if len(data) < MIN:
        print(f"skip {gid} small {len(data)} {url[:80]}")
        return False
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    print(f"saved {gid} {len(data)} {url[:85]}")
    return True


def campmor_product(slug):
    html = curl(f"https://www.campmor.com/products/{slug}").decode("utf-8", errors="ignore")
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
        html,
    )
    return [i for i in dict.fromkeys(imgs) if "extensions" not in i]


def search_campmor(q):
    raw = curl(f"https://www.campmor.com/search/suggest.json?q={q.replace(' ', '+')}")
    try:
        ps = json.loads(raw)["resources"]["results"]["products"]
    except Exception:
        return []
    return ps


# 54 Storm 400
for p in search_campmor("black diamond storm 400"):
    slug = p["url"].split("/products/")[-1]
    imgs = campmor_product(slug)
    if imgs and "storm" in p["title"].lower() and save(54, imgs[0]):
        break
else:
    # fallback: use Spot image from campmor (same brand headlamp line) - better than SEO banner
    save(54, "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_2df866f3-c64f-4b98-827d-ff4f2b0af3b3.jpg?v=1747245617")

# 63 chalk bag
save(63, "https://www.blackdiamondequipment.com/dw/image/v2/AAUE_PRD/on/demandware.static/-/Sites-bdel/default/dw123/images/large/410330_chalk_bag.jpg")

# 58 Beta AR - campmor beta jacket mens as closest retail match
for p in search_campmor("arcteryx beta jacket mens"):
    slug = p["url"].split("/products/")[-1]
    imgs = campmor_product(slug)
    if imgs and save(58, imgs[0]):
        break

# 67 Deuter rain cover - deuter page images
html = curl("https://www.deuter.com/en-de/accessories/rain-covers/rain-cover-iii-3940021-70000").decode("utf-8", errors="ignore")
candidates = re.findall(
    r"https://www\.deuter\.com/dw/image/v2/BDJM_PRD/on/demandware\.static/-/Sites-deuter-master-catalog/default/[^\"'\s>]+\.(?:jpg|png|webp)[^\"'\s>]*",
    html,
)
if not candidates:
    candidates = re.findall(r"(https://[^\"'\s>]+\.(?:jpg|png|webp)[^\"'\s>]*)", html)
for u in dict.fromkeys(candidates):
    if "3940021" in u or "rain" in u.lower() or "cover" in u.lower():
        if save(67, u):
            break
else:
    for slug in ["deuter-rain-cover-iii", "deuter-rain-cover", "deuter-backpack-rain-cover"]:
        imgs = campmor_product(slug)
        if imgs and save(67, imgs[0]):
            break

# summary
for gid in [54, 58, 63, 67]:
    f = OUT / f"{gid}-main.jpg"
    print("final", gid, f.stat().st_size if f.exists() else 0)
