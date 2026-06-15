#!/usr/bin/env python3
import json, re, subprocess, sys
from pathlib import Path

UA = "Mozilla/5.0"
ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "public" / "images" / "gears"
FALLBACK = Path(__file__).resolve().parent / "fallback_urls.json"
MIN = 8000

EXTRA = {
    53: ["https://cdn.shopify.com/s/files/1/0301/4023/5913/products/C052AA004.jpg?v=1747224600"],
    63: ["https://www.blackdiamondequipment.com/dw/image/v2/AAUE_PRD/on/demandware.static/-/Sites-bdel/default/dw123/images/large/410330_chalk_bag.jpg"],
}


def curl(url):
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "30", url], capture_output=True).stdout


def og_image(page):
    html = curl(page).decode("utf-8", errors="ignore")
    m = re.search(r'property="og:image" content="([^"]+)"', html)
    return m.group(1) if m else None


def save(data, path):
    if len(data) < MIN:
        return False
    path.write_bytes(data)
    return True


def dl(gid, urls):
    dest = OUT / f"{gid}-main.jpg"
    hover = OUT / f"{gid}-hover.jpg"
    for u in urls:
        if u and save(curl(u), dest):
            hover.write_bytes(dest.read_bytes())
            print(f"[OK] {gid} {u[:85]}")
            return True
    print(f"[FAIL] {gid}", file=sys.stderr)
    return False


# scrape pages for remaining
PAGES = {
    11: "https://www.campmor.com/search?q=hoka+anacapa+gtx",
    54: "https://www.blackdiamondequipment.com/en_US/product/storm-400-r-headlamp/",
    58: "https://www.campmor.com/products/arcteryx-mens-beta-ar-jacket",
    65: "https://www.rab.equipment/us/product/neutrino-800-sleeping-bag",
    67: "https://www.deuter.com/en-de/accessories/rain-covers/rain-cover-iii-3940021-70000",
}

if __name__ == "__main__":
    fb = json.loads(FALLBACK.read_text(encoding="utf-8"))
    ok = fail = 0
    for gid_str, url in sorted(fb.items(), key=lambda x: int(x[0])):
        if dl(int(gid_str), [url]):
            ok += 1
        else:
            fail += 1

    # try page scrapes for still missing
    for gid in [11, 54, 58, 65, 67, 53, 63]:
        main = OUT / f"{gid}-main.jpg"
        if main.exists() and main.stat().st_size >= MIN:
            continue
        urls = []
        if page := PAGES.get(gid):
            if page.startswith("https://www.campmor.com/search"):
                html = curl(page).decode("utf-8", errors="ignore")
                slugs = re.findall(r"/products/([a-z0-9-]+)", html)
                for slug in slugs[:5]:
                    phtml = curl(f"https://www.campmor.com/products/{slug}").decode("utf-8", errors="ignore")
                    imgs = re.findall(
                        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
                        phtml,
                    )
                    urls.extend([i for i in imgs if "extensions" not in i][:1])
            else:
                img = og_image(page)
                if img:
                    urls.append(img)
                html = curl(page).decode("utf-8", errors="ignore")
                urls.extend(re.findall(r"(https://cdn\.shopify\.com[^\"'\s>]+\.(?:jpg|png|webp)[^\"'\s>]*)", html)[:2])
        urls.extend(EXTRA.get(gid, []))
        if dl(gid, urls):
            ok += 1
        else:
            fail += 1
    print(f"Patch done ok={ok} fail={fail}")
