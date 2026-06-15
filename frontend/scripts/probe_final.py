#!/usr/bin/env python3
import json, re, subprocess
UA = "Mozilla/5.0"

def search(q):
    raw = subprocess.run(["curl","-sL","-A",UA,f"https://www.campmor.com/search/suggest.json?q={q.replace(' ','+')}"],capture_output=True).stdout
    d=json.loads(raw)
    return d.get("resources",{}).get("results",{}).get("products",[])

def sz(u):
    return int(subprocess.run(["curl","-sL","-A",UA,"--max-time","12","-o","NUL","-w","%{size_download}",u],capture_output=True,text=True).stdout or 0)

def page_img(url):
    html=subprocess.run(["curl","-sL","-A",UA,url],capture_output=True).stdout.decode('utf-8','ignore')
    og=re.search(r'property=\"og:image\" content=\"([^\"]+)\"',html)
    if og and sz(og.group(1))>8000: return og.group(1)
    imgs=re.findall(r'https://cdn\.shopify\.com/s/files/1/0301/4023/5913/files/[^\s\"\'\\]+\.(?:jpg|png|webp)[^\s\"\'\\]*',html)
    imgs=[i for i in dict.fromkeys(imgs) if 'extensions' not in i]
    return imgs[0] if imgs and sz(imgs[0])>8000 else None

for q in ["storm 400 headlamp","petzl corax","beta ar","rab neutrino","deuter rain cover","chalk bag","anacapa hoka"]:
    ps=search(q)
    print('Q',q,'hits',len(ps))
    for p in ps[:3]:
        u='https://www.campmor.com'+p['url'] if p['url'].startswith('/') else p['url']
        img=page_img(u)
        print(' ',p['title'][:50], img[:85] if img else 'none')

# hoka official og
for u in ["https://www.hoka.com/en/us/anacapa-low-gtx-1127915-bblc.html","https://www.backcountry.com/hoka-anacapa-low-gtx-hiking-shoe-mens"]:
    html=subprocess.run(["curl","-sL","-A",UA,u],capture_output=True).stdout.decode('utf-8','ignore')
    og=re.search(r'og:image[^>]+content=\"([^\"]+)\"',html)
    print('OG',u.split('/')[2], sz(og.group(1)) if og else 0, og.group(1)[:80] if og else 'none')
