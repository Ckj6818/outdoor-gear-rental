#!/usr/bin/env python3
import json, re, subprocess
from pathlib import Path

UA = "Mozilla/5.0"
SCRIPT = Path(__file__).parent


def sz(u):
    return int(subprocess.run(["curl","-sL","-A",UA,"--max-time","12","-o","NUL","-w","%{size_download}",u],capture_output=True,text=True).stdout or 0)

def search(q):
    raw = subprocess.run(["curl","-sL","-A",UA,f"https://www.campmor.com/search/suggest.json?q={q.replace(' ','+')}"],capture_output=True).stdout
    try: d=json.loads(raw)
    except: return []
    return [p["url"].split("/products/")[-1] for p in d.get("resources",{}).get("results",{}).get("products",[])]

def imgs(slug):
    html = subprocess.run(["curl","-sL","-A",UA,f"https://www.campmor.com/products/{slug}"],capture_output=True).stdout.decode("utf-8","ignore")
    xs = re.findall(r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|jpeg|png|webp)[^\s\"'\\]*", html)
    return [x for x in dict.fromkeys(xs) if "extensions" not in x]

extra = {
    11: ["hoka anacapa gtx", "hoka anacapa mid"],
    12: ["gregory baltoro", "gregory pack baltoro"],
    26: ["osprey daylite plus", "osprey daylite 26"],
    30: ["copper spur ul2", "big agnes copper spur"],
    53: ["petzl corax", "petzl harness corax"],
    54: ["storm 400 headlamp", "black diamond storm"],
    58: ["beta ar jacket", "arcteryx beta ar"],
    60: ["leki trail", "leki trekking poles"],
    63: ["chalk bag black diamond", "bd chalk bag"],
    65: ["rab neutrino", "neutrino 800 sleeping bag"],
    67: ["deuter rain cover", "deuter backpack cover"],
    69: ["hornet elite 1p", "nemo hornet elite"],
    70: ["osprey ultralight pack", "osprey ultralight 45"],
}

direct = {
    58: "https://images.arcteryx.com/pdp/beta-ar-jacket/Mens/Black/Beta-AR-Jacket-Black-Front-View.jpg?w=1200",
    53: "https://www.petzl.com/global/catalog/product/image/primary/2400/c38a-corax-harness.jpg",
    54: "https://cdn.shopify.com/s/files/1/0880/2195/8973/files/620284_Storm400R_Headlamp_Primary.jpg",
    30: "https://cdn.shopify.com/s/files/1/0647/7171/9417/files/CopperSpurHVUL2_Orange_01.jpg",
    69: "https://cdn.shopify.com/s/files/1/0264/4071/2726/files/HornetEliteOSMO1P_Tent_Green_01.jpg",
}

found = {}
for gid, queries in extra.items():
    best = None
    for q in queries:
        for slug in search(q)[:4]:
            im = imgs(slug)
            if im and sz(im[0]) > 8000:
                best = im[0]
                break
        if best: break
    if not best and gid in direct and sz(direct[gid]) > 8000:
        best = direct[gid]
    found[str(gid)] = best
    print(gid, sz(best) if best else 0, (best or "FAIL")[:90])

existing = json.loads((SCRIPT / "fallback_urls.json").read_text(encoding="utf-8"))
existing.update({k: v for k, v in found.items() if v})
(SCRIPT / "fallback_urls.json").write_text(json.dumps(existing, ensure_ascii=False, indent=2), encoding="utf-8")
