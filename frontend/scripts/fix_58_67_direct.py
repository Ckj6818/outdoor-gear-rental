#!/usr/bin/env python3
import subprocess
from pathlib import Path

UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
OUT = Path(__file__).resolve().parents[1] / "public" / "images" / "gears"


def sz(u, ref=None):
    cmd = ["curl", "-sL", "-A", UA, "-o", "NUL", "-w", "%{size_download}", u]
    if ref:
        cmd.extend(["-e", ref])
    return int(subprocess.run(cmd, capture_output=True, text=True).stdout or 0)


def save(gid, url, ref=None):
    cmd = ["curl", "-sL", "-A", UA, "--max-time", "30", url]
    if ref:
        cmd.extend(["-e", ref])
    data = subprocess.run(cmd, capture_output=True).stdout
    if len(data) < 8000:
        print("fail", gid, len(data), url[-70:])
        return False
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    print("ok", gid, len(data))
    return True

arc_urls = [
    "https://images.arcteryx.com/pdp/beta-ar-jacket/Mens/Black/Beta-AR-Jacket-Black-Front-View.jpg?w=1200",
    "https://images.arcteryx.com/details/960x960/beta-ar-jacket/Mens/Black/Beta-AR-Jacket-Black-Front-View.jpg",
    "https://images.arcteryx.com/pdp/beta-ar-jacket/Mens/Kingfisher/Beta-AR-Jacket-Kingfisher-Front-View.jpg?w=1200",
    "https://images-dynamic-arcteryx.imgix.net/details/960x960/beta-ar-jacket/Mens/Black/Beta-AR-Jacket-Black-Front-View.jpg",
]
ref = "https://arcteryx.com/"
for u in arc_urls:
    print("arc sz", sz(u, ref), u[-60:])
    if sz(u, ref) > 8000:
        save(58, u, ref)
        break

deuter_urls = [
    "https://www.deuter.com/dw/image/v2/BDJM_PRD/on/demandware.static/-/Sites-deuter-master-catalog/default/dw3940021/images/large/3940021-70000_black_01.jpg?sw=800",
    "https://www.deuter.com/dw/image/v2/BDJM_PRD/on/demandware.static/-/Sites-deuter-master-catalog/default/dw3940021/images/large/3940021-70000_orchid_01.jpg?sw=800",
    "https://www.deuter.com/on/demandware.static/-/Sites-deuter-master-catalog/default/dw3940021/images/large/3940021-70000_black_01.jpg",
    "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/b_0adb84e4-a95c-4c68-9a1d-308c0644102d.png?v=1774279840",
]
ref = "https://www.deuter.com/"
for u in deuter_urls:
    print("deuter sz", sz(u, ref), u[-60:])
    if "3940021" in u and sz(u, ref) > 8000:
        save(67, u, ref)
        break
