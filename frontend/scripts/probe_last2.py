#!/usr/bin/env python3
import json, re, subprocess
UA = "Mozilla/5.0"

def curl(u):
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "20", u], capture_output=True).stdout.decode("utf-8", errors="ignore")

def sz(u):
    return int(subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "12", "-o", "NUL", "-w", "%{size_download}", u], capture_output=True, text=True).stdout or 0)

h = curl("https://www.blackdiamondequipment.com/en_US/product/storm-400-r-headlamp/")
for pat in [
    r'property="og:image" content="([^"]+)"',
    r"(https://cdn\.shopify\.com/s/files/1/0880/2195/8973/files/[^\"'\s]+\.(?:jpg|png|webp)[^\"'\s]*)",
]:
    for m in re.findall(pat, h):
        print("storm", sz(m), m[:95])

raw = subprocess.run(["curl", "-sL", "-A", UA, "https://rab.equipment/products/neutrino-800.json"], capture_output=True).stdout
if raw[:1] == b"{":
    d = json.loads(raw)
    u = d["product"]["images"][0]["src"]
    print("rab", sz(u), u[:95])

h = curl("https://www.deuter.com/en-de/accessories/rain-covers/rain-cover-iii-3940021-70000")
m = re.search(r'property="og:image" content="([^"]+)"', h)
if m:
    print("deuter", sz(m.group(1)), m.group(1)[:95])
imgs = re.findall(r"(https://[^\"'\s>]+\.(?:jpg|png|webp)[^\"'\s>]*)", h)
for u in imgs[:5]:
    if "deuter" in u.lower() and sz(u) > 8000:
        print("deuter-img", sz(u), u[:95])

for slug in ["arcteryx-mens-beta-ar-jacket", "arcteryx-beta-ar-jacket-mens", "mens-beta-ar-jacket"]:
    p = curl(f"https://www.campmor.com/products/{slug}")
    if "404 Not Found" in p:
        continue
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
        p,
    )
    imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
    if imgs:
        print("arc", slug, sz(imgs[0]), imgs[0][:95])

h = curl("https://www.campmor.com/search?q=hoka+anacapa")
for slug in dict.fromkeys(re.findall(r"/products/([a-z0-9-]+)", h)):
    if "hoka" not in slug and "anacapa" not in slug:
        continue
    p = curl(f"https://www.campmor.com/products/{slug}")
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
        p,
    )
    imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
    if imgs and sz(imgs[0]) > 8000:
        print("hoka", slug, sz(imgs[0]), imgs[0][:95])

# petzl corax campmor product page
p = curl("https://www.campmor.com/products/petzl-corax-climbing-harness")
imgs = re.findall(r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*", p)
for u in dict.fromkeys(imgs):
    if sz(u) > 8000:
        print("petzl", sz(u), u[:95])
