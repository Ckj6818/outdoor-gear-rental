#!/usr/bin/env python3
import json, subprocess
UA = "Mozilla/5.0"
for url in [
    "https://www.blackdiamondequipment.com/products/storm-400-r-headlamp.json",
    "https://www.blackdiamondequipment.com/en_US/products/storm-400-r-headlamp.json",
    "https://rab.equipment/products/neutrino-800-sleeping-bag.json",
    "https://www.rab.equipment/products/neutrino-800.json",
]:
    raw = subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "20", url], capture_output=True).stdout
    ok = raw[:1] == b"{"
    print(url.split("/")[-1], "OK" if ok else "FAIL", len(raw))
    if ok:
        d = json.loads(raw)
        for img in d["product"]["images"][:2]:
            print(" ", img["src"][:100])
