USE outdoor_gear_rental;

-- 修复 Terraframe 65 远程图片防盗链导致无法加载
UPDATE gear_info SET
  main_image = '/images/gears/2-main.jpg',
  hover_image = '/images/gears/2-hover.jpg'
WHERE id = 2;

-- 新增户外装备
INSERT INTO gear_info (gear_name, brand, category, daily_rent, total_stock, available_stock, description, main_image, hover_image, status) VALUES
('Baltoro 65 重装背包',        'Gregory',       '重装背包',   68.00, 5,  4,  'Gregory Baltoro 65，Response A3 背负系统，适合多日重装徒步与登山。',              '/images/gears/12-main.jpg', '/images/gears/12-hover.jpg', 1),
('Atmos AG 65 重装背包',       'Osprey',        '重装背包',   62.00, 6,  5,  'Osprey Atmos AG 65，Anti-Gravity 空速背板，长途徒步舒适透气。',                    '/images/gears/13-main.jpg', '/images/gears/13-hover.jpg', 1),
('Blacktail 2 双人帐篷',       'Big Agnes',     '帐篷',       52.00, 8,  6,  'Big Agnes Blacktail 2，经典双人三季帐，快速搭建，适合徒步露营。',                  '/images/gears/14-main.jpg', '/images/gears/14-hover.jpg', 1),
('Mistral 20 羽绒睡袋',        'Kelty',         '睡袋',       22.00, 10, 8,  'Kelty Mistral 20°F 羽绒睡袋，适合春秋三季露营与徒步露营。',                       '/images/gears/15-main.jpg', '/images/gears/15-hover.jpg', 1),
('PocketRocket 2 炉具套装',    'MSR',           '炉具',       18.00, 15, 12, 'MSR PocketRocket 2 超轻气炉，含锅具，适合单人轻量化露营。',                        '/images/gears/16-main.jpg', '/images/gears/16-hover.jpg', 1),
('Distance Z 碳纤维登山杖',    'Black Diamond', '登山杖',     12.00, 20, 16, 'Black Diamond Distance Z 折叠碳纤维登山杖，轻量耐用，适合长距离徒步。',              '/images/gears/17-main.jpg', '/images/gears/17-hover.jpg', 1),
('Kestrel 48 超轻背包',        'Osprey',        '轻量化背包', 48.00, 7,  6,  'Osprey Kestrel 48，稳定背负与合理容量，适合2-3日轻装徒步。',                        '/images/gears/18-main.jpg', '/images/gears/18-hover.jpg', 1);
