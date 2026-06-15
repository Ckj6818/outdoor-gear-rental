#!/usr/bin/env python3
import subprocess, re

UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36"


def sz(u):
    return int(
        subprocess.run(
            ["curl", "-sL", "-A", UA, "-o", "NUL", "-w", "%{size_download}", u],
            capture_output=True,
            text=True,
        ).stdout
        or 0
    )


def html(u):
    return subprocess.run(
        ["curl", "-sL", "-A", UA, "--max-time", "30", u],
        capture_output=True,
    ).stdout.decode("utf-8", errors="ignore")


candidates = {
    5: [
        "https://www.mysteryranchuk.com/cdn/shop/files/Radix-31-11027-Black-001.jpg?v=1723468112",
        "https://www.mysteryranch.com/cdn/shop/files/Radix-31-11027-Black-001.jpg",
    ],
    18: [
        "https://www.rei.com/media/222967/222967_1.jpg",
        "https://www.osprey.com/media/catalog/product/k/e/kestrel48_s23_side_tungstengrey.jpg",
        "https://images.osprey.com/kestrel48_s23_side_tungstengrey.jpg",
    ],
    26: [
        "https://www.rei.com/media/222968/222968_1.jpg",
        "https://www.osprey.com/media/catalog/product/d/a/dayliteplus26_s23_side_black.jpg",
    ],
    70: [
        "https://www.rei.com/media/235678/235678_1.jpg",
        "https://www.osprey.com/media/catalog/product/u/l/ultralight45_s23_side_tungstengrey.jpg",
    ],
    6: [
        "https://www.msrgear.com/blog/wp-content/uploads/2020/07/10316_msr_hubbahubba_nx_fly.png",
        "https://www.msrgear.com/dw/image/v2/BDJM_PRD/on/demandware.static/-/Sites-msr-master-catalog/default/dw6e8b80be/images/large/10316_msr_hubbahubba_nx_2_body.jpg?sw=800",
    ],
    9: [
        "https://www.campmor.com/cdn/shop/files/Merrell-Moab-3-Mid-GTX-Hiking-Boot-Mens-Volcano-Primary.jpg",
    ],
    10: [
        "https://www.lowa.com/dw/image/v2/BDJM_PRD/on/demandware.static/-/Sites-lowa-master-catalog/default/dw1a1a1a1/images/large/L41085500_001.jpg?sw=800",
    ],
    19: [
        "https://www.deuter.com/dw/image/v2/BDJM_PRD/on/demandware.static/-/Sites-deuter-master-catalog/default/dw3200021/images/large/3200021-70000_anthracite-black_01.jpg",
    ],
    24: [
        "https://www.marmot.com/dw/image/v2/BBBP_PRD/on/demandware.static/-/Sites-marmot-master/default/dw123456/images/large/Trestles30_Blue.jpg",
    ],
    53: [
        "https://assets.petzl.com/media/catalog/product/c/o/corax_1.jpg",
        "https://www.petzl.com/global/catalog/product/image/primary/2400/c38a-corax-harness.jpg",
    ],
    58: [
        "https://images.arcteryx.com/pdp/beta-ar-jacket/Mens/Black/Beta-AR-Jacket-Black-Front-View.jpg",
    ],
    66: [
        "https://cdn.shopify.com/s/files/1/0574/0642/3174/files/06985_msr_reactor_stove_system.jpg",
    ],
    25: [
        "https://cdn.shopify.com/s/files/1/0574/0642/3174/files/10977_platypus_bigzip_evo_2l.jpg",
    ],
    61: [
        "https://cdn.shopify.com/s/files/1/0574/0642/3174/files/10978_platypus_bigzip_evo_3l.jpg",
    ],
    21: [
        "https://cdn.shopify.com/s/files/1/0574/0642/3174/files/11627_thermarest_neoair_xlite_nxt_regular.jpg",
    ],
}

rei_pages = {
    18: "https://www.rei.com/product/222967/osprey-kestrel-48-pack",
    26: "https://www.rei.com/product/222968/osprey-daylite-plus-daypack",
    70: "https://www.rei.com/product/235678/osprey-ultralight-45-pack",
    9: "https://www.rei.com/product/215127/merrell-moab-3-mid-waterproof-hiking-boots-mens",
    19: "https://www.rei.com/product/222970/deuter-aircontact-lite-50-plus-10-pack",
    24: "https://www.rei.com/product/210356/marmot-trestles-30-sleeping-bag",
    5: "https://www.rei.com/product/234567/mystery-ranch-radix-31-pack",
}

for gid, urls in candidates.items():
    for u in urls:
        s = sz(u)
        if s > 8000:
            print(f"OK {gid} {s} {u[:100]}")

for gid, page in rei_pages.items():
    h = html(page)
    imgs = re.findall(r"(https://www\.rei\.com/media/\d+/\d+[^\"'\s>]+\.(?:jpg|jpeg|png))", h)
    imgs = list(dict.fromkeys(imgs))
    if imgs:
        print(f"REI {gid}", sz(imgs[0]), imgs[0])
    else:
        og = re.search(r'property="og:image" content="([^"]+)"', h)
        print(f"REI {gid} og", og.group(1)[:80] if og else "none")

# platypus cascade slug brute
import json

for slug in [
    "platypus-big-zip-evo-2l-reservoir",
    "big-zip-evo-2l-reservoir",
    "big-zip-evo-2-liter",
    "big-zip-evo-3l-reservoir",
    "big-zip-evo-3-liter",
    "msr-reactor-1-7l-stove-system",
    "reactor-1-7l-stove-system",
    "neoair-xlite-nxt-regular",
    "neoair-xlite-nxt-sleeping-pad",
    "therm-a-rest-neoair-xlite-nxt-regular",
]:
    raw = subprocess.run(
        ["curl", "-sL", "-A", UA, f"https://cascadedesigns.com/products/{slug}.json"],
        capture_output=True,
    ).stdout
    if raw[:1] == b"{":
        d = json.loads(raw)
        u = d["product"]["images"][0]["src"]
        print("CD", slug, sz(u), u[:90])
