#!/usr/bin/env python3
"""下载与商品一一对应的官方/授权零售商商品图到 public/images/gears/"""

from __future__ import annotations

import json
import re
import subprocess
import sys
from pathlib import Path
from urllib.parse import urljoin, urlparse

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "public" / "images" / "gears"
MANIFEST = Path(__file__).resolve().parent / "gear-image-sources.json"
UA = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
)
MIN_BYTES = 6000

# 官方商品页：下载失败时从页面提取 og:image / shopify CDN
PRODUCT_PAGES: dict[int, str] = {
    2: "https://www.mysteryranchuk.com/products/terraframe-65",
    5: "https://www.mysteryranchuk.com/products/radix-31",
    7: "https://nhike.ru/palatka-naturehike-sloud-up-2-nh17t001-t-20d-dvukhmestnaya-s-kovrikom-sinyaya/",
    8: "https://www.campmor.com/products/salomon-mens-wide-x-ultra-4-gtx-low-hiking-shoe",
    9: "https://www.campmor.com/products/salomon-x-ultra-4-mid-gtx-hiking-boot-mens-1",
    10: "https://www.lowa.com/en-us/mens-renegade-gtx-mid",
    11: "https://www.hoka.com/en/us/anacapa-low-gtx-1127915-bblc.html",
    12: "https://www.gregorypacks.com/backpacks/multi-day-backpacks/baltoro-65-GREGORY00888.html",
    14: "https://www.bigagnes.com/products/blacktail-hotel-2",
    15: "https://www.kelty.com/products/mistral-20-sleeping-bag",
    16: "https://www.msrgear.com/stoves/stove-systems/pocketrocket-2-stove/06980.html",
    17: "https://www.blackdiamondequipment.com/en_US/product/distance-z-trekking-poles/",
    19: "https://www.deuter.com/en-de/backpacks-and-bags/backpacks/hiking-backpacks/aircontact-lite-50-10-sl-3200021-70000",
    20: "https://jetboil.johnsonoutdoors.com/us/shop/stoves-systems/flash-10l-fast-boil-system",
    21: "https://cascadedesigns.com/products/neoair-xlite-nxt-sleeping-pad",
    22: "https://www.nemoequipment.com/products/hornet-osmo-2p-tent",
    23: "https://www.blackdiamondequipment.com/en_US/product/spot-r-400-headlamp/",
    24: "https://www.marmot.com/men/trestles-30-sleeping-bag",
    25: "https://www.platy.com/hydration/reservoirs/big-zip-evo-2l",
    28: "https://sawyer.com/products/squeeze-water-filtration-system/",
    29: "https://jetboil.johnsonoutdoors.com/us/shop/stoves-systems/minimo-cooking-system",
    30: "https://www.bigagnes.com/products/copper-spur-hv-ul2-tent",
    51: "https://www.garmin.com.sg/products/wearables/fenix-7-silver/",
    52: "https://gopro.com/en/pg/shop/cameras/hero12-black/CHDHX-121-master.html",
    53: "https://www.petzl.com/US/en/Sport/Corax-Harness/C38A",
    54: "https://www.blackdiamondequipment.com/en_US/product/storm-400-r-headlamp/",
    55: "https://cascadedesigns.com/products/z-lite-sol-sleeping-pad",
    56: "https://seatosummit.com/products/aeros-pillow-premium",
    57: "https://www.msrgear.com/stoves/liquid-fuel-stoves/whisperlite-international-stove/06646.html",
    58: "https://arcteryx.com/us/en/shop/mens/beta-ar-jacket",
    59: "https://www.outdoorresearch.com/us/mens-helium-rain-pants-265446",
    60: "https://www.leki.com/products/trail-al-poles",
    64: "https://nalgene.com/product/32oz-wide-mouth-bottle/",
    65: "https://www.rab.equipment/us/neutrino-800-sleeping-bag",
    66: "https://www.msrgear.com/stoves/stove-systems/reactor-1-7l-stove-system/06985.html",
    67: "https://www.deuter.com/en-de/accessories/rain-covers/rain-cover-iii-3940021-70000",
    68: "https://www.patagonia.com/product/mens-nano-puff-jacket/84212.html",
    69: "https://www.nemoequipment.com/products/hornet-elite-osmo-1p-tent",
    61: "https://www.platy.com/hydration/reservoirs/big-zip-evo-3l",
    63: "https://www.blackdiamondequipment.com/en_US/product/gym-sack-chalk-bag/",
}


def curl_bytes(url: str, referer: str | None = None) -> bytes:
    cmd = ["curl", "-sL", "-A", UA, "--max-time", "45", url]
    if referer:
        cmd.extend(["-e", referer])
    result = subprocess.run(cmd, capture_output=True)
    return result.stdout or b""


def fetch_html(url: str) -> str:
    data = curl_bytes(url, referer=url)
    return data.decode("utf-8", errors="ignore")


def extract_images_from_html(html: str, base_url: str) -> list[str]:
    found: list[str] = []

    for pattern in (
        r'<meta[^>]+property=["\']og:image["\'][^>]+content=["\']([^"\']+)',
        r'<meta[^>]+content=["\']([^"\']+)["\'][^>]+property=["\']og:image',
        r'"preview_image"\s*:\s*\{[^}]*"src"\s*:\s*"([^"]+)"',
        r'"(?:src|image|featured_image)"\s*:\s*"(https?:\\?/\\?/[^"]+?\.(?:jpg|jpeg|png|webp)[^"]*)"',
        r'(https?:\\/\\/cdn\.shopify\.com[^"\\]+\.(?:jpg|jpeg|png|webp)[^"\\]*)',
        r'(https://cdn\.shopify\.com[^"\s>]+\.(?:jpg|jpeg|png|webp)[^"\s>]*)',
        r'(https?:\\/\\/[^"\\]+\.(?:jpg|jpeg|png|webp)[^"\\]*)',
        r'(https://[^"\s>]+\.(?:jpg|jpeg|png|webp)(?:\?[^"\s>]*)?)',
        r'(//[^"\s>]+\.(?:jpg|jpeg|png|webp)(?:\?[^"\s>]*)?)',
        r'dw/image/v2/[^"\s>]+\.(?:jpg|jpeg|png|webp)[^"\s>]*',
    ):
        for match in re.findall(pattern, html, flags=re.I):
            url = match.replace("\\/", "/")
            if url.startswith("//"):
                url = "https:" + url
            elif url.startswith("/"):
                url = urljoin(base_url, url)
            if url.startswith("dw/image"):
                url = "https://www.blackdiamondequipment.com/" + url
            skip = ("icon", "logo", "banner", "avatar", "sprite", "favicon")
            if not any(s in url.lower() for s in skip):
                found.append(url)

    # 去重保序
    unique: list[str] = []
    seen = set()
    for url in found:
        if url not in seen:
            seen.add(url)
            unique.append(url)
    return unique


def save_image(data: bytes, dest: Path) -> bool:
    if len(data) < MIN_BYTES:
        return False
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_bytes(data)
    return True


def try_download(urls: list[str], dest: Path, referer: str | None = None) -> str | None:
    for url in urls:
        if not url or url.startswith("https://www.petzl.com/US/en/Sport/"):
            continue
        data = curl_bytes(url, referer=referer or url)
        if save_image(data, dest):
            return url
    return None


def download_for_item(item: dict, suffix: str, url_key: str) -> tuple[str | None, list[str]]:
    gear_id = item["id"]
    dest = OUT_DIR / f"{gear_id}-{suffix}.jpg"
    candidates = list(item.get(url_key, []))

    page = PRODUCT_PAGES.get(gear_id)
    if page and url_key == "main":
        html = fetch_html(page)
        candidates.extend(extract_images_from_html(html, page))
    elif page and url_key == "hover" and not candidates:
        html = fetch_html(page)
        imgs = extract_images_from_html(html, page)
        if len(imgs) > 1:
            candidates.append(imgs[1])
        elif imgs:
            candidates.append(imgs[0])

    # Johnson Outdoors widen 链接转 jpeg
    normalized = []
    for url in candidates:
        if "widen.net" in url and "/webp/" in url:
            normalized.append(url.replace("/webp/", "/jpeg/").split("?")[0] + "?w=900")
        else:
            normalized.append(url)

    used = try_download(normalized, dest, referer=page)
    return used, normalized


def main() -> int:
    items = json.loads(MANIFEST.read_text(encoding="utf-8"))
    report = {"ok": [], "fail": []}

    for item in items:
        gear_id = item["id"]
        main_used, _ = download_for_item(item, "main", "main")
        hover_used, _ = download_for_item(item, "hover", "hover")

        if not hover_used and main_used:
            # 悬停图缺失时复用主图
            src = OUT_DIR / f"{gear_id}-main.jpg"
            dst = OUT_DIR / f"{gear_id}-hover.jpg"
            if src.exists():
                dst.write_bytes(src.read_bytes())
                hover_used = main_used + " (copied)"

        if main_used:
            report["ok"].append(
                {"id": gear_id, "name": item["name"], "main": main_used, "hover": hover_used}
            )
            print(f"[OK] {gear_id} {item['name']}")
        else:
            report["fail"].append({"id": gear_id, "name": item["name"]})
            print(f"[FAIL] {gear_id} {item['name']}", file=sys.stderr)

    (OUT_DIR / "download-report.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print(f"\nDone: {len(report['ok'])} ok, {len(report['fail'])} fail")
    return 0 if not report["fail"] else 1


if __name__ == "__main__":
    raise SystemExit(main())
