USE outdoor_gear_rental;

-- 新增户外装备（图片由前端 gearImages.js 按分类/id 映射展示）
INSERT INTO gear_info (gear_name, brand, category, daily_rent, total_stock, available_stock, description, main_image, hover_image, condition_grade, status) VALUES
('Fenix 7 户外GPS手表',           'Garmin',          '电子设备',   45.00, 6,  5,  'Garmin Fenix 7，多星定位与长续航，适合长线徒步与越野跑导航。',                     '', '', '9成新', 1),
('Hero 12 运动相机',              'GoPro',           '电子设备',   38.00, 8,  7,  'GoPro Hero 12，5.3K 视频，HyperSmooth 防抖，记录户外精彩瞬间。',                   '', '', '全新', 1),
('Corax 攀岩安全带',              'Petzl',           '攀岩装备',   28.00, 10, 8,  'Petzl Corax 全可调安全带，适合入门攀岩与室内抱石保护。',                           '', '', '9成新', 1),
('Storm 400 头灯',                'Black Diamond',   '头灯',       15.00, 14, 11, 'Black Diamond Storm 400，800 流明，IP67 防水，适合夜爬与露营。',                  '', '', '全新', 1),
('Z Lite Sol 泡沫睡垫',           'Ther-a-Rest',     '睡垫',       18.00, 12, 10, 'Ther-a-Rest Z Lite Sol，折叠泡沫睡垫，R 值 2.0，轻量化徒步露营。',                '', '', '9成新', 1),
('Aeros Premium 充气枕',          'Sea to Summit',   '配件',       8.00,  20, 16, 'Sea to Summit Aeros Premium，轻量充气枕，提升露营睡眠舒适度。',                    '', '', '全新', 1),
('WhisperLite 国际版柴油炉',      'MSR',             '炉具',       25.00, 6,  5,  'MSR WhisperLite International，多燃料柴油炉，适合高海拔与极寒环境。',               '', '', '9成新', 1),
('Beta AR 硬壳冲锋衣',            'Arc''teryx',      '户外服装',   55.00, 5,  4,  'Arc''teryx Beta AR，Gore-Tex Pro 面料，全天候防风防水，适合登山徒步。',            '', '', '9成新', 1),
('Helium Rain 防雨裤',            'Outdoor Research','户外服装',   22.00, 10, 8,  'Outdoor Research Helium Rain， Pertex Shield 防雨裤，轻量易打包。',                 '', '', '全新', 1),
('Trail 铝合金登山杖',            'Leki',            '登山杖',     14.00, 18, 14, 'Leki Trail 三节铝合金登山杖，EVA 握把，适合中低海拔徒步。',                        '', '', '9成新', 1),
('Platypus Big Zip 3L 水袋',      'Platypus',        '水具',       10.00, 16, 13, 'Platypus Big Zip EVO 3L，宽口易清洗，适合单日徒步补水。',                          '', '', '全新', 1),
('Squeeze 便携滤水器',            'Sawyer',          '水具',       12.00, 15, 12, 'Sawyer Squeeze 0.1 微米滤水器，可过滤细菌与原虫，野营必备。',                     '', '', '9成新', 1),
('Black Diamond 粉袋套装',        'Black Diamond',   '攀岩装备',   8.00,  12, 10, 'Black Diamond 攀岩粉袋 + 镁粉，室内抱石与运动攀辅助。',                            '', '', '全新', 1),
('Nalgene 宽口 1L 水杯',          'Nalgene',         '水具',       5.00,  25, 20, 'Nalgene 宽口 1L 水杯，BPA Free，耐摔耐用，徒步标配。',                             '', '', '全新', 1),
('Rab Neutrino 800 羽绒睡袋',     'Rab',             '睡袋',       35.00, 6,  5,  'Rab Neutrino 800，-12°C 舒适温标，850 蓬松度鹅绒，高海拔露营。',                   '', '', '9成新', 1),
('MSR Reactor 1.7L 反应堆炉',     'MSR',             '炉具',       32.00, 5,  4,  'MSR Reactor 1.7L，内置热交换器，极寒环境快速烧水。',                               '', '', '9成新', 1),
('Deuter Rain Cover 防雨罩',      'Deuter',          '配件',       6.00,  20, 17, 'Deuter 背包防雨罩，多尺寸可选，应对突发降雨。',                                    '', '', '全新', 1),
('Patagonia Nano Puff 棉服',      'Patagonia',       '户外服装',   42.00, 7,  6,  'Patagonia Nano Puff，Primaloft 金标填充，轻量保暖中间层。',                        '', '', '9成新', 1),
('NEMO Hornet Elite 1P 帐篷',     'NEMO',            '帐篷',       58.00, 4,  3,  'NEMO Hornet Elite 1P，DAC 帐杆，单人超轻三季帐，仅约 700g。',                     '', '', '全新', 1),
('Osprey Ultralight 45L 背包',    'Osprey',          '轻量化背包', 52.00, 6,  5,  'Osprey Ultralight 45L，Fit-on-the-Fly 腰带，适合超轻长线徒步。',                  '', '', '9成新', 1);
