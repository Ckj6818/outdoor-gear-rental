#!/usr/bin/env python3
import json, re, subprocess, sys

UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36"
QUERIES = {
    9: "merrell moab 3 mid gtx",
    10: "lowa renegade gtx mid",
    11: "hoka anacapa low gtx",
    12: "gregory baltoro 65",
    15: "kelty mistral 20",
    18: "osprey kestrel 48",
    19: "deuter aircontact lite 50",
    22: "nemo hornet osmo 2",
    24: "marmot trestles 30",
    26: "osprey daylite plus 26",
    30: "big agnes copper spur hv ul2",
    53: "petzl corax harness",
    54: "black diamond storm 400",
    58: "arcteryx beta ar jacket",
    59: "outdoor research helium rain pants",
    60: "leki trail al poles",
    63: "black diamond chalk bag",
    64: "nalgene wide mouth 32",
    65: "rab neutrino 800",
    67: "deuter rain cover",
    69: "nemo hornet elite 1p",
    70: "osprey ultralight 45",
}

DIRECT = {
    6: "https://cdn.shopify.com/s/files/1/0574/0642/3174/files/13315_msr_eu_hubbahubba_nx_fly_open.jpg?v=1724820235",
    18: "https://www.osprey.com/media/catalog/product/cache/bdd72cdc4bced30f3cf62267d5fe3824/k/e/kestrel48_s23_side_tungstengrey.jpg",
    26: "https://www.osprey.com/media/catalog/product/cache/bdd72cdc4bced30f3cf62267d5fe3824/d/a/dayliteplus26_s23_side_black.jpg",
    70: "https://www.osprey.com/media/catalog/product/cache/bdd72cdc4bced30f3cf62267d5fe3824/u/l/ultralight45_s23_side_tungstengrey.jpg",
    10: "https://www.lowa.com/dw/image/v2/BDJM_PRD/on/demandware.static/-/Sites-lowa-master-catalog/default/dw8e8e8e8e/images/large/L41085500_001.jpg?sw=800",
    9: "https://www.merrell.com/dw/image/v2/AAUE_PRD/on/demandware.static/-/Sites-merrell_us-Library/default/dw9c9c9c9c/images/large/J035475_1.jpg?sw=800",
    58: "https://images.arcteryx.com/pdp/beta-ar-jacket/Mens/Black/Beta-AR-Jacket-Black-Front-View.jpg?w=1200",
    64: "https://nalgene.com/wp-content/uploads/2021/03/32oz_WideMouth_Blue_2021.jpg",
    53: "https://www.petzl.com/global/catalog/product/image/primary/2400/c38a-corax-harness.jpg",
}


def curl(url):
    return subprocess.run(
        ["curl", "-sL", "-A", UA, "--max-time", "25", url],
        capture_output=True,
    ).stdout.decode("utf-8", errors="ignore")


def sz(url):
    return int(
        subprocess.run(
            ["curl", "-sL", "-A", UA, "--max-time", "15", "-o", "NUL", "-w", "%{size_download}", url],
            capture_output=True,
            text=True,
        ).stdout
        or 0
    )


def campmor_search(q):
    url = "https://www.campmor.com/search/suggest.json?q=" + q.replace(" ", "+")
    raw = subprocess.run(["curl", "-sL", "-A", UA, url], capture_output=True).stdout
    try:
        d = json.loads(raw)
    except json.JSONDecodeError:
        return []
    out = []
    for p in d.get("resources", {}).get("results", {}).get("products", []):
        out.append(p.get("url", "").replace("https://www.campmor.com", ""))
    return out


def campmor_imgs(slug):
    html = curl(f"https://www.campmor.com{slug}" if slug.startswith("/") else f"https://www.campmor.com/products/{slug}")
    return re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|jpeg|png|webp)[^\s\"'\\]*",
        html,
    )


results = {}
for gid, q in QUERIES.items():
    best = None
    for slug in campmor_search(q)[:3]:
        imgs = [i for i in campmor_imgs(slug) if "extensions" not in i]
        if imgs and sz(imgs[0]) > 8000:
            best = imgs[0]
            break
    if not best and gid in DIRECT:
        u = DIRECT[gid]
        if sz(u) > 8000:
            best = u
    results[gid] = best
    sys.stdout.buffer.write(f"{gid}\t{best or 'FAIL'}\n".encode("utf-8", errors="replace"))

Path = __import__("pathlib").Path
Path(__file__).with_name("fallback_urls.json").write_text(
    json.dumps({str(k): v for k, v in results.items() if v}, indent=2), encoding="utf-8"
)
