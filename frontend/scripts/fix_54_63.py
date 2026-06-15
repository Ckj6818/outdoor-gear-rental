#!/usr/bin/env python3
import subprocess
from pathlib import Path

UA = "Mozilla/5.0"
OUT = Path(__file__).resolve().parents[1] / "public" / "images" / "gears"
MIN = 8000

FIXES = {
    54: "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_2df866f3-c64f-4b98-827d-ff4f2b0af3b3.jpg?v=1747245617",  # campmor storm/spot family - replace if better found
    63: "https://www.blackdiamondequipment.com/dw/image/v2/AAUE_PRD/on/demandware.static/-/Sites-bdel/default/dw123/images/large/410330_chalk_bag.jpg",
}

# Better storm: campmor black diamond storm 400-r
FIXES[54] = "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_2df866f3-c64f-4b98-827d-ff4f2b0af3b3.jpg?v=1747245617"

for gid, url in FIXES.items():
    data = subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "30", url], capture_output=True).stdout
    dest = OUT / f"{gid}-main.jpg"
    hover = OUT / f"{gid}-hover.jpg"
    if len(data) >= MIN:
        dest.write_bytes(data)
        hover.write_bytes(data)
        print(f"fixed {gid} {len(data)}")
    else:
        print(f"fail {gid} {len(data)}")
