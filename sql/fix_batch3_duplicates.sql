USE outdoor_gear_rental;

-- 第三批中与已有商品重复的名称/品类调整
UPDATE gear_info SET
  gear_name = 'Cosmic 20 合成睡袋',
  brand = 'Kelty',
  description = 'Kelty Cosmic 20 合成睡袋，20°F 温标，三季露营入门之选。',
  main_image = 'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_d760bb7d-4417-4db4-a4ff-255304754f7d.jpg?v=1747239767',
  hover_image = 'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_d760bb7d-4417-4db4-a4ff-255304754f7d.jpg?v=1747239767'
WHERE id = 76;

UPDATE gear_info SET
  gear_name = 'Distance Carbon Z 登山杖',
  brand = 'Black Diamond',
  category = '登山杖',
  daily_rent = 16.00,
  description = 'Black Diamond Distance Carbon Z 折叠碳纤维登山杖，轻量高刚性，适合速穿与长线。',
  main_image = 'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_6cfddf3c-4a2d-4509-b94a-99aa95ef0269.jpg?v=1747226072',
  hover_image = 'https://cdn.shopify.com/s/files/1/0301/4023/5913/files/a_6cfddf3c-4a2d-4509-b94a-99aa95ef0269.jpg?v=1747226072'
WHERE id = 78;
