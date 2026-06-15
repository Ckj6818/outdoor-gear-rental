#!/usr/bin/env python3
import json, re, subprocess

UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36"
OUT = []


def curl(url):
    return subprocess.run(
        ["curl", "-sL", "-A", UA, "--max-time", "30", url],
        capture_output=True,
    ).stdout.decode("utf-8", errors="ignore")


def size(url):
    r = subprocess.run(
        ["curl", "-sL", "-A", UA, "-o", "NUL", "-w", "%{size_download}", url],
        capture_output=True,
        text=True,
    )
    return int(r.stdout.strip() or 0)


def shopify(url):
    raw = subprocess.run(
        ["curl", "-sL", "-A", UA, "--max-time", "25", url],
        capture_output=True,
    ).stdout
    if not raw or raw[:1] != b"{":
        return []
    d = json.loads(raw)
    return [i["src"] for i in d["product"]["images"]]


def campmor(slug):
    page = f"https://www.campmor.com/products/{slug}"
    html = curl(page)
    imgs = re.findall(
        r"(https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|jpeg|png|webp)[^\s\"'\\]*)",
        html,
    )
    good = [u for u in dict.fromkeys(imgs) if "extensions" not in u]
    return good


def log(label, imgs):
    if imgs and size(imgs[0]) > 8000:
        print(f"OK {label} {size(imgs[0])} {imgs[0][:95]}")
        return imgs
    print(f"-- {label} {size(imgs[0]) if imgs else 0}")
    return []


# Osprey US catalog
osprey = {
    1: "https://www.osprey.com/media/catalog/product/cache/bdd72cdc4bced30f3cf62267d5fe3824/_/0/_0006_s24_stratos36_seaweedmatchagreen_10005794_2_web_1.jpg",
    3: "https://www.osprey.com/media/catalog/product/cache/b2f1ce2dfe10d3d31bf2056bf6e0d10f/H/i/Hikelite26_S26_Side_GraphitePurple_Hi-res.jpg",
    4: "https://www.osprey.com/media/catalog/product/cache/bdd72cdc4bced30f3cf62267d5fe3824/e/x/exos58_s22_side_tungstengrey.jpg",
    13: "https://www.osprey.com/media/catalog/product/cache/b2f1ce2dfe10d3d31bf2056bf6e0d10f/A/t/AtmosAG65_S22_Side_Black-resized.jpg",
    18: "https://www.osprey.com/media/catalog/product/cache/bdd72cdc4bced30f3cf62267d5fe3824/k/e/kestrel48_s23_side_tungstengrey.jpg",
    26: "https://www.osprey.com/media/catalog/product/cache/bdd72cdc4bced30f3cf62267d5fe3824/d/a/dayliteplus26_s23_side_black.jpg",
    70: "https://www.osprey.com/media/catalog/product/cache/bdd72cdc4bced30f3cf62267d5fe3824/u/l/ultralight45_s23_side_tungstengrey.jpg",
}
for k, u in osprey.items():
    print(f"osprey {k} {size(u)}")

# Cascade search via sitemap products
for slug in [
    "neoair-xlite-nxt-regular",
    "neoair-xlite-nxt",
    "therm-a-rest-neoair-xlite-nxt-regular",
    "platypus-big-zip-evo-2-liter",
    "big-zip-evo-2-liter",
    "big-zip-evo-3-liter",
    "msr-reactor-1-7-liter-stove-system",
    "reactor-1-7-liter-stove-system",
    "msr-reactor-stove-system",
]:
    imgs = shopify(f"https://cascadedesigns.com/products/{slug}.json")
    if imgs:
        print("CD", slug, size(imgs[0]), imgs[0][:90])

campmor_slugs = {
    5: "mystery-ranch-radix-31-backpack",
    9: "merrell-mens-moab-3-mid-gtx-hiking-boot",
    10: "lowa-mens-renegade-gtx-mid-hiking-boot",
    11: "hoka-mens-anacapa-low-gtx-hiking-shoe",
    12: "gregory-baltoro-65-backpack",
    15: "kelty-mistral-20-degree-sleeping-bag",
    17: "black-diamond-distance-z-trekking-poles",
    19: "deuter-aircontact-lite-50-10-backpack",
    21: "therm-a-rest-neoair-xlite-nxt-sleeping-pad",
    22: "nemo-hornet-osmo-2-person-tent",
    23: "black-diamond-spot-400-headlamp",
    24: "marmot-trestles-30-sleeping-bag",
    25: "platypus-big-zip-evo-2-liter-reservoir",
    30: "big-agnes-copper-spur-hv-ul2-tent",
    54: "black-diamond-storm-400-headlamp",
    58: "arcteryx-beta-ar-jacket-mens",
    60: "leki-trail-al-poles",
    64: "nalgene-wide-mouth-32-oz-bottle",
    65: "rab-neutrino-800-sleeping-bag",
    67: "deuter-rain-cover-iii",
    69: "nemo-hornet-elite-osmo-1-person-tent",
}
for gid, slug in campmor_slugs.items():
    log(gid, campmor(slug))

# Sea to Summit / widen / other direct
direct = {
    56: shopify("https://seatosummit.com/products/aeros-pillow-premium.json"),
    20: ["https://johnsonoutdoors.widen.net/content/3h1abqnmol/jpeg/1122524_primary.jpeg?w=900"],
    29: ["https://johnsonoutdoors.widen.net/content/7h1abqnmol/jpeg/1122525_primary.jpeg?w=900"],
    28: ["https://sawyer.com/wp-content/uploads/2019/01/Squeeze-SP2129-Primary.jpg"],
    51: ["https://static.garmincdn.com/en/products/010-02540-00/g/36192-D-FAN-db48c275-3ae6-4d12-b7c4-2ccf88a5c179.jpg"],
    2: ["https://www.mysteryranchuk.com/cdn/shop/files/Terraframe-2065-20112383_black-10_jpg.jpg?v=1723468112"],
    7: ["https://nhike.ru/upload/dev2fun.imagecompress/webp/iblock/487/cj7ermumu3vlrj1s0nrv5ytzrpay4xi8/1.webp"],
}
for k, imgs in direct.items():
    log(k, imgs)

# Arc'teryx image pattern
for u in [
    "https://images.arcteryx.com/pdp/beta-ar-jacket/Mens/Kingfisher/Beta-AR-Jacket-Kingfisher-Front-View.jpg?w=800",
    "https://images.arcteryx.com/pdp/beta-ar-jacket/Mens/Black/Beta-AR-Jacket-Black-Front-View.jpg?w=800",
]:
    print("arc", size(u), u[-60:])

# Petzl
for u in [
    "https://www.petzl.com/global/catalog/product/image/primary/1200/c38a-corax-harness.jpg",
    "https://assets.petzl.com/media/catalog/product/c/o/corax_1.jpg",
    "https://www.petzl.com/US/en/Sport/Corax-Harness/C38A",
]:
    if u.endswith(".jpg"):
        print("petzl", size(u))
    else:
        html = curl(u)
        m = re.search(r'og:image["\'][^>]+content=["\']([^"\']+)', html)
        print("petzl html", m.group(1)[:80] if m else "none", "sz", size(m.group(1)) if m else 0)
