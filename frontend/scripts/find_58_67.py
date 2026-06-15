#!/usr/bin/env python3
import json, re, subprocess
UA = "Mozilla/5.0"

def curl(u):
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "25", u], capture_output=True).stdout.decode("utf-8", errors="ignore")

def sz(u):
    return int(subprocess.run(["curl", "-sL", "-A", UA, "-o", "NUL", "-w", "%{size_download}", u], capture_output=True, text=True).stdout or 0)

def campmor(q):
    raw = subprocess.run(["curl", "-sL", "-A", UA, f"https://www.campmor.com/search/suggest.json?q={q.replace(' ','+')}"], capture_output=True).stdout
    d = json.loads(raw)
    return d.get("resources", {}).get("results", {}).get("products", [])

for q in ["beta ar jacket men", "arcteryx beta ar", "deuter rain cover iii", "deuter rain cover backpack"]:
    print("===", q)
    for p in campmor(q)[:5]:
        slug = p["url"].split("/products/")[-1]
        html = curl(f"https://www.campmor.com/products/{slug}")
        imgs = re.findall(
            r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
            html,
        )
        imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
        if imgs:
            print(p["title"][:55], sz(imgs[0]), imgs[0][:90])

# rab neutrino from rab json partial
raw = subprocess.run(["curl", "-sL", "-A", UA, "https://www.rab.equipment/products/neutrino-800.json"], capture_output=True).stdout
if raw[:1] == b"{":
    d = json.loads(raw)
    u = d["product"]["images"][0]["src"]
    print("rab json", sz(u), u[:90])
