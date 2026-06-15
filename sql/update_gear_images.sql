USE outdoor_gear_rental;

-- 统一本地商品图路径（对应 frontend/public/images/gears/{id}-main.jpg）
UPDATE gear_info SET
  main_image  = CONCAT('/images/gears/', id, '-main.jpg'),
  hover_image = CONCAT('/images/gears/', id, '-hover.jpg')
WHERE id BETWEEN 1 AND 30 OR id BETWEEN 51 AND 70;
