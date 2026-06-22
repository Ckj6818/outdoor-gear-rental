#!/usr/bin/env python3
"""Download local copies for batch3 gear (ids 71-78)."""
import json
import subprocess
from pathlib import Path

UA = "Mozilla/5.0"
OUT = Path(__file__).resolve().parents[1] / "public" / "images" / "gears"
MANIFEST = Path(__file__).resolve().parent / "batch3_urls.json"
MIN = 8000

# Full URL map (includes items beyond batch3_urls search)
URLS = {
    71: "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/b_7ae646d3-ec5a-480b-9a48-e7cbcccf3b78.jpg?v=1760370884",
    72: "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_e371be8a-554a-4db3-ba5c-013b9d81281a.png?v=1776185078",
    73: "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/L412892005_5d8f3bc8-7c72-4046-a489-fca6d7b0539f.jpg?v=1747434836",
    74: "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/11900-3.jpg?v=1747417865",
    75: "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_8afceab6-31ce-49d1-b50a-13370861c122.jpg?v=1747154299",
    76: "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_3bfdd849-44b8-420a-9eaa-342c7cbc6b85.jpg?v=1747239607",
    77: "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_97d5b750-f26e-4711-9268-9ad8ef226095.jpg?v=1747233259",
    78: "https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_b3973720-b607-41fd-92d8-7609a16f161e.jpg?v=1747238247",
}


def curl(url: str) -> bytes:
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "35", url], capture_output=True).stdout or b""


def save(gid: int, url: str) -> bool:
    data = curl(url)
    if len(data) < MIN:
        print(f"FAIL {gid} {len(data)}")
        return False
    (OUT / f"{gid}-main.jpg").write_bytes(data)
    (OUT / f"{gid}-hover.jpg").write_bytes(data)
    print(f"OK {gid} {len(data)}")
    return True


if __name__ == "__main__":
    ok = sum(save(gid, url) for gid, url in URLS.items())
    print(f"done {ok}/{len(URLS)}")
