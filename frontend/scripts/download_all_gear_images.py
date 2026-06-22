#!/usr/bin/env python3
"""Build verified image URL manifest and download all gear images."""

from __future__ import annotations

import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "public" / "images" / "gears"
SCRIPTS = Path(__file__).resolve().parent
MANIFEST = SCRIPTS / "gear_image_manifest.json"
UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36"
MIN = 8000


def curl(url: str, referer: str | None = None) -> bytes:
    cmd = ["curl", "-sL", "-A", UA, "--max-time", "35", url]
    if referer:
        cmd.extend(["-e", referer])
    return subprocess.run(cmd, capture_output=True).stdout or b""


def curl_text(url: str) -> str:
    return curl(url).decode("utf-8", errors="ignore")


def size(url: str) -> int:
    r = subprocess.run(
        ["curl", "-sL", "-A", UA, "--max-time", "20", "-o", "NUL", "-w", "%{size_download}", url],
        capture_output=True,
        text=True,
    )
    return int(r.stdout.strip() or 0)


def shopify_images(json_url: str) -> list[str]:
    raw = curl(json_url)
    if not raw or raw[:1] != b"{":
        return []
    data = json.loads(raw)
    return [img["src"] for img in data["product"]["images"]]


def campmor_images(slug: str) -> list[str]:
    html = curl_text(f"https://www.campmor.com/products/{slug}")
    imgs = re.findall(
        r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|jpeg|png|webp)[^\s\"'\\]*",
        html,
    )
    out: list[str] = []
    seen = set()
    for u in imgs:
        if "extensions" in u or u in seen:
            continue
        seen.add(u)
        out.append(u)
    return out


def save(data: bytes, path: Path) -> bool:
    if len(data) < MIN:
        return False
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)
    return True


def try_urls(urls: list[str], dest: Path, referer: str | None = None) -> str | None:
    for u in urls:
        if not u:
            continue
        if save(curl(u, referer), dest):
            return u
    return None


# gear_id -> config
# cascade: Shopify JSON on cascadedesigns.com
# campmor: product slug on campmor.com
# direct: explicit image URLs (main/hover)
GEAR: dict[int, dict] = {
    1: {"direct": {"main": ["https://www.osprey.com/media/catalog/product/cache/bdd72cdc4bced30f3cf62267d5fe3824/_/0/_0006_s24_stratos36_seaweedmatchagreen_10005794_2_web_1.jpg"], "hover": ["https://www.osprey.com/media/catalog/product/cache/302d7df2a97518ad2bae6dcc163ab237/_/0/_0006_s24_stratos36_seaweedmatchagreen_10005794_2_web.jpg"]}},
    2: {"direct": {"main": ["https://www.mysteryranchuk.com/cdn/shop/files/Terraframe-2065-20112383_black-10_jpg.jpg?v=1723468112"], "hover": ["https://www.mysteryranchuk.com/cdn/shop/files/Terraframe-2065-20112383_black-_Body-20Panel_-1010_jpg.jpg?v=1723468112"]}},
    3: {"direct": {"main": ["https://www.osprey.com/media/catalog/product/cache/b2f1ce2dfe10d3d31bf2056bf6e0d10f/H/i/Hikelite26_S26_Side_GraphitePurple_Hi-res.jpg"]}},
    4: {"direct": {"main": ["https://www.osprey.com/media/catalog/product/cache/bdd72cdc4bced30f3cf62267d5fe3824/e/x/exos58_s22_side_tungstengrey.jpg"], "hover": ["https://www.osprey.com/gb/media/catalog/product/_/0/_0002_exos58_s22_sideback_tungstengrey_10004019_web_2.jpg"]}},
    5: {"campmor": "mystery-ranch-radix-31-backpack", "direct": {"main": ["https://www.mysteryranchuk.com/cdn/shop/files/Radix-31-11027-Black-001.jpg?v=1723468112"]}},
    6: {"direct": {"main": ["https://www.msrgear.com/blog/wp-content/uploads/2020/07/10316_msr_hubbahubba_nx_fly.png"], "hover": ["https://www.msrgear.com/blog/wp-content/uploads/2020/07/Hubba-Hubba-tent-setup-on-backpacking-trip.jpg"]}},
    7: {"direct": {"main": ["https://nhike.ru/upload/dev2fun.imagecompress/webp/iblock/487/cj7ermumu3vlrj1s0nrv5ytzrpay4xi8/1.webp"], "hover": ["https://nhike.ru/upload/dev2fun.imagecompress/webp/iblock/e93/fpnsxbbjkycd9fqxub0qet18jx4mc041/1.webp"]}},
    8: {"campmor": "salomon-mens-wide-x-ultra-4-gtx-low-hiking-shoe"},
    9: {"campmor": "merrell-mens-moab-3-mid-gtx-hiking-boot"},
    10: {"campmor": "lowa-renegade-gtx-mid-hiking-boot-mens"},
    11: {"campmor": "hoka-anacapa-low-gtx-hiking-shoe-mens"},
    12: {"campmor": "gregory-baltoro-65-pack-mens"},
    13: {"direct": {"main": ["https://www.osprey.com/media/catalog/product/cache/b2f1ce2dfe10d3d31bf2056bf6e0d10f/A/t/AtmosAG65_S22_Side_Black-resized.jpg"], "hover": ["https://www.osprey.com/media/catalog/product/cache/b2f1ce2dfe10d3d31bf2056bf6e0d10f/A/t/AtmosAG65_S22_Back_Black-resized.jpg"]}},
    14: {"campmor": "big-agnes-blacktail-hotel-2-tent"},
    15: {"campmor": "kelty-mistral-20-sleeping-bag"},
    16: {"cascade": "pocketrocket-2-stove"},
    17: {"campmor": "black-diamond-distance-z-trekking-poles"},
    18: {"campmor": "osprey-kestrel-48-pack"},
    19: {"campmor": "deuter-aircontact-lite-50-10-sl-backpack"},
    20: {"direct": {"main": ["https://johnsonoutdoors.widen.net/content/3h1abqnmol/jpeg/1122524_primary.jpeg?w=900"], "hover": ["https://johnsonoutdoors.widen.net/content/4flr7g2heq/jpeg/1122524_alt01.jpeg?w=900"]}},
    21: {"cascade": "neoair-xlite-nxt-sleeping-pad"},
    22: {"campmor": "nemo-hornet-osmo-2p-tent"},
    23: {"campmor": "black-diamond-spot-400-headlamp"},
    24: {"campmor": "marmot-trestles-30-sleeping-bag-mens"},
    25: {"cascade": "big-zip-evo"},
    26: {"campmor": "osprey-daylite-plus-daypack-26"},
    27: {"direct": {"main": ["https://www.patagonia.com/dw/image/v2/BDJB_PRD/on/demandware.static/-/Sites-patagonia-master/default/dw85240_BLK/images/hi-res/85240_BLK.jpg"], "hover": ["https://www.patagonia.com/dw/image/v2/BDJB_PRD/on/demandware.static/-/Sites-patagonia-master/default/dw85240_BLK/images/hi-res/85240_BLK_alt1.jpg"]}},
    28: {"direct": {"main": ["https://cdn11.bigcommerce.com/s-jmhyu0/images/stencil/1280x1280/products/388/3035/Sawyer-Squeeze-Water-Filtration-System__68831.1757870500.jpg?c=2"], "hover": ["https://cdn.prod.website-files.com/61549f9352f3558157a226ea/698b4622f92f548340c80d0b_SP304-Sawyer-CNOC-Micro-Squeeze-Water-Filtration-System-Main-Image.png"]}},
    29: {"direct": {"main": ["https://johnsonoutdoors.widen.net/content/7h1abqnmol/jpeg/1122525_primary.jpeg?w=900"], "hover": ["https://johnsonoutdoors.widen.net/content/8flr7g2heq/jpeg/1122525_alt01.jpeg?w=900"]}},
    30: {"campmor": "big-agnes-copper-spur-hv-ul2-tent"},
    51: {"direct": {"main": ["https://static.garmincdn.com/en/products/010-02540-00/g/36192-D-FAN-db48c275-3ae6-4d12-b7c4-2ccf88a5c179.jpg"], "hover": ["https://static.garmincdn.com/en/products/010-02540-00/g/36192-M-FAN-c85ea7f8-06bf-4d4d-8722-3083dbdc95f5.jpg"]}},
    52: {"direct": {"main": ["http://www2.xitek.com/production/xitekpic.php?path=FgUYGwgKHHcZHQYfJRsGB1pAVXlWSQkfQAwRFhw6CSsTDEdWXSZcMUVDS1F8U0EJBAJNHwQT.jpg"], "hover": ["https://production.xitek.com/uploads/pictures/202309/thumb_fokmUcs6cCGJi1Fz1694768940.jpeg"]}},
    53: {"campmor": "petzl-corax-climbing-harness"},
    54: {"campmor": "black-diamond-storm-400-r-headlamp"},
    55: {"cascade": "z-lite-sol-sleeping-pad"},
    56: {"shopify": "https://seatosummit.com/products/aeros-pillow-premium.json"},
    57: {"cascade": "whisperlite-international-stove"},
    58: {"campmor": "arcteryx-beta-ar-jacket-mens"},
    59: {"campmor": "outdoor-research-helium-rain-pants-mens"},
    60: {"campmor": "leki-trail-al-poles"},
    61: {"cascade": "big-zip-evo"},
    62: {"direct": {"main": ["https://cdn11.bigcommerce.com/s-jmhyu0/images/stencil/1280x1280/products/388/3035/Sawyer-Squeeze-Water-Filtration-System__68831.1757870500.jpg?c=2"], "hover": ["https://cdn.prod.website-files.com/61549f9352f3558157a226ea/698b4622f92f548340c80d0b_SP304-Sawyer-CNOC-Micro-Squeeze-Water-Filtration-System-Main-Image.png"]}},
    63: {"campmor": "black-diamond-gym-sack-chalk-bag"},
    64: {"campmor": "nalgene-wide-mouth-32-oz-bottle"},
    65: {"campmor": "rab-neutrino-800-sleeping-bag"},
    66: {"cascade": "reactor-stove-systems"},
    67: {"campmor": "deuter-rain-cover-iii"},
    68: {"direct": {"main": ["https://cdn.shopify.com/s/files/1/0301/4023/5913/files/b_1263ede6-045b-4f2e-89e5-4546432b2dec.jpg?v=1770930882"], "hover": ["https://www.patagonia.com/dw/image/v2/BDJB_PRD/on/demandware.static/-/Sites-patagonia-master/default/dw84212_BLK/images/hi-res/84212_BLK_alt1.jpg"]}},
    69: {"campmor": "nemo-hornet-elite-osmo-1p-tent"},
    70: {"campmor": "osprey-ultralight-45-pack"},
}


def resolve_urls(gid: int, cfg: dict) -> tuple[list[str], list[str]]:
    main: list[str] = []
    hover: list[str] = []

    if "direct" in cfg:
        main.extend(cfg["direct"].get("main", []))
        hover.extend(cfg["direct"].get("hover", []))

    if slug := cfg.get("cascade"):
        imgs = shopify_images(f"https://cascadedesigns.com/products/{slug}.json")
        main.extend(imgs[:1])
        hover.extend(imgs[1:2])

    if json_url := cfg.get("shopify"):
        imgs = shopify_images(json_url)
        main.extend(imgs[:1])
        hover.extend(imgs[1:2])

    if slug := cfg.get("campmor"):
        imgs = campmor_images(slug)
        main.extend(imgs[:1])
        hover.extend(imgs[1:2])

    # dedupe
    def uniq(xs: list[str]) -> list[str]:
        out, seen = [], set()
        for x in xs:
            if x and x not in seen:
                seen.add(x)
                out.append(x)
        return out

    return uniq(main), uniq(hover)


def download_all() -> dict:
    report = {"ok": [], "fail": []}
    resolved: dict[str, dict] = {}

    for gid, cfg in sorted(GEAR.items()):
        main_urls, hover_urls = resolve_urls(gid, cfg)
        resolved[str(gid)] = {"main": main_urls, "hover": hover_urls}

        main_dest = OUT / f"{gid}-main.jpg"
        hover_dest = OUT / f"{gid}-hover.jpg"

        m = try_urls(main_urls, main_dest, "https://www.campmor.com/")
        h = try_urls(hover_urls, hover_dest, "https://www.campmor.com/")

        if not h and m:
            hover_dest.write_bytes(main_dest.read_bytes())
            h = m + " (copy)"

        if m:
            report["ok"].append({"id": gid, "main": m, "hover": h})
            print(f"[OK] {gid}")
        else:
            report["fail"].append({"id": gid, "candidates": main_urls[:3]})
            print(f"[FAIL] {gid}", file=sys.stderr)

    MANIFEST.write_text(json.dumps(resolved, ensure_ascii=False, indent=2), encoding="utf-8")
    (OUT / "download-report.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print(f"\nDone: {len(report['ok'])} ok, {len(report['fail'])} fail")
    return report


if __name__ == "__main__":
    raise SystemExit(0 if len(download_all()["fail"]) == 0 else 1)
