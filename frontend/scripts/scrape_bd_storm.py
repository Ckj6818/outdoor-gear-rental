#!/usr/bin/env python3
import json, re, subprocess

UA = "Mozilla/5.0"
URL = "https://www.blackdiamondequipment.com/en_US/product/storm-400-r-headlamp/"


def curl(u):
    return subprocess.run(["curl", "-sL", "-A", UA, "--max-time", "25", u], capture_output=True).stdout.decode("utf-8", errors="ignore")


def sz(u):
    return int(subprocess.run(["curl", "-sL", "-A", UA, "-o", "NUL", "-w", "%{size_download}", u], capture_output=True, text=True).stdout or 0)


html = curl(URL)
for block in re.findall(r'<script[^>]*type="application/json"[^>]*>(.*?)</script>', html, re.S):
    if "Storm" in block or "620284" in block or "images" in block:
        try:
            data = json.loads(block)
        except json.JSONDecodeError:
            continue
        text = json.dumps(data)
        for u in re.findall(r"https://cdn\.shopify\.com/s/files/1/0880/2195/8973/files/[^\"\\]+\.(?:jpg|jpeg|png|webp)[^\"\\]*", text):
            print("json-img", sz(u.replace("\\/", "/")), u.replace("\\/", "/")[:100])

for u in re.findall(r"https://cdn\.shopify\.com/s/files/1/0880/2195/8973/files/[^\"'\s>\\]+\.(?:jpg|jpeg|png|webp)[^\"'\s>\\]*", html):
    u = u.replace("\\/", "/")
    if "logo" not in u.lower() and "seo" not in u.lower() and "halpin" not in u.lower():
        print("html-img", sz(u), u[:100])
