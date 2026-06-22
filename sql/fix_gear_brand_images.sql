USE outdoor_gear_rental;

-- 修复商品图与品牌/名称不符（本地图重复拷贝导致）
-- 写入 CDN 地址，前端 gearImages.js 会优先使用数据库字段

-- Patagonia Nano Puff 原与 Beta AR 共用同一张图
UPDATE gear_info SET
  main_image  = 'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/b_1263ede6-045b-4f2e-89e5-4546432b2dec.jpg?v=1770930882',
  hover_image = 'https://www.patagonia.com/dw/image/v2/BDJB_PRD/on/demandware.static/-/Sites-patagonia-master/default/dw84212_BLK/images/hi-res/84212_BLK_alt1.jpg'
WHERE gear_name LIKE '%Nano Puff%';

-- Exos 58 与 Terraframe 误用同一张图
UPDATE gear_info SET
  main_image  = 'https://www.osprey.com/media/catalog/product/cache/bdd72cdc4bced30f3cf62267d5fe3824/e/x/exos58_s22_side_tungstengrey.jpg',
  hover_image = 'https://www.osprey.com/gb/media/catalog/product/_/0/_0002_exos58_s22_sideback_tungstengrey_10004019_web_2.jpg'
WHERE gear_name LIKE 'Exos 58%';

-- Baltoro 65 与 Radix 31 误用同一张图
UPDATE gear_info SET
  main_image  = 'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_ac8f45f7-a270-4aa2-89b1-bded70a9be24.jpg?v=1747239607',
  hover_image = '/images/gears/12-hover.jpg'
WHERE gear_name LIKE 'Baltoro 65%';

-- Radix 31 独立图
UPDATE gear_info SET
  main_image  = 'https://www.mysteryranchuk.com/cdn/shop/files/Radix-31-11027-Black-001.jpg?v=1723468112',
  hover_image = '/images/gears/5-hover.jpg'
WHERE gear_name LIKE 'Radix 31%';

-- Terraframe 65 独立图
UPDATE gear_info SET
  main_image  = 'https://www.mysteryranchuk.com/cdn/shop/files/Terraframe-2065-20112383_black-10_jpg.jpg?v=1723468112',
  hover_image = 'https://www.mysteryranchuk.com/cdn/shop/files/Terraframe-2065-20112383_black-_Body-20Panel_-1010_jpg.jpg?v=1723468112'
WHERE gear_name LIKE 'Terraframe 65%';
