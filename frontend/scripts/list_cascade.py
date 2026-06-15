#!/usr/bin/env python3
import json, subprocess, sys
UA = "Mozilla/5.0"
raw = subprocess.run(
    ["curl", "-sL", "-A", UA, "--max-time", "40", "https://cascadedesigns.com/products.json?limit=250"],
    capture_output=True,
).stdout
d = json.loads(raw)
keys = sys.argv[1:] or ["big zip", "reactor", "neoair", "platypus", "whisper", "pocket", "z lite", "hubba"]
for p in d["products"]:
    t = p["title"].lower()
    if any(k in t for k in keys):
        img = p["images"][0]["src"] if p["images"] else ""
        sys.stdout.buffer.write(f"{p['handle']} | {p['title']} | {img}\n".encode("utf-8", errors="replace"))
