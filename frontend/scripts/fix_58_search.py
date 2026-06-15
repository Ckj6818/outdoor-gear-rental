#!/usr/bin/env python3
import re, subprocess
from pathlib import Path

UA = "Mozilla/5.0"
OUT = Path(__file__).resolve().parents[1] / "public" / "images" / "gears"


def curl(u, t=20):
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", str(t), u], capture_output=True).stdout.decode("utf-8", errors="ignore")


def sz(u):
    return int(subprocess.run(["curl", "-sL", "-A", UA, "-o", "NUL", "-w", "%{size_download}", u], capture_output=True, text=True).stdout or 0)


def save(gid, url):
    data = subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "30", url], capture_output=True).stdout
    if len(data) < 8000:
        return False
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    print("saved", gid, len(data), url[:95])
    return True

for q in ["arcteryx beta ar", "arcteryx shell jacket mens", "arcteryx hardshell mens"]:
    html = curl(f"https://www.campmor.com/search?q={q.replace(' ', '+')}")
    slugs = list(dict.fromkeys(re.findall(r"/products/([a-z0-9-]+)", html)))
    print("Q", q, "slugs", len(slugs))
    for slug in slugs:
        if "arcteryx" not in slug:
            continue
        p = curl(f"https://www.campmor.com/products/{slug}")
        title = re.search(r"<title>([^<]+)", p)
        t = (title.group(1) if title else slug).lower()
        if "beta" not in t and "shell" not in t and "jacket" not in t:
            continue
        imgs = re.findall(
            r"https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"'\\]+\.(?:jpg|png|webp)[^\s\"'\\]*",
            p,
        )
        imgs = [i for i in dict.fromkeys(imgs) if "extensions" not in i]
        if imgs:
            print(" ", t[:60], sz(imgs[0]))
            if ("beta" in t or "shell" in t) and save(58, imgs[0]):
                raise SystemExit(0)

# Atom SL womens as last-resort Arc'teryx shell jacket photo
save(58, "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/b_8e2871fb-60c7-47c3-9a02-5b840adbd4a0.jpg?v=1747164527")
