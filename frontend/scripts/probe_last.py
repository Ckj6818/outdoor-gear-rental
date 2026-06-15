#!/usr/bin/env python3
import subprocess
UA = "Mozilla/5.0"
def sz(u):
    return int(subprocess.run(["curl","-sL","-A",UA,"--max-time","15","-o","NUL","-w","%{size_download}",u],capture_output=True,text=True).stdout or 0)
urls = [
("11a","https://cdn.shopify.com/s/files/1/0301/4023/5913/files/1127915-BBLC_1.jpg"),
("11b","https://www.hoka.com/dw/image/v2/AAQM_PRD/on/demandware.static/-/Sites-HOKA-US/default/dw1a2b3c4d/images/ProductImages/1127915-BBLC_1.jpg"),
("11c","https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_0c0e0e0e.jpg"),
("53a","https://www.petzl.com/global/catalog/product/image/primary/2400/c38a-corax-harness.jpg"),
("53b","https://assets.petzl.com/media/catalog/product/c/o/corax_1.jpg"),
("53c","https://cdn.shopify.com/s/files/1/0301/4023/5913/files/petzl-corax.jpg"),
("54a","https://cdn.shopify.com/s/files/1/0880/2195/8973/files/620284_Storm400R_Headlamp_Primary.jpg"),
("54b","https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_2df866f3-c64f-4b98-827d-ff4f2b0af3b3.jpg"),
("58a","https://images.arcteryx.com/pdp/beta-ar-jacket/Mens/Black/Beta-AR-Jacket-Black-Front-View.jpg"),
("58b","https://images.arcteryx.com/pdp/beta-ar-jacket/Mens/Kingfisher/Beta-AR-Jacket-Kingfisher-Front-View.jpg"),
("58c","https://cdn.shopify.com/s/files/1/0301/4023/5913/files/arcteryx-beta-ar.jpg"),
("63a","https://cdn.shopify.com/s/files/1/0880/2195/8973/files/BD_GymSack_ChalkBag_Primary.jpg"),
("63b","https://www.blackdiamondequipment.com/dw/image/v2/AAUE_PRD/on/demandware.static/-/Sites-bdel/default/dw123/images/large/410330_chalk_bag.jpg"),
("65a","https://www.rab.equipment/cdn/shop/files/Neutrino800_SleepingBag_Orange_01.jpg"),
("65b","https://cdn.shopify.com/s/files/1/0301/4023/5913/files/rab-neutrino.jpg"),
("67a","https://www.deuter.com/dw/image/v2/BDJM_PRD/on/demandware.static/-/Sites-deuter-master-catalog/default/dw3940021/images/large/3940021-70000_raincover_01.jpg"),
("67b","https://cdn.shopify.com/s/files/1/0301/4023/5913/files/deuter-rain-cover.jpg"),
]
for k,u in urls:
    print(k, sz(u))
