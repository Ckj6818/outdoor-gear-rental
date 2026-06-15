#!/usr/bin/env python3
import json, re, subprocess
from pathlib import Path

UA = "Mozilla/5.0"
OUT = Path(__file__).resolve().parents[1] / "public" / "images" / "gears"


def curl(u):
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "30", u], capture_output=True).stdout


def sz(u):
    return int(subprocess.run(["curl", "-sL", "-A", UA, "-o", "NUL", "-w", "%{size_download}", u], capture_output=True, text=True).stdout or 0)


def save(gid, url):
    data = curl(url)
    if len(data) < 8000:
        print("fail", gid, len(data))
        return False
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    print("ok", gid, len(data), url[:90])
    return True

# Deuter Raincover III official page
for page in [
    "https://www.deuter.com/se-en/raincover-iii/4046051162188",
    "https://www.deuter.com/int-en/shop/accessories/p27098-rain-cover-rain-cover-iii",
]:
    html = curl(page).decode("utf-8", errors="ignore")
    og = re.search(r'property="og:image" content="([^"]+)"', html)
    if og:
        print("deuter og", sz(og.group(1)), og.group(1)[:90])
        if save(67, og.group(1)):
            break
    for u in re.findall(r"(https://www\.deuter\.com/dw/image/v2/BDJM_PRD/[^\"'\s>]+)", html):
        if sz(u) > 8000:
            print("deuter img", sz(u), u[:90])
            if save(67, u):
                break

# Arc'teryx product page scrape
html = curl("https://arcteryx.com/us/en/shop/mens/beta-ar-jacket-9906").decode("utf-8", errors="ignore")
for u in re.findall(r"(https://images\.arcteryx\.com/[^\"'\\]+\.(?:jpg|jpeg|png|webp)[^\"'\\]*)", html):
    u = u.replace("\\/", "/")
    if "Front-View" in u or "front" in u.lower():
        print("arc", sz(u), u[:90])
        if sz(u) > 8000 and save(58, u):
            break
for block in re.findall(r'<script[^>]*type="application/ld\+json"[^>]*>(.*?)</script>', html, re.S):
    if "image" in block:
        try:
            data = json.loads(block)
        except json.JSONDecodeError:
            continue
        imgs = data.get("image", [])
        if isinstance(imgs, str):
            imgs = [imgs]
        for u in imgs:
            print("ld", sz(u), u[:90])
            if sz(u) > 8000 and save(58, u):
                break

for gid in [58, 67]:
    f = OUT / f"{gid}-main.jpg"
    print("final", gid, f.stat().st_size if f.exists() else 0)
