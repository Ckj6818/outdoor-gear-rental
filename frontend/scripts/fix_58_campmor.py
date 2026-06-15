#!/usr/bin/env python3
import re, subprocess
from pathlib import Path

UA = "Mozilla/5.0"
OUT = Path(__file__).resolve().parents[1] / "public" / "images" / "gears"


def curl(u, t=15):
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", str(t), u], capture_output=True).stdout


def sz(u):
    return int(subprocess.run(["curl", "-sL", "-A", UA, "-o", "NUL", "-w", "%{size_download}", u], capture_output=True, text=True).stdout or 0)


def save(gid, url):
    data = curl(url)
    if len(data) < 8000:
        return False
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    print("saved", gid, len(data), url[:95])
    return True

html = curl("https://www.campmor.com/search?q=arcteryx+beta").decode("utf-8", errors="ignore")
slugs = list(dict.fromkeys(re.findall(r"/products/(arcteryx[a-z0-9-]+)", html)))
print("slugs", len(slugs))
for slug in slugs[:8]:
    p = curl(f"https://www.campmor.com/products/{slug}").decode("utf-8", errors="ignore")
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
        p,
    )
    imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
    if imgs:
        print(slug, sz(imgs[0]), imgs[0][:90])
        if "beta" in slug and save(58, imgs[0]):
            break

# imgix fallback
if not (OUT / "58-main.jpg").exists() or (OUT / "58-main.jpg").stat().st_size < 8000:
    for u in [
        "https://images-dynamic-arcteryx.imgix.net/pdp/beta-ar-jacket/Mens/Black/Beta-AR-Jacket-Black-Front-View.jpg?w=960&q=75&auto=format",
        "https://images-dynamic-arcteryx.imgix.net/details/960x960/beta-ar-jacket/Mens/Black/Beta-AR-Jacket-Black-Front-View.jpg?w=960",
    ]:
        print("imgix", sz(u))
        if save(58, u):
            break

print("58 size", (OUT / "58-main.jpg").stat().st_size if (OUT / "58-main.jpg").exists() else 0)
