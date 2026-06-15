#!/usr/bin/env python3
import json, subprocess, sys
from pathlib import Path

out = Path(__file__).with_name("cascade_products.txt")
lines = []
for page in range(1, 6):
    raw = subprocess.run(
        ["curl", "-sL", "-A", "Mozilla/5.0", f"https://cascadedesigns.com/products.json?limit=250&page={page}"],
        capture_output=True,
    ).stdout
    if not raw or raw[:1] != b"{":
        break
    for p in json.loads(raw)["products"]:
        img = p["images"][0]["src"] if p["images"] else ""
        lines.append(f"{p['handle']}\t{p['title']}\t{img}")

out.write_text("\n".join(lines), encoding="utf-8")
print(f"Wrote {len(lines)} products to {out}")
