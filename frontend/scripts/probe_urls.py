#!/usr/bin/env python3
import json, re, subprocess

UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36"


def curl(url, raw=False):
    r = subprocess.run(
        ["curl", "-sL", "-A", UA, "--max-time", "30", url],
        capture_output=True,
    )
    return r.stdout if raw else (r.stdout.decode("utf-8", errors="ignore"))


def size(url):
    r = subprocess.run(
        ["curl", "-sL", "-A", UA, "-o", "NUL", "-w", "%{size_download}", url],
        capture_output=True,
        text=True,
    )
    return int(r.stdout.strip() or 0)


def shopify_json(url):
    data = curl(url, raw=True)
    if not data or data[:1] != b"{":
        return None
    d = json.loads(data)
    imgs = [i["src"] for i in d["product"]["images"]]
    return imgs


def html_shopify(page):
    html = curl(page)
    imgs = re.findall(
        r"(https://cdn\.shopify\.com[^\s\"'\\]+\.(?:jpg|jpeg|png|webp)[^\s\"'\\]*)",
        html,
    )
    out = []
    seen = set()
    for u in imgs:
        u = u.replace("\\/", "/")
        if "logo" in u.lower() or u in seen:
            continue
        seen.add(u)
        out.append(u)
    return out


if __name__ == "__main__":
    # Cascade Designs slugs
    for slug in [
        "big-zip-evo-2-liter-reservoir",
        "big-zip-evo-2l",
        "platypus-big-zip-evo-2l",
        "big-zip-evo-3-liter-reservoir",
        "reactor-stove-system",
        "msr-reactor-1-7l-stove-system",
        "neoair-xlite-nxt",
        "therm-a-rest-neoair-xlite-nxt",
        "pocketrocket-2-stove",
        "whisperlite-international-stove",
        "z-lite-sol-sleeping-pad",
    ]:
        imgs = shopify_json(f"https://cascadedesigns.com/products/{slug}.json")
        if imgs:
            print("CD", slug, size(imgs[0]), imgs[0][:90])

    pages = {
        8: "https://www.campmor.com/products/salomon-mens-wide-x-ultra-4-gtx-low-hiking-shoe",
        9: "https://www.campmor.com/products/merrell-mens-moab-3-mid-gtx-hiking-boot",
        14: "https://www.campmor.com/products/big-agnes-blacktail-hotel-2-tent",
        24: "https://www.campmor.com/products/marmot-trestles-30-sleeping-bag",
        5: "https://www.campmor.com/products/mystery-ranch-radix-31-backpack",
        19: "https://www.campmor.com/products/deuter-aircontact-lite-50-10-backpack",
        18: "https://www.rei.com/product/222967/osprey-kestrel-48-pack",
    }
    for k, u in pages.items():
        imgs = html_shopify(u)
        print(k, len(imgs), size(imgs[0]) if imgs else 0, imgs[0][:85] if imgs else "none")
