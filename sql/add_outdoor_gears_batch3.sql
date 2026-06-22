USE outdoor_gear_rental;

-- 第三批新增户外装备（图片使用 CDN 地址，与品牌/名称一致）
INSERT INTO gear_info (id, gear_name, brand, category, daily_rent, total_stock, available_stock, description, main_image, hover_image, condition_grade, status) VALUES
(71, 'Watertight II 冲锋衣',           'Columbia',   '户外服装',   32.00, 10, 9,  'Columbia Watertight II，Omni-Tech 防水透气，入门徒步雨具。',                          'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/b_7ae646d3-ec5a-480b-9a48-e7cbcccf3b78.jpg?v=1760370884', 'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/b_7ae646d3-ec5a-480b-9a48-e7cbcccf3b78.jpg?v=1760370884', '全新', 1),
(72, 'Trestles 30 合成睡袋',           'Marmot',     '睡袋',       28.00, 8,  7,  'Marmot Trestles 30，SpiraFil 填充，30°F 舒适温标，三季露营。',                       'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_e371be8a-554a-4db3-ba5c-013b9d81281a.png?v=1776185078', 'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_e371be8a-554a-4db3-ba5c-013b9d81281a.png?v=1776185078', '9成新', 1),
(73, 'X Ultra 4 GTX 徒步鞋',           'Salomon',    '徒步鞋',     40.00, 12, 10, 'Salomon X Ultra 4 GTX，Gore-Tex 防水，Contagrip 大底，多日徒步。',                     'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/L412892005_5d8f3bc8-7c72-4046-a489-fca6d7b0539f.jpg?v=1747434836', 'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/L412892005_5d8f3bc8-7c72-4046-a489-fca6d7b0539f.jpg?v=1747434836', '9成新', 1),
(74, 'Nomad 10 太阳能充电板',          'Goal Zero',  '电子设备',   35.00, 6,  5,  'Goal Zero Nomad 10，10W 太阳能板，可为手机/GPS 应急充电。',                            'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/11900-3.jpg?v=1747417865', 'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/11900-3.jpg?v=1747417865', '全新', 1),
(75, 'Rim Runner X30 跑山水袋包',      'CamelBak',   '水具',       30.00, 8,  7,  'CamelBak Rim Runner X30，2L 水袋，30L 容量，越野跑与单日徒步。',                       'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_8afceab6-31ce-49d1-b50a-13370861c122.jpg?v=1747154299', 'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_8afceab6-31ce-49d1-b50a-13370861c122.jpg?v=1747154299', '9成新', 1),
(76, 'Mistral 20 羽绒睡袋',            'Kelty',      '睡袋',       24.00, 10, 8,  'Kelty Mistral 20°F 羽绒睡袋，适合春秋三季露营。',                                     'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_3bfdd849-44b8-420a-9eaa-342c7cbc6b85.jpg?v=1747239607', 'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_3bfdd849-44b8-420a-9eaa-342c7cbc6b85.jpg?v=1747239607', '9成新', 1),
(77, 'Intraknit 美利奴底层',           'Smartwool',  '户外服装',   18.00, 14, 12, 'Smartwool Intraknit 美利奴羊毛底层，透气保暖，高海拔徒步。',                           'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_97d5b750-f26e-4711-9268-9ad8ef226095.jpg?v=1747233259', 'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_97d5b750-f26e-4711-9268-9ad8ef226095.jpg?v=1747233259', '全新', 1),
(78, 'MiniMo Carbon 炉具系统',         'Jetboil',    '炉具',       28.00, 7,  6,  'Jetboil MiniMo Carbon，精密火焰控制，轻量一体化烹饪系统。',                            'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_b3973720-b607-41fd-92d8-7609a16f161e.jpg?v=1747238247', 'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_b3973720-b607-41fd-92d8-7609a16f161e.jpg?v=1747238247', '9成新', 1)
ON DUPLICATE KEY UPDATE
  gear_name = VALUES(gear_name),
  brand = VALUES(brand),
  category = VALUES(category),
  daily_rent = VALUES(daily_rent),
  description = VALUES(description),
  main_image = VALUES(main_image),
  hover_image = VALUES(hover_image),
  condition_grade = VALUES(condition_grade),
  status = VALUES(status);
